package com.farcr.treephysics.api;

import com.farcr.treephysics.api.tree_gathering.TreeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class TreeUtil {
    public static final BlockPos[] DIRECTION_OFFSETS = new BlockPos[] {
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
            new BlockPos(0, 1, -1)
    };

    public static boolean treeSpread(BlockState from, BlockState to, TreeContext context) {
        if(context.isLog(from)) {
            return context.isLog(to) || context.isLeaf(to);
        }

        if(context.isLeaf(from) && context.isLeaf(to)) {
            int fromDistance = from.getValue(LeavesBlock.DISTANCE);
            int toDistance = to.getValue(LeavesBlock.DISTANCE);
            return fromDistance < toDistance;
        }

        return false;
    }

    public static boolean logSpread(BlockState from, BlockState to, TreeContext context) {
        return context.isLog(from) && context.isLog(to);
    }
}
