package com.farcr.treephysics.api.flood_fill;

import com.farcr.treephysics.api.TreeUtil;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class TreeFloodFill {
    private final List<Rule> rules = new ArrayList<>();
    private final Set<TagKey<Block>> tags = new HashSet<>();
    private BlockPos ignorePos = null;

    public TreeFloodFill addRule(Rule rule) {
        this.rules.add(rule);
        return this;
    }

    public TreeFloodFill addTag(TagKey<Block> tag) {
        this.tags.add(tag);
        return this;
    }

    public TreeFloodFill ignore(BlockPos pos) {
        this.ignorePos = pos;
        return this;
    }

    public TreeResult findBlocks(BlockGetter blockGetter, BlockPos start) {
        if(!blockGetter.getBlockState(start).is(BlockTags.LOGS)) {
            return null;
        }

        TreeResult result = new TreeResult(this.tags);
        Queue<BlockPos> queue = new LinkedList<>();
        Set<Long> visited = new LongOpenHashSet();

        queue.add(start);

        while (!queue.isEmpty()) {
            BlockPos centerPos = queue.poll();
            BlockState centerState = blockGetter.getBlockState(centerPos);

            visited.add(centerPos.asLong());

            if(!this.shouldIgnore(centerPos)) {
                result.add(centerPos, centerState);
                result.afterSpread(blockGetter, centerPos, centerState);
            }

            for (BlockPos offset : TreeUtil.DIRECTION_OFFSETS_CORNERS) {
                BlockPos nextPos = centerPos.offset(offset);
                if(visited.contains(nextPos.asLong())) {
                    continue;
                }

                BlockState nextState = blockGetter.getBlockState(nextPos);
                if(nextState.isAir()) {
                    continue;
                }

                for (Rule rule : this.rules) {
                    if(this.shouldIgnore(nextPos)) {
                        continue;
                    }

                    if(rule.canSpread(centerPos, nextPos, centerState, nextState, result)) {
                        queue.add(nextPos);
                        visited.add(nextPos.asLong());
                        break;
                    }
                }
            }
        }

        return result;
    }

    private boolean shouldIgnore(BlockPos pos) {
        return this.ignorePos != null && this.ignorePos.equals(pos);
    }

}
