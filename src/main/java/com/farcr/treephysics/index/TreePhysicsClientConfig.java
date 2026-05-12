package com.farcr.treephysics.index;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TreePhysicsClientConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.DoubleValue LEAF_VOLUME;


    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        LEAF_VOLUME = builder
                .comment("How loud the leaf breaking sound should be on trees")
                .defineInRange("leaf_volume", 0.15, 0.0, 1.0);

        SPEC = builder.build();
    }
}
