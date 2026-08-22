package com.farcr.treephysics.mixin.entity_panic;

import com.farcr.treephysics.mixinterface.LivingEntityExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PanicGoal.class)
public class PanicGoalMixin {

    @Shadow
    @Final
    protected PathfinderMob mob;

    @WrapOperation(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/PanicGoal;shouldPanic()Z"))
    private boolean treephysics$shouldPanic(PanicGoal instance, Operation<Boolean> original) {
        boolean shouldPanic = original.call(instance);
        if(!shouldPanic && this.mob instanceof LivingEntityExtension extension) {
            shouldPanic = (this.mob.level().getGameTime() - extension.treephysics$getPanicTimestamp()) < 40L;
        }
        return shouldPanic;
    }

}
