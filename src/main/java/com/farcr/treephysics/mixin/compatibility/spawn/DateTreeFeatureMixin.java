package com.farcr.treephysics.mixin.compatibility.spawn;

import com.farcr.treephysics.api.util.TreeUtil;
import com.farcr.treephysics.index.TreePhysicsTags;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.ninni.spawn.server.level.feature.DateTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DateTreeFeature.class)
public class DateTreeFeatureMixin {

    @WrapOperation(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean treephysics$setBlock(WorldGenLevel instance, BlockPos pos, BlockState state, int i, Operation<Boolean> original) {
        if(TreeUtil.isLog(state) && instance.getBlockState(pos.below()).is(TreePhysicsTags.CAN_BE_ROOTS)) {
            instance.setBlock(pos.below(), Blocks.ROOTED_DIRT.defaultBlockState(), i);
        }

        return original.call(instance, pos, state, i);
    }

}
