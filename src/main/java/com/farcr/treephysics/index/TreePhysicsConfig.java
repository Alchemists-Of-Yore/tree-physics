package com.farcr.treephysics.index;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TreePhysicsConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue DESPAWN_TIME;
    public static final ModConfigSpec.EnumValue<DespawnBehavior> DESPAWN_BEHAVIOR;
    public static final ModConfigSpec.BooleanValue CAN_BUILD;

    public static final ModConfigSpec.DoubleValue GRAVITY_MULTIPLIER;
    public static final ModConfigSpec.IntValue GRAVITY_MULTIPLIER_TICKS;
    public static final ModConfigSpec.DoubleValue IMPULSE_FORCE;
    public static final ModConfigSpec.DoubleValue IMPULSE_TORQUE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        DESPAWN_TIME = builder
                .comment("The amount of time in ticks a tree will exist before despawning")
                .defineInRange("despawn_time", 144000, 0, Integer.MAX_VALUE);

        DESPAWN_BEHAVIOR = builder
                .comment(
                        "NO_DESPAWN: Trees will not despawn at all",
                        "DESPAWN_SMALL: Trees with 5 or less logs will despawn",
                        "DESPAWN_ALL: Every tree will despawn"
                )
                .defineEnum("despawn_behavior", DespawnBehavior.DESPAWN_SMALL);

        CAN_BUILD = builder
                .comment("If trees can be built on")
                .define("can_build", false);

        builder.push("physics");

        GRAVITY_MULTIPLIER = builder
                .comment("How much extra gravity should be applied to trees")
                .defineInRange("gravity_multiplier", 2.0, 1.0, Double.MAX_VALUE);

        GRAVITY_MULTIPLIER_TICKS = builder
                .comment("How long in ticks the gravity multiplier should be applied. -1 for infinite")
                .defineInRange("gravity_multiplier_ticks", 400, -1, Integer.MAX_VALUE);

        IMPULSE_FORCE = builder
                .comment("How much force should be applied to trees when chopped down")
                .defineInRange("impulse_force", 1.0, 0.0, Double.MAX_VALUE);

        IMPULSE_TORQUE = builder
                .comment("How much torque should be applied to trees when chopped down")
                .defineInRange("impulse_torque", 0.3, 0.0, Double.MAX_VALUE);

        builder.pop();

        SPEC = builder.build();
    }

    public enum DespawnBehavior {
        NO_DESPAWN,
        DESPAWN_SMALL,
        DESPAWN_ALL
    }

}
