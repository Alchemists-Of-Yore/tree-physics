package com.farcr.treephysics.neoforge.mixin.compatibility.create;

import com.farcr.treephysics.api.util.FloodFillUtil;
import com.farcr.treephysics.index.TreePhysicsConfig;
import com.simibubi.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SawBlockEntity.class)
public abstract class SawBlockEntityMixin extends BlockBreakingKineticBlockEntity {


    @Shadow
    public ProcessingInventory inventory;

    public SawBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "onBlockBroken", at = @At("HEAD"), cancellable = true)
    private void treephysics$onBlockBroken(BlockState stateToBreak, CallbackInfo ci) {
        if (!TreePhysicsConfig.CREATE_SAWS_FELL_TREES.get()) return;

        if (this.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos brokenPos = this.breakingPos;
            BlockState brokenState = serverLevel.getBlockState(brokenPos);
            Vec3 pos = this.getBlockPos().getCenter();
            FloodFillUtil.blockBroken(serverLevel, brokenPos, brokenState, pos, false);
        }

        super.onBlockBroken(stateToBreak);
        ci.cancel();
    }

}
