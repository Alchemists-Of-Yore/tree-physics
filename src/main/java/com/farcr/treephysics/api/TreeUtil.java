package com.farcr.treephysics.api;

import com.farcr.treephysics.api.flood_fill.TreeFloodFill;
import com.farcr.treephysics.api.flood_fill.TreeResult;
import com.farcr.treephysics.api.manager.ServerTreeManager;
import com.farcr.treephysics.client.TreeManager;
import com.farcr.treephysics.index.TreePhysicsConfig;
import com.farcr.treephysics.index.TreePhysicsTags;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TreeUtil {
    public static final BlockPos[] DIRECTION_OFFSETS_CORNERS = new BlockPos[] {
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 1, 0),
            new BlockPos(0, -1, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, -1),
            new BlockPos(1, 1, 0),
            new BlockPos(-1, -1, 0),
            new BlockPos(1, -1, 0),
            new BlockPos(-1, 1, 0),
            new BlockPos(1, 0, 1),
            new BlockPos(-1, 0, -1),
            new BlockPos(1, 0, -1),
            new BlockPos(-1, 0, 1),
            new BlockPos(0, 1, 1),
            new BlockPos(0, -1, -1),
            new BlockPos(0, -1, 1),
            new BlockPos(0, 1, -1),
            new BlockPos(1, 1, 1),
            new BlockPos(1, 1, -1),
            new BlockPos(-1, 1, -1),
            new BlockPos(-1, 1, 1),
            new BlockPos(1, -1, 1),
            new BlockPos(1, -1, -1),
            new BlockPos(-1, -1, -1),
            new BlockPos(-1, -1, 1)
    };

    private static final Vector3d DIRECTION = new Vector3d();
    private static final Vector3dc UP = new Vector3d(0, 1, 0);

    public static double getUprightness(SubLevel subLevel) {
        Vector3d direction = subLevel.logicalPose().transformNormal(DIRECTION.set(UP));
        return Math.max(0, direction.dot(UP));
    }

    public static Iterable<BlockPos> plotIterator(SubLevel subLevel) {
        BoundingBox3ic box = subLevel.getPlot().getBoundingBox();
        return BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    private static final TreeFloodFill TREE_VALIDATOR = new TreeFloodFill()
            .addRule(TreeUtil::logRule)
            .addTag(TreePhysicsTags.TREE);

    private static final TreeFloodFill ROOTLESS_TREE_VALIDATOR = new TreeFloodFill()
            .addRule(TreeUtil::logRule)
            .addRule(TreeUtil::leafRule)
            .addTag(TreePhysicsTags.TREE);

    private static final TreeFloodFill TREE_FINDER = new TreeFloodFill()
            .addRule(TreeUtil::logRule)
            .addRule(TreeUtil::leafRule)
            .addRule(TreeUtil::attachmentRule)
            .addRule(TreeUtil::fallingBlockRule)
            .addTag(TreePhysicsTags.TREE)
            .addTag(TreePhysicsTags.FALLS_FROM_TREES);

    public static boolean isValidTree(BlockGetter blockGetter, BlockPos pos) {
        boolean rootless = TreePhysicsConfig.ROOTLESS_TREE_DETECTION.getAsBoolean();
        TreeResult tree = (rootless ? ROOTLESS_TREE_VALIDATOR : TREE_VALIDATOR).findBlocks(blockGetter, pos);
        boolean isTree = tree != null && tree.hasRoot();
        if(isTree && rootless) {
            isTree = tree.hasLeaves();
        }
        return isTree;
    }

    public static List<ServerSubLevel> trySplit(ServerLevel level, BlockPos pos) {
        if(!isValidTree(level, pos)) {
            return List.of();
        }

        TreeFloodFill floodFill = TREE_FINDER.ignore(pos);

        List<ServerSubLevel> subLevels = new ArrayList<>();
        ServerTreeManager manager = (ServerTreeManager) TreeManager.get(level);

        for (BlockPos offset : DIRECTION_OFFSETS_CORNERS) {
            BlockPos start = pos.offset(offset);

            TreeResult tree = floodFill.findBlocks(level, start);

            if(tree != null && !tree.hasRoot()) {
                Set<BlockPos> treeBlocks = tree.getBlocks(TreePhysicsTags.TREE);
                ServerSubLevel subLevel = SubLevelAssemblyHelper.assembleBlocks(level, pos, treeBlocks, new BoundingBox3i(pos, pos));
                subLevels.add(subLevel);
                manager.setTree(subLevel);

                Set<BlockPos> fallingBlocks = tree.getBlocks(TreePhysicsTags.FALLS_FROM_TREES);
                for (BlockPos blockPos : fallingBlocks) {
                    SubLevelAssemblyHelper.assembleBlocks(level, blockPos, List.of(blockPos), new BoundingBox3i(blockPos, blockPos));
                }

                for (BlockPos blockPos : tree.getBlocks()) {
                    level.setBlock(blockPos, Blocks.BARRIER.defaultBlockState(), 2);
                    level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                }
            }

        }
        return subLevels;
    }

    public static boolean logRule(BlockPos fromPos, BlockPos toPos, BlockState fromState, BlockState toState, TreeResult result) {
        return result.isLog(fromState) && result.isLog(toState);
    }

    public static boolean leafRule(BlockPos fromPos, BlockPos toPos, BlockState fromState, BlockState toState, TreeResult result) {
        if(result.isLog(fromState) && result.isLeaf(toState)) {
            return true;
        }

        if(result.isLeaf(fromState) && result.isLeaf(toState)) {
            if(fromState.getBlock() == toState.getBlock()) {
                int fromDistance = fromState.getValue(LeavesBlock.DISTANCE);
                int toDistance = toState.getValue(LeavesBlock.DISTANCE);
                return toDistance > fromDistance;
            }
        }

        return false;
    }

    public static boolean attachmentRule(BlockPos fromPos, BlockPos toPos, BlockState fromState, BlockState toState, TreeResult result) {
        return !fromState.is(TreePhysicsTags.STAYS_ON_TREE) && toState.is(TreePhysicsTags.STAYS_ON_TREE);
    }

    public static boolean fallingBlockRule(BlockPos fromPos, BlockPos toPos, BlockState fromState, BlockState toState, TreeResult result) {
        return !fromState.is(TreePhysicsTags.FALLS_FROM_TREES) && toState.is(TreePhysicsTags.FALLS_FROM_TREES);
    }

    public static boolean isRoot(BlockState state) {
        return state.is(TreePhysicsTags.ROOTS);
    }

}
