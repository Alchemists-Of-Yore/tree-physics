package com.farcr.treephysics.mixin.accessors;

import dev.ryanhcode.sable.util.LevelAccelerator;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(LevelAccelerator.class)
public interface LevelAcceleratorAccessor {
    @Accessor
    Level getLevel();
}
