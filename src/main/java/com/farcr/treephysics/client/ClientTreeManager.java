package com.farcr.treephysics.client;

import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.util.SableDistUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClientTreeManager implements TreeManager {
    private final Map<ResourceKey<Level>, Set<UUID>> trees = new Object2ObjectOpenHashMap<>();

    @Override
    public boolean isTree(@Nullable SubLevel subLevel) {
        if(subLevel == null) return false;
        Set<UUID> trees = this.trees.get(subLevel.getLevel().dimension());
        return trees.contains(subLevel.getUniqueId());
    }

    @Override
    public Level getLevel() {
        return SableDistUtil.getClientLevel();
    }

    public void setTrees(ResourceKey<Level> dimension, Set<UUID> trees) {
        this.trees.put(dimension, trees);
    }
}
