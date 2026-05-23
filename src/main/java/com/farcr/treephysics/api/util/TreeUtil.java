package com.farcr.treephysics.api.util;

import com.farcr.treephysics.api.LeafGroupManager;
import com.farcr.treephysics.api.flood_fill.TreeResult;
import com.farcr.treephysics.index.TreePhysicsTags;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Set;

public class TreeUtil {

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

    public static boolean isRoot(BlockState state) {
        return state.is(TreePhysicsTags.ROOTS);
    }

    public static boolean isLog(BlockState state) {
        return state.is(TreePhysicsTags.LOGS);
    }

    public static boolean isLeaf(BlockState state) {
        return state.is(TreePhysicsTags.LEAVES);
    }

    public static boolean isSameLeafType(BlockState first, BlockState second) {
        Block firstBlock = first.getBlock();
        Block secondBlock = second.getBlock();
        if(firstBlock == secondBlock) {
            return true;
        }

        Set<Block> group = LeafGroupManager.GROUPS.get(firstBlock);
        if(group != null) {
            return group.contains(secondBlock);
        }

        return false;
    }

    public static boolean isLeafPersistent(BlockState state) {
        if(isLeaf(state)) {
            if(state.hasProperty(BlockStateProperties.PERSISTENT)) {
                return state.getValue(BlockStateProperties.PERSISTENT);
            } else {
                return false;
            }
        }

        return true;
    }

    public static int getLeafDistance(BlockState state, BlockPos pos, TreeResult result) {
        int distance = getLeafDistanceRaw(state);
        if(distance > 0) {
            return distance;
        } else {
            BlockPos start = new BlockPos(result.getStart().getX(), pos.getY(), result.getStart().getZ());
            return Math.clamp(pos.distManhattan(start), 1, BlockStateProperties.MAX_DISTANCE);
        }
    }

    public static int getLeafDistanceRaw(BlockState state) {
        if(state.hasProperty(BlockStateProperties.DISTANCE)) {
            return state.getValue(BlockStateProperties.DISTANCE);
        }

        return 0;
    }

    public static @Nullable Direction.Axis getLogAxis(BlockState state) {
        if(isLog(state)) {
            if(state.hasProperty(BlockStateProperties.AXIS)) {
                return state.getValue(BlockStateProperties.AXIS);
            }

            if(state.getBlock() instanceof HugeMushroomBlock) {
                if(!state.getValue(HugeMushroomBlock.UP) && !state.getValue(HugeMushroomBlock.DOWN)) {
                    return Direction.Axis.Y;
                } else if (!state.getValue(HugeMushroomBlock.EAST) && !state.getValue(HugeMushroomBlock.WEST)) {
                    return Direction.Axis.X;
                } else if (!state.getValue(HugeMushroomBlock.NORTH) && !state.getValue(HugeMushroomBlock.SOUTH)) {
                    return Direction.Axis.Z;
                }
            }
        }

        return null;
    }

}
