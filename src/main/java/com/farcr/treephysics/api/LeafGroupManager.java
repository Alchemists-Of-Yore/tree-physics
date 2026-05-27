package com.farcr.treephysics.api;

import com.farcr.treephysics.TreePhysics;
import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LeafGroupManager extends SimplePreparableReloadListener<Map<ResourceLocation, Set<ResourceLocation>>> {
    public static final LeafGroupManager INSTANCE = new LeafGroupManager();
    public static final Map<Block, Set<Block>> GROUPS = new Object2ObjectOpenHashMap<>();

    private static final String PATH = TreePhysics.MOD_ID + "/leaf_grouping.json";

    @Override
    protected Map<ResourceLocation, Set<ResourceLocation>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Set<ResourceLocation>> map = new Object2ObjectOpenHashMap<>();
        Map<String, Set<ResourceLocation>> groupMap = new Object2ObjectOpenHashMap<>();

        for (String modId : resourceManager.getNamespaces()) {
            for (Resource resource : resourceManager.getResourceStack(ResourceLocation.fromNamespaceAndPath(modId, PATH))) {
                try (Reader reader = resource.openAsReader()) {
                    JsonElement element = JsonParser.parseReader(reader);
                    if(element.isJsonObject() && element.getAsJsonObject().has("groups") && element.getAsJsonObject().get("groups").isJsonObject()) {
                        JsonObject groups = element.getAsJsonObject().get("groups").getAsJsonObject();

                        for (String key : groups.keySet()) {
                            JsonElement list = groups.get(key);
                            DataResult<List<ResourceLocation>> result = ResourceLocation.CODEC.listOf().parse(JsonOps.INSTANCE, list);
                            if(result.isSuccess()) {
                                groupMap.computeIfAbsent(key, k -> new ObjectOpenHashSet<>())
                                        .addAll(result.getOrThrow());
                            }
                        }

                    }
                } catch (IOException | JsonParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (Set<ResourceLocation> group : groupMap.values()) {
            for (ResourceLocation groupMember : group) {
                Set<ResourceLocation> set = map.computeIfAbsent(groupMember, rl -> new ObjectOpenHashSet<>());
                set.addAll(group.stream().filter(v -> v != groupMember).toList());
            }
        }

        return map;
    }

    @Override
    protected void apply(Map<ResourceLocation, Set<ResourceLocation>> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        GROUPS.clear();

        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : map.entrySet()) {
            ResourceLocation key = entry.getKey();
            Set<ResourceLocation> value = entry.getValue();

            Block keyBlock = BuiltInRegistries.BLOCK.get(key);
            if(keyBlock == Blocks.AIR) {
                continue;
            }

            for (ResourceLocation id : value) {
                Block valueBlock = BuiltInRegistries.BLOCK.get(id);
                if(valueBlock == Blocks.AIR) {
                    continue;
                }

                Set<Block> keySet = GROUPS.computeIfAbsent(keyBlock, b -> new ObjectOpenHashSet<>());
                Set<Block> valueSet = GROUPS.computeIfAbsent(valueBlock, b -> new ObjectOpenHashSet<>());
                keySet.add(valueBlock);
                valueSet.add(keyBlock);
            }
        }
    }
}
