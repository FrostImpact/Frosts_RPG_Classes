package net.Frostimpact.rpgclasses.entity.summon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import java.util.EnumSet; // Required for Goal Flags

public class ArcherSummonEntity extends PathfinderMob implements RangedAttackMob {

    private Player owner;
    private int shootCooldown = 0;
    private static final int SHOOT_INTERVAL = 40;

    public ArcherSummonEntity(EntityType<? extends PathfinderMob> type, Level level) {
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
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0, 40, 16.0f));

        // Priority 3: Follow Owner.
        // Note: min distance increased to 5.0 to give it space to breathe
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 20.0f, 5.0f));

        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.STEP_HEIGHT, 1.0); // Ensures it doesn't get stuck on carpet/snow
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

    @Override
    public void performRangedAttack(net.minecraft.world.entity.LivingEntity target, float velocity) {
        if (shootCooldown > 0) return;
        if (target == null) return;

        Arrow arrow = new Arrow(this.level(), this, new ItemStack(Items.ARROW), null);
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
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

    // --- FIXED GOAL ---
    private static class FollowOwnerGoal extends Goal {
        private final ArcherSummonEntity mob;
        private final double speedModifier;
        private final float maxDist;
        private final float minDist;
        private int timeToRecalcPath;

        public FollowOwnerGoal(ArcherSummonEntity mob, double speed, float maxDist, float minDist) {
            this.mob = mob;
            this.speedModifier = speed;
            this.maxDist = maxDist;
            this.minDist = minDist;

            // THIS IS THE FIX:
            // Locking MOVE and LOOK ensures RandomStroll and LookAtPlayer don't run
            // at the same time as this goal.
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Player owner = mob.getOwner();
            if (owner == null) return false;
            if (mob.distanceTo(owner) < minDist) return false; // Too close, don't move
            return mob.distanceTo(owner) > maxDist; // Too far, start moving
        }

        @Override
        public boolean canContinueToUse() {
            Player owner = mob.getOwner();
            if (owner == null) return false;

            // Determine navigation status
            if (mob.getNavigation().isDone()) return false;

            // Keep running until we are fairly close (minDist), not just within maxDist
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
                // Force Mob to look at owner while following
                mob.getLookControl().setLookAt(owner, 10.0f, mob.getMaxHeadXRot());

                if (--this.timeToRecalcPath <= 0) {
                    this.timeToRecalcPath = 10; // Update path every 0.5 seconds
                    mob.getNavigation().moveTo(owner, speedModifier);
                }
            }
        }
    }
}