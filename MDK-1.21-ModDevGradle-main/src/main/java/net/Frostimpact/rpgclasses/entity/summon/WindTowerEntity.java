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
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class WindTowerEntity extends PathfinderMob {

    private Player owner;
    private int pulseCooldown = 0;
    private static final int PULSE_INTERVAL = 60; // Pulse every 3 seconds
    private static final double PULSE_RADIUS = 8.0;
    private static final double KNOCKBACK_STRENGTH = 1.5;
    private int decayTicks = 0;
    private static final int DECAY_START = 600; // 30 seconds
    private static final int DECAY_DAMAGE = 1;
    private int decayDamageCooldown = 0;

    public WindTowerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomNameVisible(true);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.setCustomName(net.minecraft.network.chat.Component.literal("§aWind Tower"));
    }

    public Player getOwner() {
        return owner;
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
        
        // Pulse wind wave
        if (!this.level().isClientSide && pulseCooldown <= 0) {
            pulseWindWave();
            pulseCooldown = PULSE_INTERVAL;
        }
    }

    private void pulseWindWave() {
        // Find all monsters in range
        List<Monster> monsters = this.level().getEntitiesOfClass(
                Monster.class,
                this.getBoundingBox().inflate(PULSE_RADIUS),
                entity -> entity.isAlive()
        );
        
        // Apply knockback
        for (Monster monster : monsters) {
            Vec3 direction = monster.position().subtract(this.position()).normalize();
            Vec3 knockback = direction.multiply(KNOCKBACK_STRENGTH, 0.5, KNOCKBACK_STRENGTH);
            monster.setDeltaMovement(monster.getDeltaMovement().add(knockback));
            monster.hurtMarked = true;
        }
        
        if (!monsters.isEmpty()) {
            // Visual and audio effects
            if (this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                // Ring particles
                for (int i = 0; i < 40; i++) {
                    double angle = (2 * Math.PI * i) / 40;
                    double x = this.getX() + Math.cos(angle) * PULSE_RADIUS;
                    double z = this.getZ() + Math.sin(angle) * PULSE_RADIUS;
                    
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.CLOUD,
                            x, this.getY() + 0.5, z,
                            2, 0, 0, 0, 0.1
                    );
                }
                
                // Central burst
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.GUST,
                        this.getX(), this.getY() + 0.5, this.getZ(),
                        10, 0.5, 0.5, 0.5, 0.1
                );
            }
            
            //this.playSound(SoundEvents.BREEZE_WIND_BURST, 0.5f, 1.2f);
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
    }
}
