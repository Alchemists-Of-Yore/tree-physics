package com.farcr.treephysics.client;

import com.farcr.treephysics.TreePhysics;
import com.farcr.treephysics.index.TreePhysicsParticleTypes;
import com.farcr.treephysics.particle.collision_dust.CollisionDustProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@Mod(value = TreePhysics.MOD_ID, dist = Dist.CLIENT)
public class TreePhysicsClient {

    public static final ClientTreeManager TREE_MANAGER = new ClientTreeManager();

    public TreePhysicsClient(IEventBus eventBus, ModContainer container) {
        eventBus.addListener(this::registerParticleProviders);
    }

    private void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(TreePhysicsParticleTypes.COLLISION_DUST.get(), CollisionDustProvider::new);
    }

}
