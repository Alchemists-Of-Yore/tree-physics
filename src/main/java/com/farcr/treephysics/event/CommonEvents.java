package com.farcr.treephysics.event;

import com.farcr.treephysics.TreePhysics;
import com.farcr.treephysics.api.tree_gathering.TreeGatherer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = TreePhysics.MOD_ID)
public class CommonEvents {

    @SubscribeEvent
    public static void blockBreak(BlockEvent.BreakEvent event) {
        if(!event.getPlayer().isShiftKeyDown() && event.getPlayer().getItemInHand(InteractionHand.MAIN_HAND).is(ItemTags.AXES)) {
            TreeGatherer.trySplit((ServerLevel) event.getLevel(), event.getPos());
        }
    }

}
