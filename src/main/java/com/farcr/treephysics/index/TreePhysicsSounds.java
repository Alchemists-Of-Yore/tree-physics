package com.farcr.treephysics.index;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

import static com.farcr.treephysics.TreePhysics.path;

public class TreePhysicsSounds {
    public static final Holder<SoundEvent> TREE_CREAK = create("sub_level.tree.creak");
    public static final Holder<SoundEvent> TREE_IMPACT = create("sub_level.tree.impact");
    public static final Holder<SoundEvent> LEAF_RUSTLE = create("sub_level.tree.leaf_rustle");

    private static Holder<SoundEvent> create(String id) {
        return Holder.direct(SoundEvent.createVariableRangeEvent(path(id)));
    }
}
