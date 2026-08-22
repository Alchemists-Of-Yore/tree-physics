package com.farcr.treephysics.mixin.entity_panic;

import com.farcr.treephysics.mixinterface.LivingEntityExtension;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements LivingEntityExtension {

    @Unique
    private long treephysics$panicTimestamp = 0;

    @Override
    public long treephysics$getPanicTimestamp() {
        return this.treephysics$panicTimestamp;
    }

    @Override
    public void treephysics$setPanicTimestamp(long panicTimestamp) {
        this.treephysics$panicTimestamp = panicTimestamp;
    }
}
