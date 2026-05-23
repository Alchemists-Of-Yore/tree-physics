package com.farcr.treephysics.event;

import com.farcr.treephysics.TreePhysics;
import com.farcr.treephysics.api.TreeUtil;
import com.farcr.treephysics.api.manager.ServerTreeManager;
import com.farcr.treephysics.api.manager.TreeSubLevelObserver;
import com.farcr.treephysics.client.TreeManager;
import com.farcr.treephysics.data.TreePhysicsLang;
import com.farcr.treephysics.index.TreePhysicsCommands;
import com.farcr.treephysics.index.TreePhysicsConfig;
import com.mojang.brigadier.CommandDispatcher;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.AlterGroundEvent;
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

        if(TreeUtil.isLog(brokenState)) {
            if(manager.isTree(pos)) {
                SubLevel tree = manager.getTree(pos);
                manager.decrementLogs(tree);
                return;
            }

            if(!player.isShiftKeyDown()) {
                if(TreePhysicsConfig.REQUIRES_AXE.getAsBoolean() && !event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.AXES)) {
                    return;
                }

                List<ServerSubLevel> subLevels = TreeUtil.trySplit((ServerLevel) level, pos);

                BlockPos belowPos = pos.below();
                BlockState belowState = level.getBlockState(belowPos);
                if(belowState.is(Blocks.ROOTED_DIRT) && TreePhysicsConfig.REMOVE_ROOTED_DIRT.get()) {
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

    @SubscribeEvent
    public static void entityPlace(BlockEvent.EntityPlaceEvent event) {
        if(TreePhysicsConfig.PREVENT_INTERACTING_WITH_TREES.get()) return;
        if(event.getLevel() instanceof ServerLevel level) {
            BlockPos pos = event.getPos();
            ServerTreeManager manager = (ServerTreeManager) TreeManager.get(level);
            SubLevel subLevel = Sable.HELPER.getContaining(level, pos);
            if(manager.isTree(subLevel)) {
                manager.unsetTree(subLevel);
            }
        }
    }

    @SubscribeEvent
    public static void useItemOnBlock(UseItemOnBlockEvent event) {
        if(!TreePhysicsConfig.PREVENT_INTERACTING_WITH_TREES.get()) return;
        BlockPos pos = event.getPos();
        SubLevel subLevel = Sable.HELPER.getContaining(event.getLevel(), pos);
        TreeManager treeManager = TreeManager.get(event.getLevel());

        if(treeManager.isTree(subLevel)) {
            event.setCanceled(true);
        }
    }

    // will need to just mixin to AlterGroundDecorator when porting to multiloader for this
    @SubscribeEvent
    public static void alterGround(AlterGroundEvent event) {
        if(!TreePhysicsConfig.ROOTED_DIRT_GENERATION.getAsBoolean()) {
            return;
        }

        TreeDecorator.Context context = event.getContext();
        LevelSimulatedReader reader = context.level();

        AlterGroundEvent.StateProvider provider = event.getStateProvider();
        event.setStateProvider((random, pos) -> {
            boolean isRoot = reader.isStateAtPosition(pos, TreeUtil::isRoot);
            if(isRoot) {
                return Blocks.ROOTED_DIRT.defaultBlockState();
            }
            return provider.getState(random, pos);
        });
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandBuildContext context = event.getBuildContext();
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        TreePhysicsCommands.registerCommands(context, dispatcher);
    }

    @EventBusSubscriber(modid = TreePhysics.MOD_ID)
    public static class Mod {

        @SubscribeEvent
        public static void gatherData(GatherDataEvent event) {
            if (event.includeClient()) {
                PackOutput output = event.getGenerator().getPackOutput();
                event.addProvider(new TreePhysicsLang(output));
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
