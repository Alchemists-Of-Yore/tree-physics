package com.farcr.treephysics.mixin.tree_pushing;

import com.farcr.treephysics.api.util.TreeUtil;
import com.farcr.treephysics.client.TreeManager;
import com.farcr.treephysics.index.TreePhysicsConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.network.packets.tcp.ServerboundPunchSubLevelPacket;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerboundPunchSubLevelPacket.class)
public class ServerboundPunchSubLevelPacketMixin {

    @WrapOperation(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeInstance;getValue()D"))
    private double treephysics$getValue(AttributeInstance instance, Operation<Double> original, @Local(name = "targetSubLevel") SubLevel targetSubLevel, @Local(name = "level") ServerLevel level) {
        double value = original.call(instance);
        TreeManager manager = TreeManager.get(level);
        if(manager.isTree(targetSubLevel)) {
            value += TreePhysicsConfig.EXTRA_PUSH_MULTIPLIER.getAsDouble() * TreeUtil.getUprightness(targetSubLevel);
        }
        return value;
    }

}
