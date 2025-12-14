package net.Frostimpact.rpgclasses.entity.summon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public class TurretSummonEntity extends PathfinderMob implements RangedAttackMob {

    private Player owner;
    private int shootCooldown = 0;
    private static final int SHOOT_INTERVAL = 30; // Shoots every 1.5 seconds
    private int decayTicks = 0;
    private static final int DECAY_START = 600; // 30 seconds
    private static final int DECAY_DAMAGE = 1; // 1 HP every 2 seconds
    private int decayDamageCooldown = 0;

    public TurretSummonEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomNameVisible(true);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.setCustomName(net.minecraft.network.chat.Component.literal("§7Turret"));
    }

    public Player getOwner() {
        return owner;
    }

    @Override
    protected void registerGoals() {
        // Stationary - no movement goals
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Monster.class, 16.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        
        // No target selector - we'll handle targeting manually in tick()
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0) // Stationary
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0); // Cannot be knocked back
    }

    @Override
    public void tick() {
        super.tick();
        
        // Make truly stationary by setting motion to zero
        this.setDeltaMovement(Vec3.ZERO);
        
        if (shootCooldown > 0) {
            shootCooldown--;
        }
        
        // Handle decay after 30 seconds
        decayTicks++;
        if (decayTicks >= DECAY_START) {
            if (decayDamageCooldown <= 0) {
                this.hurt(this.damageSources().starve(), DECAY_DAMAGE);
                decayDamageCooldown = 40; // Damage every 2 seconds
                
                // Decay particles
                if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.SMOKE,
                            this.getX(), this.getY() + 0.5, this.getZ(),
                            3, 0.2, 0.2, 0.2, 0.01
                    );
                }
            } else {
                decayDamageCooldown--;
            }
        }
        
        // Target and shoot nearest enemy
        if (!this.level().isClientSide && shootCooldown <= 0) {
            Monster target = this.level().getNearestEntity(
                    Monster.class,
                    net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat().range(16.0),
                    this,
                    this.getX(), this.getY(), this.getZ(),
                    this.getBoundingBox().inflate(16.0)
            );
            
            if (target != null) {
                this.performRangedAttack(target, 1.0f);
            }
        }
    }

    @Override
    public void performRangedAttack(net.minecraft.world.entity.LivingEntity target, float velocity) {
        if (shootCooldown > 0) return;
        if (target == null) return;

        // Shoot a small fireball (pellet)
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5) - this.getY(0.5);
        double dz = target.getZ() - this.getZ();
        
        SmallFireball fireball = new SmallFireball(this.level(), this, dx, dy, dz);
        fireball.setPos(this.getX(), this.getY() + 0.5, this.getZ());
        
        this.level().addFreshEntity(fireball);
        this.playSound(SoundEvents.BLAZE_SHOOT, 0.5f, 1.5f);

        shootCooldown = SHOOT_INTERVAL;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Check if healing from owner (RESTORATION passive)
        if (source.getEntity() != null && source.getEntity().equals(owner)) {
            // This is handled by the passive handler
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
        tag.putInt("shootCooldown", shootCooldown);
        tag.putInt("decayTicks", decayTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("shootCooldown")) {
            shootCooldown = tag.getInt("shootCooldown");
        }
        if (tag.contains("decayTicks")) {
            decayTicks = tag.getInt("decayTicks");
        }
    }
}
