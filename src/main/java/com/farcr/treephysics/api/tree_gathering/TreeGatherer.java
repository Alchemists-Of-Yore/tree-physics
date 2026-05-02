package com.farcr.treephysics.api.tree_gathering;

import com.farcr.treephysics.api.TreeUtil;
import com.farcr.treephysics.api.manager.ServerTreeManager;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TreeGatherer {
    // TODO: make this a config
    private static final int MAX_ITERATIONS = 100000;

    public static @Nullable List<ServerSubLevel> trySplit(ServerLevel level, BlockPos brokenPos) {
        if(!isValidTree(level, brokenPos)) {
            return null;
        }

        List<ServerSubLevel> subLevels = new ArrayList<>();
        ServerTreeManager handler = ServerTreeManager.get(level);

        for (BlockPos offset : TreeUtil.DIRECTION_OFFSETS_CORNERS) {
            BlockPos start = brokenPos.offset(offset);

            Tree tree = gatherTree(level, start, TreeUtil::treeSpread, TreeUtil.DIRECTION_OFFSETS_CORNERS, brokenPos);
            if(tree != null && !tree.hasRoot()) {
                ServerSubLevel serverSubLevel = SubLevelAssemblyHelper.assembleBlocks(level, brokenPos, tree.blocks(), new BoundingBox3i(brokenPos, brokenPos));
                subLevels.add(serverSubLevel);
                handler.setTree(serverSubLevel);
            }
        }

        return subLevels;
    }

    public static boolean isValidTree(BlockGetter blockGetter, BlockPos start) {
        BlockState state = blockGetter.getBlockState(start);
        if(!state.is(BlockTags.LOGS)) return false;

        Tree tree = gatherTree(blockGetter, start, TreeUtil::logSpread, TreeUtil.DIRECTION_OFFSETS_CORNERS, null);
        if(tree != null) {
            return tree.hasRoot();
        }

        return false;
    }

    public static @Nullable Tree gatherTree(BlockGetter blockGetter, BlockPos root, SpreadPredicate predicate, BlockPos[] offsets, @Nullable BlockPos ignore) {
        BlockState rootState = blockGetter.getBlockState(root);
        TreeContext context = new TreeContext();
        if(!context.isLog(rootState)) {
            return null;
        }

        boolean hasRoots = false;
        Set<Long> visited = new LongOpenHashSet();
        Set<BlockPos> result = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();

        queue.add(root);

        int count = 0;
        while (!queue.isEmpty() && count < MAX_ITERATIONS) {
            BlockPos centerPos = queue.poll();
            BlockState centerState = blockGetter.getBlockState(centerPos);
            visited.add(centerPos.asLong());
            result.add(centerPos);

            if(!hasRoots && blockGetter.getBlockState(centerPos.below()).is(Blocks.ROOTED_DIRT)) {
                hasRoots = true;
            }

            for (BlockPos offset : offsets) {
                BlockPos nextPos = centerPos.offset(offset);
                long nextLong = nextPos.asLong();
                if(visited.contains(nextLong)) {
                    continue;
                }

                BlockState nextState = blockGetter.getBlockState(nextPos);
                if(nextState.isAir()) {
                    visited.add(nextLong);
                    continue;
                }

                if(!nextPos.equals(ignore)) {
                    if(predicate.test(centerState, nextState, context)) {
                        visited.add(nextLong);
                        queue.add(nextPos);
                    }
                }
            }

            count++;
        }

        return new Tree(result, hasRoots);
    }

}
