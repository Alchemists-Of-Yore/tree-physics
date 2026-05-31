package com.farcr.treephysics.api.grouping;

import com.farcr.treephysics.TreePhysics;
import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class BlockGroupingManager extends SimplePreparableReloadListener<Map<ResourceLocation, Map<ResourceLocation, Set<ResourceLocation>>>> {
    public static final BlockGroupingManager INSTANCE = new BlockGroupingManager();
    public static final FileToIdConverter GROUPING_LISTER = FileToIdConverter.json(TreePhysics.MOD_ID + "/grouping");
    public static final Map<ResourceLocation, BlockGrouping> GROUPS_MAP = new Object2ObjectOpenHashMap<>();

    @Override
    protected Map<ResourceLocation, Map<ResourceLocation, Set<ResourceLocation>>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, Map<String, Set<ResourceLocation>>> map = new Object2ObjectOpenHashMap<>();

        Map<ResourceLocation, List<Resource>> resourceStacks = GROUPING_LISTER.listMatchingResourceStacks(resourceManager);
        for (Map.Entry<ResourceLocation, List<Resource>> entry : resourceStacks.entrySet()) {

            ResourceLocation id = GROUPING_LISTER.fileToId(entry.getKey());
            Map<String, Set<ResourceLocation>> grpupMap = map.computeIfAbsent(id, rl -> new Object2ObjectOpenHashMap<>());

            for (Resource resource : entry.getValue()) {
                try (Reader reader = resource.openAsReader()) {

                    JsonElement element = JsonParser.parseReader(reader);
                    if(element instanceof JsonObject object && object.has("groups") && object.get("groups") instanceof JsonObject groupsObject) {

                        for (String groupId : groupsObject.keySet()) {
                            DataResult<List<ResourceLocation>> result = ResourceLocation.CODEC.listOf().parse(JsonOps.INSTANCE, groupsObject.get(groupId));
                            if(result.isSuccess()) {
                                grpupMap.computeIfAbsent(groupId, s -> new ObjectOpenHashSet<>())
                                        .addAll(result.getOrThrow());
                            }

                        }

                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        Map<ResourceLocation, Map<ResourceLocation, Set<ResourceLocation>>> finalMap = new Object2ObjectOpenHashMap<>();

        for (ResourceLocation id : map.keySet()) {
            Map<ResourceLocation, Set<ResourceLocation>> groupMap = new Object2ObjectOpenHashMap<>();
            Map<String, Set<ResourceLocation>> stringMap = map.get(id);

            for (Set<ResourceLocation> values : stringMap.values()) {
                for (ResourceLocation value : values) {
                    Set<ResourceLocation> set = groupMap.computeIfAbsent(value, rl -> new ObjectOpenHashSet<>());
                    set.addAll(values.stream().filter(v -> v != value).toList());
                }
            }

            finalMap.put(id, groupMap);
        }

        return finalMap;
    }

    @Override
    protected void apply(Map<ResourceLocation, Map<ResourceLocation, Set<ResourceLocation>>> groups, ResourceManager resourceManager, ProfilerFiller profiler) {
        GROUPS_MAP.clear();

        for (Map.Entry<ResourceLocation, Map<ResourceLocation, Set<ResourceLocation>>> entry : groups.entrySet()) {
            ResourceLocation id = entry.getKey();
            Map<ResourceLocation, Set<ResourceLocation>> groupMap = entry.getValue();
            Map<Block, Set<Block>> blockMap = new Object2ObjectOpenHashMap<>();

            for (Map.Entry<ResourceLocation, Set<ResourceLocation>> groupEntry : groupMap.entrySet()) {
                ResourceLocation key = groupEntry.getKey();
                Block keyBlock = BuiltInRegistries.BLOCK.get(key);
                if(keyBlock == Blocks.AIR) {
                    continue;
                }

                for (ResourceLocation value : groupEntry.getValue()) {
                    Block valueBlock = BuiltInRegistries.BLOCK.get(value);
                    if(valueBlock == Blocks.AIR) {
                        continue;
                    }

                    Set<Block> keySet = blockMap.computeIfAbsent(keyBlock, b -> new ObjectOpenHashSet<>());
                    Set<Block> valueSet = blockMap.computeIfAbsent(valueBlock, b -> new ObjectOpenHashSet<>());
                    keySet.add(valueBlock);
                    valueSet.add(keyBlock);
                }
            }

            GROUPS_MAP.put(id, new BlockGrouping(blockMap));
        }

    }
}
