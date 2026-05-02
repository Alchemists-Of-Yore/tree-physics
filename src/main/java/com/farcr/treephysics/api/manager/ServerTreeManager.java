package com.farcr.treephysics.api.manager;

import com.farcr.treephysics.client.TreeManager;
import com.farcr.treephysics.index.TreePhysicsConfig;
import com.farcr.treephysics.networking.UpdateClientTrees;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import foundry.veil.api.network.VeilPacketManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerTreeManager extends SavedData implements TreeManager {
    public static final String ID = "treephysics_trees";

    private final Map<UUID, TreeData> trees = new Object2ObjectOpenHashMap<>();
    private ServerLevel level;

    public ServerTreeManager() {
        this(null);
    }

    public ServerTreeManager(ServerLevel level) {
        this.level = level;
    }

    public void physicsTick(ServerLevel level, SubLevelPhysicsSystem system, PhysicsPipeline pipeline, double timeStep) {
        for (TreeData tree : this.trees.values()) {
            ServerSubLevel subLevel = (ServerSubLevel) tree.getSubLevel(this.level);
            if(subLevel == null) {
                continue;
            }

            int gravityTicks = TreePhysicsConfig.GRAVITY_MULTIPLIER_TICKS.getAsInt();
            if(gravityTicks == -1 || tree.lifeTicks <= gravityTicks) {
                Vector3d gravity = DimensionPhysicsData.getGravity(level);

                Vector3d dir = subLevel.logicalPose().transformNormal(new Vector3d(0, 1, 0));
                double gravityScale = 1.0 -  Math.max(0, dir.dot(0, 1, 0));

                gravityScale *= Math.max(0.0, TreePhysicsConfig.GRAVITY_MULTIPLIER.getAsDouble() - 1.0);


                RigidBodyHandle handle = system.getPhysicsHandle(subLevel);
                handle.addLinearAndAngularVelocity(gravity.mul(timeStep * gravityScale), JOMLConversion.ZERO);
            }
        }
    }

    public void tick() {
        for (TreeData tree : this.trees.values()) {
            SubLevel subLevel = tree.getSubLevel(this.level);
            if(subLevel == null) {
                continue;
            }

            if(this.shouldDespawn(tree)) {
                subLevel.markRemoved();
                continue;
            }

            tree.lifeTicks++;
            this.setDirty(true);
        }

    }

    public void setTree(SubLevel subLevel) {
        TreeData data = new TreeData(subLevel.getUniqueId());
        data.updateLogCount(this.level);
        this.trees.put(subLevel.getUniqueId(), data);
        this.setDirty();
        this.sendAllTrees();
    }

    public void updateTree(@Nullable SubLevel subLevel) {
        if(subLevel == null) return;
        TreeData data = this.trees.get(subLevel.getUniqueId());
        if(data != null) {
            int logCount = data.logs;
            data.updateLogCount(this.level);
            if(logCount != data.logs) {
                this.setDirty();
            }
        }
    }

    public void decrementLogs(@Nullable SubLevel subLevel) {
        if(subLevel == null) return;
        TreeData data = this.trees.get(subLevel.getUniqueId());
        if(data != null) {
            data.logs = Math.max(0, data.logs - 1);
            this.setDirty();
        }
    }

    public void unsetTree(SubLevel subLevel) {
        this.trees.remove(subLevel.getUniqueId());
        this.setDirty();
        this.sendAllTrees();
    }

    public void setSplitFrom(SubLevel subLevel, SubLevel split) {
        TreeData originalTree = this.trees.get(subLevel.getUniqueId());
        if(originalTree != null) {
            TreeData data = new TreeData(split.getUniqueId()).copy(originalTree);
            data.updateLogCount(this.level);
            originalTree.updateLogCount(this.level);
            this.trees.put(split.getUniqueId(), data);
            this.sendAllTrees();
            this.setDirty();
        }
    }

    @Override
    public void setDirty(boolean dirty) {
        super.setDirty(dirty);
    }

    private void sendAllTrees() {
        VeilPacketManager.all(this.level.getServer())
                .sendPacket(new UpdateClientTrees(this.level.dimension(), this.trees.keySet().stream().toList()));
    }

    public static void sendUpdatePacket(Player player) {
        MinecraftServer server = player.getServer();
        if(server != null) {
            for (ServerLevel serverLevel : server.getAllLevels()) {
                ServerTreeManager handler = ServerTreeManager.get(serverLevel);
                VeilPacketManager.player((ServerPlayer) player).sendPacket(new UpdateClientTrees(serverLevel.dimension(), handler.trees.keySet().stream().toList()));
            }
        }
    }

    @Override
    public boolean isTree(@Nullable SubLevel subLevel) {
        return subLevel != null && trees.containsKey(subLevel.getUniqueId());
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    private static ServerTreeManager create(ServerLevel level, CompoundTag tag, HolderLookup.Provider registries) {
        ServerTreeManager handler = new ServerTreeManager(level);
        ListTag list = (ListTag) tag.get(ID);
        handler.loadTrees(list);
        return handler;
    }

    public static ServerTreeManager get(ServerLevel level) {
        ServerTreeManager handler = level.getChunkSource().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ServerTreeManager::new, (tag, provider) -> create(level, tag, provider), null),
                ID);
        handler.level = level;
        return handler;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.put(ID, this.saveTheTrees());
        return tag;
    }

    private void loadTrees(ListTag list) {
        List<TreeData> treeData = TreeData.CODEC.listOf().parse(NbtOps.INSTANCE, list).getOrThrow();
        for (TreeData data : treeData) {
            this.trees.put(data.subLevelId, data);
        }
        System.out.println("trees loaded! " + treeData);
    }

    private ListTag saveTheTrees() {
        List<TreeData> values = this.trees.values().stream().toList();
        System.out.println("trees saved! " + values);
        return (ListTag) TreeData.CODEC.listOf().encodeStart(NbtOps.INSTANCE, values).getOrThrow();
    }

    private boolean shouldDespawn(TreeData data) {
        TreePhysicsConfig.DespawnBehavior behavior = TreePhysicsConfig.DESPAWN_BEHAVIOR.get();
        if(behavior == TreePhysicsConfig.DespawnBehavior.NO_DESPAWN) {
            return false;
        }

        if(data.lifeTicks >= TreePhysicsConfig.DESPAWN_TIME.getAsInt()) {
            if(behavior == TreePhysicsConfig.DespawnBehavior.DESPAWN_ALL) {
                return true;
            }

            return data.logs <= 5;
        }

        return false;
    }
}
