package com.farcr.treephysics.api.flood_fill;

import com.farcr.treephysics.index.TreePhysicsConfig;
import com.farcr.treephysics.index.TreePhysicsTags;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TreeResult {
    private final Map<TagKey<Block>, Set<BlockPos>> collectedBlocks = new Object2ObjectOpenHashMap<>();
    private final Set<BlockPos> allBlocks = new ObjectOpenHashSet<>();
    private boolean root = false;
    private boolean leaves = false;
    private Block logBlock = null;

    public TreeResult(Collection<TagKey<Block>> tags) {
        for (TagKey<Block> tag : tags) {
            collectedBlocks.put(tag, new ObjectOpenHashSet<>());
        }
    }

    public void add(BlockPos pos, BlockState state) {
        for (TagKey<Block> key : this.collectedBlocks.keySet()) {
            if(state.is(key)) {
                this.collectedBlocks.get(key).add(pos);
                break;
            }
        }
        this.allBlocks.add(pos);
    }

    public void afterSpread(BlockGetter blockGetter, BlockPos pos, BlockState state) {
        if(!this.root && state.is(BlockTags.LOGS)) {
            BlockState belowState = blockGetter.getBlockState(pos.below());
            this.root = TreePhysicsConfig.ROOTLESS_TREE_DETECTION.getAsBoolean() ? belowState.is(BlockTags.DIRT) : belowState.is(TreePhysicsTags.ROOTS);
        }
        if(!this.leaves && state.getBlock() instanceof LeavesBlock) {
            this.leaves = !state.getValue(LeavesBlock.PERSISTENT);
        }
    }

    public Set<BlockPos> getBlocks(TagKey<Block> tag) {
        return this.collectedBlocks.get(tag);
    }

    public Set<BlockPos> getBlocks() {
        return this.allBlocks;
    }

    public boolean hasRoot() {
        return root;
    }

    public boolean hasLeaves() {
        return leaves;
    }

    public boolean isLeaf(BlockState state) {
        return state.is(BlockTags.LEAVES);
    }

    public boolean isLog(BlockState state) {
        if (this.logBlock != null) {
            return state.is(this.logBlock);
        } else {
            if (state.is(BlockTags.LOGS)) {
                this.logBlock = state.getBlock();
                return true;
            }
            return false;
        }
    }
}
