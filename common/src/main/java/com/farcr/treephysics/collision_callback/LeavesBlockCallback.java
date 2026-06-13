package com.farcr.treephysics.collision_callback;

import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

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

    @Override
    public CollisionResult sable$onCollision(BlockPos pos, @Nullable BlockPos otherHitBlockPos, Vector3d pos1, double impactVelocity) {
        final double triggerVelocity = this.getTriggerVelocity();

        if (impactVelocity * impactVelocity < triggerVelocity * triggerVelocity) {
            return CollisionResult.NONE;
        }

        final SubLevelPhysicsSystem system = SubLevelPhysicsSystem.getCurrentlySteppingSystem();
        final ServerLevel level = system.getLevel();

        // Double check that we're actually fragile before breaking (in-case pipeline gave us a slightly off collision position)
        final BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof LeavesBlock && state.getValue(LeavesBlock.PERSISTENT))
            return CollisionResult.NONE;

        if (this.shouldTriggerFor(state)) {
            return this.onHit(level, pos, state, pos1);
        }

        return new CollisionResult(JOMLConversion.ZERO, true);
    }
}
