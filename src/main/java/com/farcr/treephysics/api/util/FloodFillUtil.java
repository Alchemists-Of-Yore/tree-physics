package com.farcr.treephysics.api.util;

import com.farcr.treephysics.api.flood_fill.TreeFloodFill;
import com.farcr.treephysics.api.flood_fill.TreeResult;
import com.farcr.treephysics.api.manager.ServerTreeManager;
import com.farcr.treephysics.client.TreeManager;
import com.farcr.treephysics.index.TreePhysicsConfig;
import com.farcr.treephysics.index.TreePhysicsTags;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FloodFillUtil {
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

    private static final TreeFloodFill TREE_VALIDATOR = new TreeFloodFill()
            .addRule(FloodFillUtil::logRule)
            .addTag(TreePhysicsTags.TREE);

    private static final TreeFloodFill ROOTLESS_TREE_VALIDATOR = new TreeFloodFill()
            .addRule(FloodFillUtil::logRule)
            .addRule(FloodFillUtil::leafRule)
            .addTag(TreePhysicsTags.TREE);

    private static final TreeFloodFill TREE_FINDER = new TreeFloodFill()
            .addRule(FloodFillUtil::logRule)
            .addRule(FloodFillUtil::leafRule)
            .addRule(FloodFillUtil::attachmentRule)
            .addRule(FloodFillUtil::fallingBlockRule)
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
                    BlockState state = level.getBlockState(blockPos);
                    if (PhysicsBlockPropertyHelper.getMass(level, pos, state) > 0.0) {
                        SubLevelAssemblyHelper.assembleBlocks(level, blockPos, List.of(blockPos), new BoundingBox3i(blockPos, blockPos));
                    }
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
        if(TreeUtil.isLog(fromState) && TreeUtil.isLog(toState)) {
            if(fromState.getBlock() == toState.getBlock()) {
                return true;
            } else {
                BlockPos relative = toPos.subtract(fromPos);
                Direction direction = Direction.getNearest(relative.getX(), relative.getY(), relative.getZ());

                Direction.Axis fromAxis = TreeUtil.getLogAxis(fromState);
                Direction.Axis toAxis = TreeUtil.getLogAxis(toState);
                return fromAxis == direction.getAxis() && toAxis == direction.getAxis();
            }
        }

        return false;
    }

    public static boolean leafRule(BlockPos fromPos, BlockPos toPos, BlockState fromState, BlockState toState, TreeResult result) {
        if(TreeUtil.isLog(fromState) && TreeUtil.isLeaf(toState)) {
            return true;
        }

        if(TreeUtil.isLeaf(fromState) && TreeUtil.isLeaf(toState)) {
            if(TreeUtil.isSameLeafType(fromState, toState)) {
                int fromDistance = TreeUtil.getLeafDistance(fromState, fromPos, result);
                int toDistance = TreeUtil.getLeafDistance(toState, toPos, result);
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
}
