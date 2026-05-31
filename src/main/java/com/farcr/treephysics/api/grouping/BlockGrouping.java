package com.farcr.treephysics.api.grouping;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;

public class BlockGrouping {
    private final Map<Block, Set<Block>> groups;

    public BlockGrouping(Map<Block, Set<Block>> groups) {
        this.groups = groups;
    }

    public boolean isSameType(BlockState first, BlockState second) {
        Block firstBlock = first.getBlock();
        Block secondBlock = second.getBlock();
        if(firstBlock == secondBlock) {
            return true;
        }

        Set<Block> group = this.groups.get(firstBlock);
        if(group != null) {
            return group.contains(secondBlock);
        }

        return false;
    }
}
