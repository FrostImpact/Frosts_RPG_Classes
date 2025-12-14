package net.Frostimpact.rpgclasses.entity.summon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ShockTowerEntity extends PathfinderMob {

    private Player owner;
    private double baseDamage = 3.0; // Stores owner's damage multiplier at spawn
    private int pulseCooldown = 0;
    private static final int PULSE_INTERVAL = 60; // Pulse every 3 seconds
    private static final double PULSE_RADIUS = 8.0;
    private int decayTicks = 0;
    private static final int DECAY_START = 600; // 30 seconds
    private static final int DECAY_DAMAGE = 1;
    private int decayDamageCooldown = 0;
    private int idleParticleTicks = 0;

    public ShockTowerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomNameVisible(true);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.setCustomName(net.minecraft.network.chat.Component.literal("§bShock Tower"));
    }

    public Player getOwner() {
        return owner;
    }

    public void setBaseDamage(double damage) {
        this.baseDamage = damage;
    }

    @Override
    protected void registerGoals() {
        // Stationary - minimal goals
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Monster.class, 16.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0) // Stationary
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    public void tick() {
        super.tick();
        
        // Make truly stationary
        this.setDeltaMovement(Vec3.ZERO);
        
        if (pulseCooldown > 0) {
            pulseCooldown--;
        }
        
        // Idle electric arc particles
        idleParticleTicks++;
        if (!this.level().isClientSide && idleParticleTicks % 15 == 0) {
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                // Random electric sparks
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + (random.nextDouble() - 0.5) * 0.5,
                        this.getY() + 0.5 + random.nextDouble() * 0.5,
                        this.getZ() + (random.nextDouble() - 0.5) * 0.5,
                        1, 0, 0, 0, 0
                );
            }
        }
        
        // Handle decay
        decayTicks++;
        if (decayTicks >= DECAY_START) {
            if (decayDamageCooldown <= 0) {
                this.hurt(this.damageSources().starve(), DECAY_DAMAGE);
                decayDamageCooldown = 40;
                
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
        
        // Pulse shock wave
        if (!this.level().isClientSide && pulseCooldown <= 0) {
            pulseShockWave();
            pulseCooldown = PULSE_INTERVAL;
        }
    }

    private void pulseShockWave() {
        // Find all monsters in range
        List<Monster> monsters = this.level().getEntitiesOfClass(
                Monster.class,
                this.getBoundingBox().inflate(PULSE_RADIUS),
                entity -> entity.isAlive()
        );
        
        // Apply slowness and damage
        for (Monster monster : monsters) {
            monster.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    80, // 4 seconds
                    1, // Slowness II
                    false,
                    false,
                    true
            ));
            
            // Apply scaled damage
            monster.hurt(this.damageSources().mobAttack(this), (float) baseDamage);
        }
        
        if (!monsters.isEmpty()) {
            // Enhanced visual and audio effects
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                // Electric arcs to each target
                for (Monster monster : monsters) {
                    Vec3 direction = monster.position().subtract(this.position()).normalize();
                    double distance = this.position().distanceTo(monster.position());
                    int arcParticles = Math.min((int) (distance * 3), 20); // Cap at 20 particles for performance
                    
                    for (int i = 0; i < arcParticles; i++) {
                        double t = (double) i / arcParticles;
                        Vec3 pos = this.position().add(direction.scale(distance * t));
                        serverLevel.sendParticles(
                                net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                                pos.x, pos.y + 0.5, pos.z,
                                1, 0.1, 0.1, 0.1, 0
                        );
                    }
                }
                
                // Ring particles
                for (int i = 0; i < 40; i++) {
                    double angle = (2 * Math.PI * i) / 40;
                    double x = this.getX() + Math.cos(angle) * PULSE_RADIUS;
                    double z = this.getZ() + Math.sin(angle) * PULSE_RADIUS;
                    
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                            x, this.getY() + 0.5, z,
                            2, 0, 0.3, 0, 0.05
                    );
                }
                
                // Central burst with lingering sparks
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        1, 0, 0, 0, 0
                );
                
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        15, 0.3, 0.3, 0.3, 0.1
                );
            }
            
            this.playSound(SoundEvents.BEACON_ACTIVATE, 0.5f, 1.5f);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() != null && source.getEntity().equals(owner)) {
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
        tag.putInt("pulseCooldown", pulseCooldown);
        tag.putInt("decayTicks", decayTicks);
        tag.putDouble("baseDamage", baseDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("pulseCooldown")) {
            pulseCooldown = tag.getInt("pulseCooldown");
        }
        if (tag.contains("decayTicks")) {
            decayTicks = tag.getInt("decayTicks");
        }
        if (tag.contains("baseDamage")) {
            baseDamage = tag.getDouble("baseDamage");
        }
    }
}
