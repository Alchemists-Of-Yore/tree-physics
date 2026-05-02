package com.farcr.treephysics.event;

import com.farcr.treephysics.TreePhysics;
import com.farcr.treephysics.api.manager.ServerTreeManager;
import com.farcr.treephysics.api.manager.TreeSubLevelObserver;
import com.farcr.treephysics.api.tree_gathering.TreeGatherer;
import com.farcr.treephysics.client.TreeManager;
import com.farcr.treephysics.index.TreePhysicsConfig;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.joml.Vector3d;

import java.util.List;

@EventBusSubscriber(modid = TreePhysics.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        ServerTreeManager.sendUpdatePacket(player);
    }

    @SubscribeEvent
    public static void blockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = player.level();
        BlockPos pos = event.getPos();
        BlockState brokenState = level.getBlockState(pos);
        ServerTreeManager manager = (ServerTreeManager) TreeManager.get(level);

        if(brokenState.is(BlockTags.LOGS)) {
            if(manager.isTree(pos)) {
                SubLevel tree = manager.getTree(pos);
                manager.decrementLogs(tree);
                return;
            }

            if(!player.isShiftKeyDown()) {
                List<ServerSubLevel> subLevels = TreeGatherer.trySplit((ServerLevel) event.getLevel(), pos);

                BlockPos belowPos = pos.below();
                BlockState belowState = level.getBlockState(belowPos);
                if(belowState.is(Blocks.ROOTED_DIRT)) {
                    level.setBlock(belowPos, Blocks.DIRT.defaultBlockState(), 2);
                }

                if(subLevels == null || subLevels.isEmpty()) return;
                if(!(brokenState.getBlock() instanceof RotatedPillarBlock) || brokenState.getValue(RotatedPillarBlock.AXIS) != Direction.Axis.Y) return;

                for (ServerSubLevel subLevel : subLevels) {
                    SubLevelPhysicsSystem system = SubLevelPhysicsSystem.get(level);
                    RigidBodyHandle handle = system.getPhysicsHandle(subLevel);

                    Vec3 breakDirection = player.getEyePosition().subtract(pos.getCenter()).normalize();
                    Vector3d forward = new Vector3d(JOMLConversion.toJOML(Direction.getNearest(breakDirection).getNormal()));
                    forward.rotateAxis(Math.toRadians(level.getRandom().nextIntBetweenInclusive(-25, 25)), 0, 1, 0);

                    Vector3d torque = forward.cross(0, 1, 0, new Vector3d()).mul(TreePhysicsConfig.IMPULSE_TORQUE.getAsDouble());
                    Vector3d velocity = forward.negate(new Vector3d()).mul(TreePhysicsConfig.IMPULSE_FORCE.getAsDouble());

                    handle.addLinearAndAngularVelocity(velocity, torque);
                }
            }
        }
    }

    @SubscribeEvent
    public static void blockPlace(BlockEvent.EntityPlaceEvent event) {
        LevelAccessor level = event.getLevel();
        BlockPos pos = event.getPos();
        ServerTreeManager manager = (ServerTreeManager) TreeManager.get((Level) level);
        SubLevel subLevel = manager.getTree(pos);
        if(subLevel != null) {
            manager.updateTree(subLevel);
        }
    }

    @SubscribeEvent
    public static void itemUseOnBlock(UseItemOnBlockEvent event) {
        BlockPos pos = event.getPos();
        SubLevel subLevel = Sable.HELPER.getContaining(event.getLevel(), pos);
        TreeManager treeManager = TreeManager.get(event.getLevel());
        Level level = event.getLevel();

        if(treeManager.isTree(subLevel)) {
            boolean isWaxing = event.getUsePhase() == UseItemOnBlockEvent.UsePhase.ITEM_AFTER_BLOCK && event.getItemStack().is(Items.HONEYCOMB);
            if(isWaxing) {
                if(!event.getLevel().isClientSide()) {
                    ServerTreeManager handler = (ServerTreeManager) treeManager;
                    handler.unsetTree(subLevel);
                } else {
                    BoundingBox3ic box = subLevel.getPlot().getBoundingBox();
                    Iterable<BlockPos> iterator = BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1);
                    for (BlockPos blockPos : iterator) {
                        BlockState state = level.getBlockState(blockPos);
                        if(!state.isAir()) {
                            for (Direction direction : Direction.values()) {
                                BlockState relative = level.getBlockState(blockPos.relative(direction));
                                if(relative.isAir()) {
                                    ParticleUtils.spawnParticlesOnBlockFace(level, blockPos, ParticleTypes.WAX_ON, UniformInt.of(1, 2), direction, () -> new Vec3(0, 0, 0), 0.55);
                                }
                            }
                        }
                    }
                }

                event.cancelWithResult(ItemInteractionResult.SUCCESS);
                level.playSound(null, event.getPos(), SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0f, 1.0f);
                event.getItemStack().shrink(1);
            } else {
                if(!TreePhysicsConfig.CAN_BUILD.getAsBoolean()) {
                    event.setCanceled(true);
                }
            }
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
