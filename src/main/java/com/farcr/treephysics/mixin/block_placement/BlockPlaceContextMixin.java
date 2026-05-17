package com.farcr.treephysics.mixin.block_placement;

import com.farcr.treephysics.client.TreeManager;
import com.farcr.treephysics.index.TreePhysicsConfig;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockPlaceContext.class)
public abstract class BlockPlaceContextMixin extends UseOnContext {

    private BlockPlaceContextMixin() {
        super(null, null, null);
    }

    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void treephysics$canPlace(CallbackInfoReturnable<Boolean> cir) {
        if(!TreePhysicsConfig.PREVENT_BUILDING_ON_TREES.getAsBoolean()) return;

        Level level = this.getLevel();
        SubLevel subLevel = Sable.HELPER.getContaining(level, this.getClickedPos());
        TreeManager treeManager = TreeManager.get(level);

        if(treeManager.isTree(subLevel)) {
            cir.setReturnValue(false);
        }
    }

}
