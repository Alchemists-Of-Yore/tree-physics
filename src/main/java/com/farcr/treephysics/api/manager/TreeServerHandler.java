package com.farcr.treephysics.api.manager;

import com.farcr.treephysics.TreePhysicsConfig;
import com.farcr.treephysics.client.TreeManager;
import com.farcr.treephysics.networking.UpdateClientTrees;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import foundry.veil.api.network.VeilPacketManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.joml.Vector3d;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TreeServerHandler extends SavedData implements TreeManager {
    public static final String ID = "treephysics_trees";

    private final Map<UUID, TreeData> trees = new Object2ObjectOpenHashMap<>();
    private ServerLevel level;

    public TreeServerHandler() {
        this(null);
    }

    public TreeServerHandler(ServerLevel level) {
        this.level = level;
    }

    public void physicsTick(ServerLevel level, SubLevelPhysicsSystem system, PhysicsPipeline pipeline, double timeStep) {
        for (TreeData tree : this.trees.values()) {
            ServerSubLevel subLevel = (ServerSubLevel) tree.getSubLevel(this.level);
            if(subLevel == null) {
                continue;
            }

            Vector3d gravity = DimensionPhysicsData.getGravity(level);

            Vector3d dir = subLevel.logicalPose().transformNormal(new Vector3d(0, 1, 0));
            double gravityScale = 1.0 -  Math.max(0, dir.dot(0, 1, 0));

            RigidBodyHandle handle = system.getPhysicsHandle(subLevel);
            handle.addLinearAndAngularVelocity(gravity.mul(timeStep * gravityScale), JOMLConversion.ZERO);
        }
    }

    public void tick() {
        for (TreeData tree : this.trees.values()) {
            SubLevel subLevel = tree.getSubLevel(this.level);
            if(subLevel == null) {
                continue;
            }

            if (tree.lifeTicks > TreePhysicsConfig.MAX_LIFE_TICKS.getAsInt()) {
                subLevel.markRemoved();
                continue;
            }

            // TODO rework
            Vector3d dir = subLevel.logicalPose().transformNormal(new Vector3d(0, 1, 0));
            double uprightness = dir.dot(0, 1, 0);
            double threshold = 0.25;

            if(uprightness < threshold && (tree.lifeTicks % 5 == 0) && tree.leafBreakProgress <= LeavesBlock.DECAY_DISTANCE) {
                int distance = LeavesBlock.DECAY_DISTANCE - tree.leafBreakProgress;
                BoundingBox3ic box = subLevel.getPlot().getBoundingBox();
                Iterable<BlockPos> posIterator = BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
                for (BlockPos blockPos : posIterator) {
                    BlockState state = subLevel.getLevel().getBlockState(blockPos);
                    if(state.getBlock() instanceof LeavesBlock && state.getValue(LeavesBlock.DISTANCE) == distance) {
                        subLevel.getLevel().destroyBlock(blockPos, true);
                    }
                }
                tree.leafBreakProgress++;
            }

            tree.lifeTicks++;
            this.setDirty(true);
        }

    }

    public void setTree(SubLevel subLevel) {
        TreeData data = new TreeData(subLevel.getUniqueId());
        this.trees.put(subLevel.getUniqueId(), data);
        this.setDirty();
        this.sendAllTrees();
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
                TreeServerHandler handler = TreeServerHandler.get(serverLevel);
                VeilPacketManager.player((ServerPlayer) player).sendPacket(new UpdateClientTrees(serverLevel.dimension(), handler.trees.keySet().stream().toList()));
            }
        }
    }

    @Override
    public boolean isTree(SubLevel subLevel) {
        return subLevel != null && trees.containsKey(subLevel.getUniqueId());
    }

    private static TreeServerHandler create(ServerLevel level, CompoundTag tag, HolderLookup.Provider registries) {
        TreeServerHandler handler = new TreeServerHandler(level);
        ListTag list = (ListTag) tag.get(ID);
        handler.loadTrees(list);
        return handler;
    }

    public static TreeServerHandler get(ServerLevel level) {
        TreeServerHandler handler = level.getChunkSource().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(TreeServerHandler::new, (tag, provider) -> create(level, tag, provider), null),
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
}
