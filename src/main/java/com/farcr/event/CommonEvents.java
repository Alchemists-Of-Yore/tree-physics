package com.farcr.event;

import com.farcr.TreePhysics;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;

@EventBusSubscriber(modid = TreePhysics.MOD_ID)
public class CommonEvents {

    private static final BlockPos[] DIRECTION_OFFSETS = new BlockPos[] {
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

    @SubscribeEvent
    public static void blockBreak(BlockEvent.BreakEvent event) {
        Collection<BlockPos> tree = gatherTree(event.getLevel(), event.getPos());
        if(tree.isEmpty()) return;

        SubLevelAssemblyHelper.assembleBlocks((ServerLevel) event.getLevel(), event.getPos(), tree, new BoundingBox3i());
    }


    private static Collection<BlockPos> gatherTree(BlockGetter blockGetter, BlockPos root) {
        Set<BlockPos> result = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        Set<Long> visited = new LongOpenHashSet();

        queue.add(root);

        int count = 0;
        while (!queue.isEmpty()) {
            if(count >= 1000) {
                System.out.println(":(");
                break;
            }

            BlockPos centerPos = queue.poll();
            long centerLong = centerPos.asLong();
            visited.add(centerLong);

            BlockState centerState = blockGetter.getBlockState(centerPos);
            boolean centerLeaf = centerState.is(BlockTags.LEAVES);
            result.add(centerPos);

            for (BlockPos offset : DIRECTION_OFFSETS) {
                BlockPos nextPos = centerPos.offset(offset);
                long nextLong = nextPos.asLong();
                if(!visited.contains(nextLong)) {
                    BlockState nextState = blockGetter.getBlockState(nextPos);
                    boolean nextLeaf = nextState.is(BlockTags.LEAVES);

                    boolean canSpread = nextState.is(BlockTags.LOGS) || nextLeaf;

                    if(centerLeaf && nextLeaf) {
                        int centerDist = centerState.getValue(LeavesBlock.DISTANCE);
                        int nextDist = nextState.getValue(LeavesBlock.DISTANCE);
                        canSpread = centerDist <= nextDist;
                    } else {
                        visited.add(nextLong);
                    }

                    if(canSpread) {
                        queue.add(nextPos);
                    }
                }
            }

            count++;
        }

        result.remove(root);

        return result;
    }
}
