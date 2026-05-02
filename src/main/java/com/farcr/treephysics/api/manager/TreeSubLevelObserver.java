package com.farcr.treephysics.api.manager;

import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelObserver;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import net.minecraft.server.level.ServerLevel;

public class TreeSubLevelObserver implements SubLevelObserver {

    private final ServerLevel level;

    public TreeSubLevelObserver(ServerLevel level) {
        this.level = level;
    }

    @Override
    public void tick(SubLevelContainer subLevels) {
        this.getTreeHandler().tick();
    }

    @Override
    public void onSubLevelRemoved(SubLevel subLevel, SubLevelRemovalReason reason) {
        if(reason == SubLevelRemovalReason.REMOVED) {
            this.getTreeHandler().unsetTree(subLevel);
        }
    }

    public ServerTreeManager getTreeHandler() {
        return ServerTreeManager.get(this.level);
    }
}
