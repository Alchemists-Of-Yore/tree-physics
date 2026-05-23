package com.farcr.treephysics.mixin.rooted_dirt_placement;

import com.farcr.treephysics.api.util.TreeUtil;
import com.farcr.treephysics.index.TreePhysicsConfig;
import com.farcr.treephysics.index.TreePhysicsTags;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.BiConsumer;

@Mixin(TreeFeature.class)
public class TreeFeatureMixin {

    @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/TreeFeature;doPlace(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;Ljava/util/function/BiConsumer;Ljava/util/function/BiConsumer;Lnet/minecraft/world/level/levelgen/feature/foliageplacers/FoliagePlacer$FoliageSetter;Lnet/minecraft/world/level/levelgen/feature/configurations/TreeConfiguration;)Z"))
    private boolean treephysics$doPlace(TreeFeature instance, WorldGenLevel level, RandomSource random, BlockPos pos, BiConsumer<BlockPos, BlockState> rootBlockSetter, BiConsumer<BlockPos, BlockState> trunkBlockSetter, FoliagePlacer.FoliageSetter foliageBlockSetter, TreeConfiguration config, Operation<Boolean> original) {
        if(!TreePhysicsConfig.ROOTED_DIRT_GENERATION.getAsBoolean()) {
            return original.call(instance, level, random, pos, rootBlockSetter, rootBlockSetter, foliageBlockSetter, config);
        }

        BiConsumer<BlockPos, BlockState> pawesomeTrunkBlockSetter = (blockPos, state) -> {
            trunkBlockSetter.accept(blockPos, state);
            if(TreeUtil.getLogAxis(state) == Direction.Axis.Y) {
                if (level.getBlockState(blockPos.below()).is(TreePhysicsTags.CAN_BE_ROOTS)) {
                    level.setBlock(blockPos.below(), Blocks.ROOTED_DIRT.defaultBlockState(), 19);
                }
            }
        };
        return original.call(instance, level, random, pos, rootBlockSetter, pawesomeTrunkBlockSetter, foliageBlockSetter, config);
    }

}
