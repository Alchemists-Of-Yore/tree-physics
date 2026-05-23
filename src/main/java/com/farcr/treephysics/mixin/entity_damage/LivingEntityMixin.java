package com.farcr.treephysics.mixin.entity_damage;

import com.farcr.treephysics.api.util.TreeUtil;
import com.farcr.treephysics.client.TreeManager;
import com.farcr.treephysics.index.TreePhysicsConfig;
import com.farcr.treephysics.mixinterface.LivingEntityExtension;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExtension {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private static final BoundingBox3d treephysics$BOX = new BoundingBox3d();

    @Unique
    private int treephysics$damageCooldown = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void treephysics$tick(CallbackInfo ci) {
        if(this.treephysics$damageCooldown > 0) {
            this.treephysics$damageCooldown--;
        } else {
            if(TreePhysicsConfig.TREE_ENTITY_DAMAGE.getAsDouble() == 0.0) return;
            treephysics$BOX.set(this.getBoundingBox());
            Level level = this.level();

            Iterable<SubLevel> intersecting = Sable.HELPER.getAllIntersecting(level, treephysics$BOX);
            TreeManager manager = TreeManager.get(level);

            for (SubLevel subLevel : intersecting) {
                if(manager.isTree(subLevel) && Sable.HELPER.getTrackingSubLevel(this) != subLevel) {
                    Vec3 localEyePos = subLevel.logicalPose().transformPositionInverse(this.getEyePosition());
                    BlockPos blockPos = BlockPos.containing(localEyePos);

                    BlockState state = level.getBlockState(blockPos);
                    if(TreeUtil.isLog(state)) {
                        this.treephysics$damageCooldown += LivingEntityExtension.doDamageAndKnockback(subLevel, blockPos, (LivingEntity) (Object) this);
                    }

                    break;
                }
            }
        }
    }

    @Override
    public boolean treephysics$wasHitByTree() {
        return this.treephysics$damageCooldown > 0;
    }
}
