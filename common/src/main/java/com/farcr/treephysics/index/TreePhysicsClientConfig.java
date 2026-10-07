package com.farcr.treephysics.index;

import net.neoforged.neoforge.common.ModConfigSpec;

import static com.farcr.treephysics.index.TreePhysicsConfig.create;

public class TreePhysicsClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue LEAF_VOLUME;
    public static final ModConfigSpec.BooleanValue COLLISION_PARTICLES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        LEAF_VOLUME = create(builder, "Leaf Volume", "How loud the leaf breaking sound should be on trees")
                .defineInRange("leaf_volume", 0.15, 0.0, 1.0);

        COLLISION_PARTICLES = create(builder, "Collision Particles", "If trees should spawn dust and block particles when colliding with terrain. (Some block particles may still appear, as that is an unrelated feature in Sable)")
                .define("collision_particles", true);

        SPEC = builder.build();
    }
}
