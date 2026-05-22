package com.farcr.treephysics.mixin.leaf_collision;

import com.farcr.treephysics.index.TreePhysicsConfig;
import com.farcr.treephysics.mixin.accessors.LevelAcceleratorAccessor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.physics.chunk.VoxelNeighborhoodState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(VoxelNeighborhoodState.class)
public class VoxelNeighborhoodStateMixin {

    @WrapOperation(method = "getState", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/physics/chunk/VoxelNeighborhoodState;isSolid(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private static boolean treephysics$isSolid(BlockGetter blockGetter, BlockPos pos, BlockState state, Operation<Boolean> original) {
        if(state.getBlock() instanceof LeavesBlock && !TreePhysicsConfig.STATIC_LEAF_COLLISION.get() && blockGetter instanceof LevelAcceleratorAccessor accessor) {
            return Sable.HELPER.getContaining(accessor.getLevel(), pos) != null;
        }
        return original.call(blockGetter, pos, state);
    }

}
