package com.farcr.treephysics.api.manager;

import com.farcr.treephysics.api.TreeUtil;
import com.farcr.treephysics.index.TreePhysicsSounds;
import com.farcr.treephysics.index.TreePhysicsTags;
import com.farcr.treephysics.particle.collision_dust.CollisionDustParticleOptions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.UUID;

public class TreeData {
    public static final Codec<TreeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("sub_level_id").forGetter(o -> o.subLevelId),
            Codec.INT.fieldOf("life_ticks").forGetter(o -> o.lifeTicks),
            Codec.INT.fieldOf("leaf_break_progress").forGetter(o -> o.leafBreakProgress),
            Codec.INT.fieldOf("logs").forGetter(o -> o.logs),
            Codec.BOOL.fieldOf("is_branch").forGetter(o -> o.isBranch),
            Codec.BOOL.fieldOf("has_leaves").forGetter(o -> o.hasLeaves),
            Codec.BOOL.fieldOf("played_impact").forGetter(o -> o.playedImpact),
            Codec.BOOL.fieldOf("played_creak").forGetter(o -> o.playedCreak)
    ).apply(instance, TreeData::new));

    public final UUID subLevelId;
    public int lifeTicks;
    public int leafBreakProgress;
    public int logs;
    public boolean isBranch;
    public boolean hasLeaves;
    public boolean playedImpact;
    public boolean playedCreak;
    public transient int stoppedTicks;

    public TreeData(UUID subLevelId, int lifeTicks, int leafBreakProgress, int logs, boolean isBranch, boolean hasLeaves, boolean playedImpact, boolean playedCreak) {
        this.subLevelId = subLevelId;
        this.lifeTicks = lifeTicks;
        this.leafBreakProgress = leafBreakProgress;
        this.logs = logs;
        this.isBranch = isBranch;
        this.hasLeaves = hasLeaves;
        this.playedImpact = playedImpact;
        this.playedCreak = playedCreak;
    }

    public TreeData(UUID subLevelId) {
        this(subLevelId, 0, -1, 0, false, false, false, false);
    }

    public void onCollision(ServerLevel level, ServerTreeManager manager, SubLevel subLevel, Vector3d hitPos, @Nullable BlockState hitState, BlockPos thisPos, BlockState thisState) {
        if(hitState != null && hitState.is(TreePhysicsTags.PRODUCES_DUST_ON_IMPACT)) {
                level.sendParticles(new CollisionDustParticleOptions(hitState), hitPos.x, hitPos.y, hitPos.z, 6, 0, 0, 0, 1);
        }

        Vector3d position = subLevel.logicalPose().position();

        double uprightness = TreeUtil.getUprightness(subLevel);
        float pitch = (float) (1.0 - Mth.clamp(this.logs / 64.0f, 0.0f, 0.25f));
        if(uprightness < 0.75) {
            if(!this.playedImpact && !this.isBranch) {
                level.playSound(null, position.x, position.y, position.z, TreePhysicsSounds.TREE_IMPACT, SoundSource.BLOCKS, 3.0f, pitch);
                this.playedImpact = true;
            }
            manager.startBreakingLeaves(subLevel);
        } else {
            if(!this.playedCreak) {
                level.playSound(null, position.x, position.y, position.z, TreePhysicsSounds.TREE_CREAK, SoundSource.BLOCKS, 1.0f, pitch);

                if(this.hasLeaves){
                    level.playSound(null, position.x, position.y, position.z, TreePhysicsSounds.LEAF_RUSTLE, SoundSource.BLOCKS, 0.8f, pitch);
                }

                this.playedCreak = true;
            }
        }
    }

    public void updateTreeData(Level level) {
        SubLevel subLevel = getSubLevel(level);
        if(subLevel != null) {
            int logs = 0;
            int horizontalLogs = 0;
            int verticalLogs = 0;
            for (BlockPos pos : TreeUtil.plotIterator(subLevel)) {
                BlockState state = level.getBlockState(pos);
                if(state.is(BlockTags.LOGS)) {
                    logs++;
                    if(state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y) {
                        verticalLogs++;
                    } else {
                        horizontalLogs++;
                    }
                } else if(state.is(BlockTags.LEAVES)) {
                    this.hasLeaves = true;
                }
            }
            this.logs = logs;
            this.isBranch = horizontalLogs > verticalLogs;
        }
    }

    public TreeData copy(TreeData data) {
        this.lifeTicks = data.lifeTicks;
        this.leafBreakProgress = data.leafBreakProgress;
        return this;
    }

    public @Nullable SubLevel getSubLevel(Level level) {
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        return container.getSubLevel(this.subLevelId);
    }

    @Override
    public String toString() {
        return "TreeData{" +
                "subLevelId=" + subLevelId +
                ", lifeTicks=" + lifeTicks +
                ", leafBreakProgress=" + leafBreakProgress +
                ", logs=" + logs +
                ", isBranch=" + isBranch +
                ", hasLeaves=" + hasLeaves +
                ", playedImpact=" + playedImpact +
                ", playedCreak=" + playedCreak +
                '}';
    }
}
