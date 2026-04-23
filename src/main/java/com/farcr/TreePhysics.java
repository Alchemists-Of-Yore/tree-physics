package com.farcr;

import com.farcr.event.CommonEvents;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(TreePhysics.MOD_ID)
public class TreePhysics {
    public static final String MOD_ID = "treephysics";
    private static final Logger LOGGER = LogUtils.getLogger();

    public TreePhysics(IEventBus modEventBus, ModContainer modContainer) {
        NeoForge.EVENT_BUS.register(CommonEvents.class);
    }

}
