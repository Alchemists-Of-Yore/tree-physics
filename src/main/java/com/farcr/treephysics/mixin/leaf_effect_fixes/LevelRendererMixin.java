package com.farcr.treephysics.mixin.leaf_effect_fixes;

import com.farcr.treephysics.client.TreeManager;
import com.farcr.treephysics.index.TreePhysicsClientConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Shadow
    @Nullable
    private ClientLevel level;

    @WrapOperation(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V", ordinal = 41))
    private void treephysics$playLocalSound(ClientLevel instance, BlockPos pos, SoundEvent soundEvent, SoundSource soundSource, float volume, float pitch, boolean b, Operation<Void> original, @Local(name = "blockstate1") BlockState blockstate1) {
        TreeManager manager = TreeManager.get(this.level);
        if(manager.isTree(pos)) {
            volume *= (float) TreePhysicsClientConfig.LEAF_VOLUME.getAsDouble();
        }
        original.call(instance, pos, soundEvent, soundSource, volume, pitch, b);
    }

//    @WrapOperation(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V", ordinal = 0))
//    private void treephysics$getVolume(ClientLevel instance, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay, Operation<Void> original, @Local(argsOnly = true) BlockPos pos, @Local(name = "blockstate1") BlockState blockstate1) {
//    }
//    @WrapOperation(method = "levelEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/SoundType;getVolume()F"))
//    private float treephysics$getVolume(SoundType instance, Operation<Float> original, @Local(argsOnly = true) BlockPos pos, @Local(name = "blockstate1") BlockState blockstate1) {
//        float volume = original.call(instance);
//        if(blockstate1.is(BlockTags.LEAVES)) {
//            System.out.println("LEAVEES!");
//            TreeManager manager = TreeManager.get(this.level);
//            if(manager.isTree(pos)) {
//                System.out.println("ON A TREE!");
//                volume *= (float) TreePhysicsClientConfig.LEAF_VOLUME.getAsDouble();
//                System.out.println(volume);
//            }
//        }
//
//        return volume;
//    }


}
