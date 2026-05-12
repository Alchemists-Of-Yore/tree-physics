package com.farcr.treephysics.mixin.collision_effects;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.physics.callback.BeehiveBlockCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeehiveBlockCallback.class)
public class BeehiveBlockCallbackMixin {

    @WrapMethod(method = "getTriggerVelocity")
    private double treephysics$getTriggerVelocity(Operation<Double> original) {
        return 4.0;
    }

    @WrapOperation(method = "onHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BeehiveBlockEntity;emptyAllLivingFromHive(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BeehiveBlockEntity$BeeReleaseStatus;)V"))
    private void treephysics$onHit(BeehiveBlockEntity instance, Player player, BlockState state, BeehiveBlockEntity.BeeReleaseStatus releaseStatus, Operation<Void> original, @Local(argsOnly = true) ServerLevel level) {
        level.destroyBlock(instance.getBlockPos(), true);
        original.call(instance, player, state, releaseStatus);
    }
}
