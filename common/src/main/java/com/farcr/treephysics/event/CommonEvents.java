package com.farcr.treephysics.event;

import com.farcr.treephysics.api.flood_fill.TreeResult;
import com.farcr.treephysics.api.manager.ServerTreeManager;
import com.farcr.treephysics.api.manager.TreeManager;
import com.farcr.treephysics.api.manager.TreeSubLevelObserver;
import com.farcr.treephysics.api.util.FloodFillUtil;
import com.farcr.treephysics.api.util.TreeUtil;
import com.farcr.treephysics.index.TreePhysicsConfig;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class CommonEvents {

    public static void playerLoggedIn(Player player) {
        ServerTreeManager.sendUpdatePacket(player);
    }

    public static void playerBreakBlock(Level level, Player player, BlockPos pos) {
        BlockState brokenState = level.getBlockState(pos);
        ServerTreeManager manager = (ServerTreeManager) TreeManager.get(level);
        ServerLevel serverLevel = (ServerLevel) level;

        if(TreeUtil.isLog(brokenState)) {
            if(manager.isTree(pos)) {
                List<TreeResult> treeResults = FloodFillUtil.findTreesAround(serverLevel, pos);
                if(treeResults.size() > 1) {
                    treeResults = new ArrayList<>(treeResults.stream().sorted().toList());
                    treeResults.removeFirst();
                    for (TreeResult tree : treeResults) {
                        BoundingBox3i box = BoundingBox3i.from(tree.getBlocks());
                        SubLevelAssemblyHelper.assembleBlocks(serverLevel, pos, tree.getBlocks(), box);
                    }
                }

                return;
            }

            if(!player.isShiftKeyDown()) {
                if(TreePhysicsConfig.REQUIRES_AXE.getAsBoolean() && !player.getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.AXES)) {
                    return;
                }

                List<ServerSubLevel> subLevels = FloodFillUtil.trySplit(serverLevel, pos);

                BlockPos belowPos = pos.below();
                BlockState belowState = level.getBlockState(belowPos);
                if(TreeUtil.isRoot(belowState) && TreePhysicsConfig.REMOVE_ROOTED_DIRT.get()) {
                    if(TreePhysicsConfig.DROP_HANGING_ROOTS.get()) {
                        Block.popResourceFromFace(level, belowPos, Direction.UP, Blocks.HANGING_ROOTS.asItem().getDefaultInstance());
                    }
                    level.setBlock(belowPos, Blocks.DIRT.defaultBlockState(), 2);
                }

                if(TreeUtil.getLogAxis(brokenState) != Direction.Axis.Y) return;

                for (ServerSubLevel subLevel : subLevels) {
                    SubLevelPhysicsSystem system = SubLevelPhysicsSystem.get(level);
                    RigidBodyHandle handle = system.getPhysicsHandle(subLevel);

                    Vec3 breakDirection = player.getEyePosition().subtract(pos.getCenter()).normalize().multiply(1, 0, 1);
                    Vector3d forward = new Vector3d(JOMLConversion.toJOML(Direction.getNearest(breakDirection).getNormal()));
                    forward.rotateAxis(Math.toRadians(level.getRandom().nextIntBetweenInclusive(-25, 25)), 0, 1, 0);

                    Vector3d torque = forward.cross(0, 1, 0, new Vector3d()).mul(TreePhysicsConfig.IMPULSE_TORQUE.getAsDouble());
                    Vector3d velocity = forward.negate(new Vector3d()).mul(TreePhysicsConfig.IMPULSE_FORCE.getAsDouble());

                    handle.addLinearAndAngularVelocity(velocity, torque);
                }
            }
        }
    }

    public static void entityPlace(Level level, BlockPos pos) {
        if(TreePhysicsConfig.PREVENT_INTERACTING_WITH_TREES.get()) return;
        if(level instanceof ServerLevel serverLevel) {
            ServerTreeManager manager = (ServerTreeManager) TreeManager.get(serverLevel);
            SubLevel subLevel = Sable.HELPER.getContaining(serverLevel, pos);
            if(manager.isTree(subLevel)) {
                manager.unsetTree(subLevel);
            }
        }
    }

    public static void useItemOnBlock(Level level, BlockPos pos, Runnable cancel) {
        if(!TreePhysicsConfig.PREVENT_INTERACTING_WITH_TREES.get()) return;
        SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
        TreeManager treeManager = TreeManager.get(level);

        if(treeManager.isTree(subLevel)) {
            cancel.run();
        }
    }

    public static void containerReady(Level level, SubLevelContainer container) {
        if(!(container instanceof ServerSubLevelContainer serverContainer)) {
            return;
        }

        serverContainer.addObserver(new TreeSubLevelObserver(serverContainer.getLevel()));
    }

    public static void postPhysicsTick(SubLevelPhysicsSystem system, double timeStep) {
        ServerLevel level = system.getLevel();
        ServerTreeManager handler = ServerTreeManager.get(level);
        PhysicsPipeline pipeline = system.getPipeline();
        handler.physicsTick(level, system, pipeline, timeStep);
    }
}
