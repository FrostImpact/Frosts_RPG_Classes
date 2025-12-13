package net.Frostimpact.rpgclasses.entity.summon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;

public class KnightSummonEntity extends Mob {

    private Player owner;

    public KnightSummonEntity(EntityType<? extends Mob> type, Level level) {
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
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Check if DEMORALIZED
        if (this.getPersistentData().getBoolean("demoralized")) {
            // Can't take actions while demoralized
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
        if (tag.hasUUID("owner")) {
            // Owner will be set when player loads
        }
    }

    // Custom goal to follow owner
    private static class FollowOwnerGoal extends Goal {
        private final KnightSummonEntity knight;
        private final double speedModifier;
        private final float maxDist;
        private final float minDist;

        public FollowOwnerGoal(KnightSummonEntity knight, double speed, float maxDist, float minDist) {
            this.knight = knight;
            this.speedModifier = speed;
            this.maxDist = maxDist;
            this.minDist = minDist;
        }

        @Override
        public boolean canUse() {
            Player owner = knight.getOwner();
            if (owner == null) return false;
            if (knight.distanceTo(owner) < minDist) return false;
            return knight.distanceTo(owner) > maxDist;
        }

        @Override
        public void start() {
            Player owner = knight.getOwner();
            if (owner != null) {
                knight.getNavigation().moveTo(owner, speedModifier);
            }
        }

        @Override
        public void stop() {
            knight.getNavigation().stop();
        }

        @Override
        public void tick() {
            Player owner = knight.getOwner();
            if (owner != null) {
                knight.getLookControl().setLookAt(owner, 10.0f, knight.getMaxHeadXRot());
                if (knight.distanceTo(owner) > maxDist * 0.75) {
                    knight.getNavigation().moveTo(owner, speedModifier);
                }
            }
        }
    }
}