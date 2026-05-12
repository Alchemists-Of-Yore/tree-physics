package com.farcr.treephysics.collision_callback;

import com.farcr.treephysics.api.manager.ServerTreeManager;
import com.farcr.treephysics.api.manager.TreeData;
import com.farcr.treephysics.client.TreeManager;
import dev.ryanhcode.sable.physics.callback.FragileBlockCallback;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public class LogCallback extends FragileBlockCallback {

    private static final Vector3dc[] OFFSETS = new Vector3d[] {
            new Vector3d(0, -1, 0),
            new Vector3d(1, 0, 0),
            new Vector3d(-1, 0, 0),
            new Vector3d(0, 0, 1),
            new Vector3d(0, 0, -1),
            new Vector3d(0, 1, 0),
    };

    private static final Vector3d offsetPos = new Vector3d();
    private static final BlockPos.MutableBlockPos offsetBlockPos = new BlockPos.MutableBlockPos();

    public static final LogCallback INSTANCE = new LogCallback();

    @Override
    public double getTriggerVelocity() {
        return 2;
    }

    @Override
    public boolean shouldTriggerFor(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    @Override
    public CollisionResult onHit(ServerLevel level, BlockPos pos, BlockState state, Vector3d hitPos) {
        ServerTreeManager treeManager = (ServerTreeManager) TreeManager.get(level);
        SubLevel subLevel = treeManager.getTree(pos);
        if(subLevel != null) {
            TreeData data = treeManager.getTreeData(subLevel);

            subLevel.logicalPose().transformPosition(hitPos);
            BlockState hitState = null;
            float dist = 0.3f;

            for (Vector3dc offset : OFFSETS) {
                offset.mul(dist, offsetPos);
                offsetPos.add(hitPos);
                BlockPos.MutableBlockPos blockPos = offsetBlockPos.set(offsetPos.x, offsetPos.y, offsetPos.z);
                BlockState blockState = level.getBlockState(blockPos);
                if(!blockState.isAir() && blockState.isSolid()) {
                    hitState = blockState;
                    break;
                }
            }

            data.onCollision(level, treeManager, subLevel, hitPos, hitState, pos, state);
        }

        return CollisionResult.NONE;
    }
}
