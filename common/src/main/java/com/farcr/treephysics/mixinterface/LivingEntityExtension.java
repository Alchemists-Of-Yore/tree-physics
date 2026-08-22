package com.farcr.treephysics.mixinterface;

import com.farcr.treephysics.index.TreePhysicsConfig;
import com.farcr.treephysics.index.TreePhysicsDamageTypes;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public interface LivingEntityExtension {
    boolean treephysics$wasHitByTree();
    long treephysics$getPanicTimestamp();
    void treephysics$setPanicTimestamp(long panicTimestamp);

    static int doDamageAndKnockback(SubLevel subLevel, BlockPos blockPos, LivingEntity entity) {
        Vec3 last = subLevel.lastPose().transformPosition(blockPos.getCenter());
        Vec3 pos = subLevel.logicalPose().transformPosition(blockPos.getCenter());
        Vec3 velocity = pos.subtract(last);

        double length = Math.min(2.0, velocity.length());
        float damage = (float) (length * TreePhysicsConfig.TREE_ENTITY_DAMAGE.getAsDouble());

        if(damage > 0.1) {
            Pose3d pose = subLevel.logicalPose();
            Vector3d localPos = pose.transformPositionInverse(new Vector3d(entity.position().toVector3f()));
            Vector3d localCom = pose.transformPositionInverse(new Vector3d(pose.position()));

            localCom.y = localPos.y;

            Vector3d force = localPos.sub(localCom, new Vector3d())
                    .normalize();

            pose.transformNormal(force)
                    .mul(1, 0, 1)
                    .normalize()
                    .mul(1.6);

            double knockback = Math.min(1.3, length);
            entity.addDeltaMovement(new Vec3(force.x, knockback, force.z));

            DamageSource source = entity.damageSources().source(TreePhysicsDamageTypes.TREE_CRUSHING);
            entity.hurt(source, damage);
            return 4;
        }

        return 0;
    }
}
