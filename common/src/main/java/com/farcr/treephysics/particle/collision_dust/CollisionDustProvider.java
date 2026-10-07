package com.farcr.treephysics.particle.collision_dust;

import com.farcr.treephysics.index.TreePhysicsClientConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.Nullable;

public class CollisionDustProvider implements ParticleProvider<CollisionDustParticleOptions> {
    private final SpriteSet spriteSet;

    public CollisionDustProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }


    @Override
    public @Nullable Particle createParticle(CollisionDustParticleOptions options, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        if (!TreePhysicsClientConfig.COLLISION_DUST.get()) {
            return null;
        }
        return new CollisionDustParticle(level, x, y, z, this.spriteSet, options.state());
    }
}
