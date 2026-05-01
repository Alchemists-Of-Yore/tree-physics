package com.farcr.treephysics.mixin.collision_effects;

import com.farcr.treephysics.collision_callback.LogCallback;
import dev.ryanhcode.sable.api.block.BlockWithSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockWithSubLevelCollisionCallback.class)
public interface BlockWithSubLevelCallbackMixin {
    @Inject(method = "sable$getCallback(Lnet/minecraft/world/level/block/state/BlockState;)Ldev/ryanhcode/sable/api/physics/callback/BlockSubLevelCollisionCallback;", at = @At("HEAD"), cancellable = true)
    private static void treephysics$getCallback(BlockState state, CallbackInfoReturnable<BlockSubLevelCollisionCallback> cir) {
        if(state.is(BlockTags.LOGS)) {
            cir.setReturnValue(LogCallback.INSTANCE);
        }
    }
}
