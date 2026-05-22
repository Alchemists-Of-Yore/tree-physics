package com.farcr.treephysics.index;

import com.farcr.treephysics.TreePhysics;
import com.farcr.treephysics.data.TreePhysicsLang;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Locale;

public class TreePhysicsConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue DESPAWN_TIME;
    public static final ModConfigSpec.EnumValue<DespawnBehavior> DESPAWN_BEHAVIOR;
    public static final ModConfigSpec.BooleanValue DROP_ITEMS_ON_DESPAWN;
    public static final ModConfigSpec.BooleanValue ROOTED_DIRT_GENERATION;
    public static final ModConfigSpec.BooleanValue ROOTLESS_TREE_DETECTION;
    public static final ModConfigSpec.BooleanValue REQUIRES_AXE;
    public static final ModConfigSpec.BooleanValue PREVENT_INTERACTING_WITH_TREES;
    public static final ModConfigSpec.EnumValue<LeafWalkingBehavior> LEAF_WALKING_BEHAVIOR;
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

        DESPAWN_TIME = create(builder, "Despawn Time", "The amount of time in ticks a tree will exist before despawning. -1 to despawn when the tree stops moving")
                .defineInRange("despawn_time", 144000, -1, Integer.MAX_VALUE);

        DESPAWN_BEHAVIOR = create(builder, "Despawn Behavior", """
                NO_DESPAWN: Trees will not despawn at all
                DESPAWN_SMALL: Trees with 5 or less logs will despawn
                DESPAWN_ALL: Every tree will despawn
                """)
                .defineEnum("despawn_behavior", DespawnBehavior.DESPAWN_SMALL);

        DROP_ITEMS_ON_DESPAWN = create(builder, "Drop Items on Despawn", "If trees should drop their items when they despawn, when off they just get removed")
                .define("drop_items_on_despawn", false);

        ROOTED_DIRT_GENERATION = create(builder, "Rooted Dirt Generation", "If rooted dirt should generate under trees")
                .define("rooted_dirt_generation", true);

        ROOTLESS_TREE_DETECTION = create(builder, "Rootless Tree Detection", "Allow tree sub-levels to be created without a root block, requiring at least one dirt and one natural leaf instead")
                .define("rootless_tree_detection", false);

        REQUIRES_AXE = create(builder, "Requires Axe", "If an axe should be required to make tree sub-levels")
                .define("requires_axe", false);

        PREVENT_INTERACTING_WITH_TREES = create(builder, "Prevent Interacting With Trees", "If interacting with trees (block placement, item use, etc) should be prevented")
                .define("prevent_interacting_with_trees", false);

        LEAF_WALKING_BEHAVIOR = create(builder, "Leaf Walking Behavior", """
                NEVER: Entities cannot walk through leaves
                ALWAYS: Entities can walk through all leaves
                IN_SUB_LEVELS: Entities can only walk through leaves on sub-levels
                IN_WORLD: Entities can only walk through leaves in the world
                This is unrelated to leaves in world having no physics collision, see "Static Leaf Collision" instead
                """)
                .defineEnum("leaf_walking_behavior", LeafWalkingBehavior.ALWAYS);

        LEAF_WALKING_SPEED = create(builder, "Leaf Walking Speed", "Multiplier for entity walking speed when in leaves")
                .defineInRange("leaf_walking_speed", 0.67, 0.0, 1.0);

        TREE_ENTITY_DAMAGE = create(builder, "Tree Entity Damage", "How much damage a falling tree should inflict on an entity when moving at 1 block per tick")
                .defineInRange("tree_entity_damage", 25, 0.0, Double.MAX_VALUE);

        builder.translation("treephysics.config.section.physics").push("physics");

        GRAVITY_MULTIPLIER = create(builder, "Gravity Multiplier", "How much extra gravity should be applied to trees")
                .defineInRange("gravity_multiplier", 1.0, 1.0, Double.MAX_VALUE);

        GRAVITY_MULTIPLIER_TICKS = create(builder, "Gravity Multiplier Ticks", "How long in ticks the gravity multiplier should be applied. -1 for infinite")
                .defineInRange("gravity_multiplier_ticks", 400, -1, Integer.MAX_VALUE);

        IMPULSE_FORCE = create(builder, "Impulse Force", "How much force should be applied to trees when chopped down")
                .defineInRange("impulse_force", 1.5, 0.0, Double.MAX_VALUE);

        IMPULSE_TORQUE = create(builder, "Impulse Torque", "How much torque should be applied to trees when chopped down")
                .defineInRange("impulse_torque", 0.3, 0.0, Double.MAX_VALUE);

        EXTRA_PUSH_MULTIPLIER = create(builder, "Extra Push Multiplier", "How much extra pushing strength should be applied for upright trees")
                .defineInRange("extra_push_multiplier", 1.5, 0.0, Double.MAX_VALUE);

        STATIC_LEAF_COLLISION = create(builder, "Static Leaf Collision", """
                If leaves in world should have physics collision.
                This is unrelated to entities being able to walk through leaves, see "Leaf Walking Behavior" instead
                """)
                .worldRestart()
                .define("static_leaf_collision", false);

        builder.pop();

        SPEC = builder.build();
    }

    public enum LeafWalkingBehavior {
        NEVER,
        ALWAYS,
        IN_SUB_LEVELS,
        IN_WORLD;

        public boolean allowSubLevel() {
            return this == ALWAYS || this == IN_SUB_LEVELS;
        }

        public boolean allowWorld() {
            return this == ALWAYS || this == IN_WORLD;
        }
    }

    public enum DespawnBehavior {
        NO_DESPAWN,
        DESPAWN_SMALL,
        DESPAWN_ALL
    }

    public static ModConfigSpec.Builder create(ModConfigSpec.Builder builder, String name, String comment) {
        String id = name.toLowerCase(Locale.ROOT).replace(" ", "_");
        String nameKey = TreePhysics.MOD_ID + ".config." + id;
        String commentKey = nameKey + ".tooltip";
        TreePhysicsLang.LANG.put(nameKey, name);
        TreePhysicsLang.LANG.put(commentKey, comment);
        return builder.comment(commentKey).translation(nameKey);
    }

}
