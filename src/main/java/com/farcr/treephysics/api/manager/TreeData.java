package com.farcr.treephysics.api.manager;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TreeData {
    public static final Codec<TreeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("sub_level_id").forGetter(o -> o.subLevelId),
            Codec.INT.fieldOf("life_ticks").forGetter(o -> o.lifeTicks),
            Codec.INT.fieldOf("leaf_break_progress").forGetter(o -> o.leafBreakProgress),
            Codec.INT.fieldOf("logs").forGetter(o -> o.logs)
    ).apply(instance, TreeData::new));

    public final UUID subLevelId;
    public int lifeTicks;
    public int leafBreakProgress;
    public int logs;

    public TreeData(UUID subLevelId, int lifeTicks, int leafBreakProgress, int logs) {
        this.subLevelId = subLevelId;
        this.lifeTicks = lifeTicks;
        this.leafBreakProgress = leafBreakProgress;
    }

    public TreeData(UUID subLevelId) {
        this(subLevelId, 0, 1, 0);
    }

    public void updateLogCount(Level level) {
        SubLevel subLevel = getSubLevel(level);
        if(subLevel != null) {
            int logs = 0;
            BoundingBox3ic box = subLevel.getPlot().getBoundingBox();
            Iterable<BlockPos> iterable = BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
            for (BlockPos pos : iterable) {
                BlockState state = level.getBlockState(pos);
                if(state.is(BlockTags.LOGS)) {
                    logs++;
                }
            }
            this.logs = logs;
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
                '}';
    }
}
