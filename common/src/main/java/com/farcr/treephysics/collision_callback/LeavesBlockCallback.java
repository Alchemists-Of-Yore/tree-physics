package com.farcr.treephysics.collision_callback;

import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LeavesBlockCallback extends FragileBlockCallback {
    public static final LeavesBlockCallback INSTANCE = new LeavesBlockCallback();

    @Override
    public double getTriggerVelocity() {
        return 0.0;
    }

    @Override
    public boolean shouldTriggerFor(BlockState state) {
        return state.getBlock() instanceof LeavesBlock;
    }

}
