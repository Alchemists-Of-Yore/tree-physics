package com.farcr.treephysics.index;

import net.neoforged.neoforge.common.ModConfigSpec;

public class TreePhysicsConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue DESPAWN_TIME;
    public static final ModConfigSpec.EnumValue<DespawnBehavior> DESPAWN_BEHAVIOR;
    public static final ModConfigSpec.BooleanValue DROP_ITEMS_ON_DESPAWN;
    public static final ModConfigSpec.BooleanValue REQUIRES_AXE;
    public static final ModConfigSpec.BooleanValue PREVENT_BUILDING_ON_TREES;
    public static final ModConfigSpec.BooleanValue CAN_WALK_THROUGH_LEAVES;
    public static final ModConfigSpec.DoubleValue LEAF_WALKING_SPEED;
    public static final ModConfigSpec.DoubleValue TREE_ENTITY_DAMAGE;

    public static final ModConfigSpec.DoubleValue GRAVITY_MULTIPLIER;
    public static final ModConfigSpec.IntValue GRAVITY_MULTIPLIER_TICKS;
    public static final ModConfigSpec.DoubleValue IMPULSE_FORCE;
    public static final ModConfigSpec.DoubleValue IMPULSE_TORQUE;
    public static final ModConfigSpec.DoubleValue EXTRA_PUSH_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue STATIC_LEAF_COLLISION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        DESPAWN_TIME = builder
                .comment("treephysics.config.despawn_time.tooltip")
                .translation("treephysics.config.despawn_time")
                .defineInRange("despawn_time", 144000, -1, Integer.MAX_VALUE);

        DESPAWN_BEHAVIOR = builder
                .comment("treephysics.config.despawn_behavior.tooltip")
                .translation("treephysics.config.despawn_behavior")
                .defineEnum("despawn_behavior", DespawnBehavior.DESPAWN_SMALL);

        DROP_ITEMS_ON_DESPAWN = builder
                .comment("treephysics.config.drop_items_on_despawn.tooltip")
                .translation("treephysics.config.drop_items_on_despawn")
                .define("drop_items_on_despawn", false);

        REQUIRES_AXE = builder
                .comment("treephysics.config.requires_axe.tooltip")
                .translation("treephysics.config.requires_axe")
                .define("requires_axe", false);

        PREVENT_BUILDING_ON_TREES = builder
                .comment("treephysics.config.prevent_building_on_trees.tooltip")
                .translation("treephysics.config.prevent_building_on_trees")
                .define("prevent_building_on_trees", true);

        CAN_WALK_THROUGH_LEAVES = builder
                .comment("treephysics.config.can_walk_through_leaves.tooltip")
                .translation("treephysics.config.can_walk_through_leaves")
                .define("can_walk_through_leaves", true);

        LEAF_WALKING_SPEED = builder
                .comment("treephysics.config.leaf_walking_speed.tooltip")
                .translation("treephysics.config.leaf_walking_speed")
                .defineInRange("leaf_walking_speed", 0.67, 0.0, 1.0);

        TREE_ENTITY_DAMAGE = builder
                .comment("treephysics.config.tree_entity_damage.tooltip")
                .translation("treephysics.config.tree_entity_damage")
                .defineInRange("tree_entity_damage", 25, 0.0, Double.MAX_VALUE);

        builder.translation("treephysics.config.section.physics").push("physics");

        GRAVITY_MULTIPLIER = builder
                .comment("treephysics.config.physics.gravity_multiplier.tooltip")
                .translation("treephysics.config.physics.gravity_multiplier")
                .defineInRange("gravity_multiplier", 1.0, 1.0, Double.MAX_VALUE);

        GRAVITY_MULTIPLIER_TICKS = builder
                .comment("treephysics.config.physics.gravity_multiplier_ticks.tooltip")
                .translation("treephysics.config.physics.gravity_multiplier_ticks")
                .defineInRange("gravity_multiplier_ticks", 400, -1, Integer.MAX_VALUE);

        IMPULSE_FORCE = builder
                .comment("treephysics.config.physics.impulse_force.tooltip")
                .translation("treephysics.config.physics.impulse_force")
                .defineInRange("impulse_force", 1.5, 0.0, Double.MAX_VALUE);

        IMPULSE_TORQUE = builder
                .comment("treephysics.config.physics.impulse_torque.tooltip")
                .translation("treephysics.config.physics.impulse_torque")
                .defineInRange("impulse_torque", 0.3, 0.0, Double.MAX_VALUE);

        EXTRA_PUSH_MULTIPLIER = builder
                .comment("treephysics.config.physics.extra_push_multiplier.tooltip")
                .translation("treephysics.config.physics.extra_push_multiplier")
                .defineInRange("extra_push_multiplier", 1.5, 0.0, Double.MAX_VALUE);

        STATIC_LEAF_COLLISION = builder
                .comment("treephysics.config.physics.static_leaf_collision.tooltip")
                .translation("treephysics.config.physics.static_leaf_collision")
                .define("static_leaf_collision", false);

        builder.pop();

        SPEC = builder.build();
    }

    public enum DespawnBehavior {
        NO_DESPAWN,
        DESPAWN_SMALL,
        DESPAWN_ALL
    }

}
