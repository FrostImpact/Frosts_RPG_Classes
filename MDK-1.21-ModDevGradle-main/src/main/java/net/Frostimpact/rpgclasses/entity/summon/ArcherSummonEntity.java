package net.Frostimpact.rpgclasses.entity.summon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;

public class ArcherSummonEntity extends Mob {

    private Player owner;
    private int shootCooldown = 0;
    private static final int SHOOT_INTERVAL = 40; // 2 seconds

    public ArcherSummonEntity(EntityType<? extends Mob> type, Level level) {
        super(type, level);
        this.setCustomNameVisible(true);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.setCustomName(net.minecraft.network.chat.Component.literal("§aArcher"));
    }

    public Player getOwner() {
        return owner;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 0.8, 15.0f, 8.0f));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tick() {
        super.tick();
        
        if (shootCooldown > 0) {
            shootCooldown--;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.getPersistentData().getBoolean("demoralized")) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public void performRangedAttack() {
        if (shootCooldown > 0) return;
        if (this.getTarget() == null) return;

        // Shoot arrow
        Arrow arrow = new Arrow(this.level(), this, null);
        double dx = this.getTarget().getX() - this.getX();
        double dy = this.getTarget().getY(0.3333333333333333) - arrow.getY();
        double dz = this.getTarget().getZ() - this.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        arrow.shoot(dx, dy + dist * 0.2, dz, 1.6f, 14.0f);
        arrow.setBaseDamage(3.0);

        this.level().addFreshEntity(arrow);
        this.playSound(SoundEvents.ARROW_SHOOT, 1.0f, 1.0f / (this.getRandom().nextFloat() * 0.4f + 0.8f));

        shootCooldown = SHOOT_INTERVAL;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) {
            tag.putUUID("owner", owner.getUUID());
        }
        tag.putInt("shootCooldown", shootCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("shootCooldown")) {
            shootCooldown = tag.getInt("shootCooldown");
        }
    }

    // Custom ranged attack goal
    private static class RangedAttackGoal extends Goal {
        private final ArcherSummonEntity archer;

        public RangedAttackGoal(ArcherSummonEntity archer) {
            this.archer = archer;
        }

        @Override
        public boolean canUse() {
            return archer.getTarget() != null && archer.shootCooldown == 0;
        }

        @Override
        public void tick() {
            if (archer.getTarget() != null) {
                archer.getLookControl().setLookAt(archer.getTarget(), 30.0f, 30.0f);
                
                double dist = archer.distanceTo(archer.getTarget());
                if (dist < 16.0 && archer.shootCooldown == 0) {
                    archer.performRangedAttack();
                } else if (dist > 8.0) {
                    archer.getNavigation().moveTo(archer.getTarget(), 0.8);
                }
            }
        }
    }

    // Follow owner goal
    private static class FollowOwnerGoal extends Goal {
        private final ArcherSummonEntity archer;
        private final double speedModifier;
        private final float maxDist;
        private final float minDist;

        public FollowOwnerGoal(ArcherSummonEntity archer, double speed, float maxDist, float minDist) {
            this.archer = archer;
            this.speedModifier = speed;
            this.maxDist = maxDist;
            this.minDist = minDist;
        }

        @Override
        public boolean canUse() {
            Player owner = archer.getOwner();
            if (owner == null) return false;
            if (archer.distanceTo(owner) < minDist) return false;
            return archer.distanceTo(owner) > maxDist;
        }

        @Override
        public void start() {
            Player owner = archer.getOwner();
            if (owner != null) {
                archer.getNavigation().moveTo(owner, speedModifier);
            }
        }

        @Override
        public void stop() {
            archer.getNavigation().stop();
        }

        @Override
        public void tick() {
            Player owner = archer.getOwner();
            if (owner != null) {
                archer.getLookControl().setLookAt(owner, 10.0f, archer.getMaxHeadXRot());
                if (archer.distanceTo(owner) > maxDist * 0.75) {
                    archer.getNavigation().moveTo(owner, speedModifier);
                }
            }
        }
    }
}