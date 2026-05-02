package com.farcr.treephysics.mixin.tree_splitting;

import com.farcr.treephysics.api.manager.ServerTreeManager;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SubLevelAssemblyHelper.class)
public class SubLevelAssemblyHelperMixin {

    @Inject(method = "assembleBlocks", at = @At("RETURN"))
    private static void treephysics$assembleBlocks(ServerLevel level, BlockPos anchor, Iterable<BlockPos> blocks, BoundingBox3ic bounds, CallbackInfoReturnable<ServerSubLevel> cir) {
        SubLevel subLevel = Sable.HELPER.getContaining(level, anchor);
        ServerTreeManager handler = ServerTreeManager.get(level);
        if(handler.isTree(subLevel)) {
            ServerSubLevel returnValue = cir.getReturnValue();
            handler.setSplitFrom(subLevel, returnValue);
        }
    }

}
