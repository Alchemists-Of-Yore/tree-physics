package com.farcr.treephysics.mixin.leaf_effect_fixes;

import com.farcr.treephysics.client.TreeManager;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {

    @Shadow
    protected ClientLevel level;

    @WrapMethod(method = "destroy")
    private void treephysics$destroy(BlockPos pos, BlockState state, Operation<Void> original) {
        TreeManager manager = TreeManager.get(this.level);
        if(manager.isTree(pos) && state.is(BlockTags.LEAVES)) {
            treephysics$destroyButWithLessParticles(pos, state, this.level, (ParticleEngine) (Object) this);
            return;
        }
        original.call(pos, state);
    }

    @Unique
    private static void treephysics$destroyButWithLessParticles(BlockPos pos, BlockState state, ClientLevel level, ParticleEngine self) {
        if (!state.isAir()) {
            int i = 2;
            int j = 2;
            int k = 2;

            for (int l = 0; l < i; l++) {
                for (int i1 = 0; i1 < j; i1++) {
                    for (int j1 = 0; j1 < k; j1++) {
                        double d4 = ((double) l + 0.5) / (double) i;
                        double d5 = ((double) i1 + 0.5) / (double) j;
                        double d6 = ((double) j1 + 0.5) / (double) k;
                        self.add(
                                new TerrainParticle(
                                        level,
                                        (double) pos.getX() + d4,
                                        (double) pos.getY() + d5,
                                        (double) pos.getZ() + d6,
                                        d4 - 0.5,
                                        d5 - 0.5,
                                        d6 - 0.5,
                                        state,
                                        pos
                                ).updateSprite(state, pos)
                        );
                    }
                }
            }
        }
    }

}
