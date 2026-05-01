package com.farcr.treephysics.mixin.rooted_dirt_placement;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.BiConsumer;

@Mixin(TrunkPlacer.class)
public class TrunkPlacerMixin {

    @WrapMethod(method = "setDirtAt")
    private static void treephysics$setDirtAt(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> blockSetter, RandomSource random, BlockPos pos, TreeConfiguration config, Operation<Void> original) {
        LevelReader reader = (LevelReader) level;
        BlockState state = reader.getBlockState(pos);
        if(!state.onTreeGrow(reader, blockSetter, random, pos, config) && !state.is(Blocks.ROOTED_DIRT)) {
            blockSetter.accept(pos, Blocks.ROOTED_DIRT.defaultBlockState());
        }
    }

//    @WrapOperation(method = "setDirtAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider;getState(Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
//    private static BlockState treephysics$setDirtAt(BlockStateProvider instance, RandomSource randomSource, BlockPos blockPos, Operation<BlockState> original) {
//        return Blocks.ROOTED_DIRT.defaultBlockState();
//    }

}
