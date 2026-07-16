package com.farcr.treephysics.mixin.evil_patch;

import com.farcr.treephysics.api.manager.TreeManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerSubLevel.class)
public abstract class ServerSubLevelMixin {

    @Shadow
    public abstract ServerLevel getLevel();

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/ModConfigSpec$BooleanValue;getAsBoolean()Z"))
    private boolean treephysics$getAsBoolean(ModConfigSpec.BooleanValue instance, Operation<Boolean> original) {
        ServerSubLevel self = (ServerSubLevel) (Object) this;
        return original.call(instance) && !TreeManager.get(this.getLevel()).isTree(self);
    }
}
