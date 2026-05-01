package com.farcr.treephysics.mixin.collision_effects;

import com.farcr.treephysics.collision_callback.LeavesBlockCallback;
import dev.ryanhcode.sable.api.block.BlockWithSubLevelCollisionCallback;
import dev.ryanhcode.sable.api.physics.callback.BlockSubLevelCollisionCallback;
import net.minecraft.world.level.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = LeavesBlock.class, priority = 2000)
public class LeavesBlockMixin implements BlockWithSubLevelCollisionCallback {
    @Override
    public BlockSubLevelCollisionCallback sable$getCallback() {
        return LeavesBlockCallback.INSTANCE;
    }
}
