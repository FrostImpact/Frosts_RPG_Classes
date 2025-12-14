package net.Frostimpact.rpgclasses.entity.summon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import java.util.EnumSet; // Required

public class KnightSummonEntity extends PathfinderMob {

    private Player owner;

    public KnightSummonEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomNameVisible(true);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.setCustomName(net.minecraft.network.chat.Component.literal("§6Knight"));
    }

    public Player getOwner() {
        return owner;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, false));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0f, 3.0f));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.getPersistentData().getBoolean("demoralized")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) {
            tag.putUUID("owner", owner.getUUID());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Load owner logic
    }

    // --- FIXED GOAL ---
    private static class FollowOwnerGoal extends Goal {
        private final KnightSummonEntity mob;
        private final double speedModifier;
        private final float maxDist;
        private final float minDist;
        private int timeToRecalcPath;

        public FollowOwnerGoal(KnightSummonEntity mob, double speed, float maxDist, float minDist) {
            this.mob = mob;
            this.speedModifier = speed;
            this.maxDist = maxDist;
            this.minDist = minDist;
            // FIX: Set Mutex Flags to prevent conflict with other movement goals
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Player owner = mob.getOwner();
            if (owner == null) return false;
            if (mob.distanceTo(owner) < minDist) return false;
            return mob.distanceTo(owner) > maxDist;
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = mob.getOwner();
            if (owner == null) return false;
            if (mob.getNavigation().isDone()) return false;
            return mob.distanceTo(owner) > minDist;
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            mob.getNavigation().stop();
        }

        @Override
        public void tick() {
            Player owner = mob.getOwner();
            if (owner != null) {
                mob.getLookControl().setLookAt(owner, 10.0f, mob.getMaxHeadXRot());
                if (--this.timeToRecalcPath <= 0) {
                    this.timeToRecalcPath = 10;
                    mob.getNavigation().moveTo(owner, speedModifier);
                }
            }
        }
    }
}