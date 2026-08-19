package com.farcr.treephysics.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class TreePhysicsLang {
    public static final Map<String, String> LANG = new HashMap<>();

    public static void provideLang(BiConsumer<String, String> consumer) {
        consumer.accept("death.attack.tree", "%s was crushed by a tree");
        consumer.accept("commands.treephysics.untree.success", "Unset %s tree%s");
        consumer.accept("treephysics.subtitles.sub_level.tree.creak", "Tree creaks");
        consumer.accept("treephysics.subtitles.sub_level.tree.impact", "Tree crashes");
        consumer.accept("treephysics.subtitles.sub_level.tree.leaf_rustle", "Leaves rustle");

        consumer.accept("treephysics.config.title", "Tree Physics Config");
        consumer.accept("treephysics.config.section.physics", "Physics");
        consumer.accept("treephysics.config.section.compatibility", "Mod Compatibility");

        LANG.forEach(consumer);
    }
}
