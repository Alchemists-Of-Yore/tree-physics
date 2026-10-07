package com.farcr.treephysics.index;

import net.neoforged.neoforge.common.ModConfigSpec;

import static com.farcr.treephysics.index.TreePhysicsConfig.create;

public class TreePhysicsClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue LEAF_VOLUME;
    public static final ModConfigSpec.BooleanValue COLLISION_DUST;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        LEAF_VOLUME = create(builder, "Leaf Volume", "How loud the leaf breaking sound should be on trees")
                .defineInRange("leaf_volume", 0.15, 0.0, 1.0);

        COLLISION_DUST = create(builder, "Collision Dust", "If dust particles should appear when trees hit the ground")
                .define("collision_dust", true);

        SPEC = builder.build();
    }
}
