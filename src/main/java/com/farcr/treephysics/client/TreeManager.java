package com.farcr.treephysics.client;

import com.farcr.treephysics.api.manager.ServerTreeManager;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface TreeManager {
    @Contract("null -> false")
    boolean isTree(@Nullable SubLevel subLevel);

    Level getLevel();

    default boolean isTree(BlockPos pos) {
        SubLevel subLevel = Sable.HELPER.getContaining(getLevel(), pos);
        return isTree(subLevel);
    }

    default @Nullable SubLevel getTree(BlockPos pos) {
        SubLevel subLevel = Sable.HELPER.getContaining(getLevel(), pos);
        if(isTree(subLevel)) {
            return subLevel;
        }
        return null;
    }

    static TreeManager get(Level level) {
        if(level.isClientSide()) {
            return TreePhysicsClient.TREE_MANAGER;
        } else {
            return ServerTreeManager.get((ServerLevel) level);
        }
    }
}
