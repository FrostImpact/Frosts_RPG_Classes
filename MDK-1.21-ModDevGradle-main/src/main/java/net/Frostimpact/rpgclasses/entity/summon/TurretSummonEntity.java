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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class TurretSummonEntity extends PathfinderMob implements RangedAttackMob {

    private Player owner;
    private double baseDamage = 4.0; // Stores owner's damage multiplier at spawn
    private int shootCooldown = 0;
    private static final int SHOOT_INTERVAL = 30; // Shoots every 1.5 seconds
    private static final int PARTICLES_PER_UNIT_DISTANCE = 2; // Particle density for beam trail
    private static final int MAX_BEAM_PARTICLES = 30; // Performance cap for particle trails
    private static final double TARGET_HIT_TOLERANCE = 0.5; // Floating point tolerance for hit detection
    private int decayTicks = 0;
    private static final int DECAY_START = 600; // 30 seconds
    private static final int DECAY_DAMAGE = 1; // 1 HP every 2 seconds
    private int decayDamageCooldown = 0;
    private int idleParticleTicks = 0;

    public TurretSummonEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomNameVisible(true);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.setCustomName(net.minecraft.network.chat.Component.literal("§7Turret"));
    }

    public void setBaseDamage(double damage) {
        this.baseDamage = damage;
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
        
        // Idle rotation particles
        idleParticleTicks++;
        if (!this.level().isClientSide && idleParticleTicks % 10 == 0) {
            if (this.level() instanceof ServerLevel serverLevel) {
                double angle = (idleParticleTicks * 0.1) % (2 * Math.PI);
                double radius = 0.5;
                double x = this.getX() + Math.cos(angle) * radius;
                double z = this.getZ() + Math.sin(angle) * radius;
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        x, this.getY() + 0.8, z,
                        1, 0, 0, 0, 0
                );
            }
        }
        
        // Handle decay after 30 seconds
        decayTicks++;
        if (decayTicks >= DECAY_START) {
            if (decayDamageCooldown <= 0) {
                this.hurt(this.damageSources().starve(), DECAY_DAMAGE);
                decayDamageCooldown = 40; // Damage every 2 seconds
                
                // Decay particles
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.SMOKE,
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
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // Muzzle flash at turret position
        serverLevel.sendParticles(
                ParticleTypes.FLAME,
                this.getX(), this.getY() + 0.8, this.getZ(),
                5, 0.1, 0.1, 0.1, 0.05
        );

        // Energy buildup effect
        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                this.getX(), this.getY() + 0.8, this.getZ(),
                3, 0.05, 0.05, 0.05, 0.02
        );

        // Calculate direction from turret to target
        Vec3 start = new Vec3(this.getX(), this.getY() + 0.8, this.getZ());
        Vec3 targetPos = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ());
        Vec3 direction = targetPos.subtract(start).normalize();
        double maxDistance = start.distanceTo(targetPos);
        Vec3 end = start.add(direction.scale(maxDistance));

        // Check for block collisions using proper raycast
        BlockHitResult blockHit = this.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
        ));

        // Determine actual hit distance
        double hitDistance = maxDistance;
        boolean blockedByWall = false;
        if (blockHit.getType() != HitResult.Type.MISS) {
            double blockDistance = start.distanceTo(blockHit.getLocation());
            if (blockDistance < maxDistance) {
                hitDistance = blockDistance;
                blockedByWall = true;
            }
        }

        // Check if we can hit the target (not blocked by walls)
        boolean hitTarget = false;
        
        if (!blockedByWall) {
            // More robust check: verify target is within shooting distance
            double distanceToTarget = start.distanceTo(targetPos);
            if (distanceToTarget <= hitDistance + TARGET_HIT_TOLERANCE) {
                hitTarget = true;
            }
        }

        // Spawn particle trail along the beam (only to hit point)
        int particleCount = Math.min((int) (hitDistance * PARTICLES_PER_UNIT_DISTANCE), MAX_BEAM_PARTICLES);
        for (int i = 0; i <= particleCount; i++) {
            double t = (double) i / particleCount;
            Vec3 particlePos = start.add(direction.scale(hitDistance * t));
            
            serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    particlePos.x, particlePos.y, particlePos.z,
                    1, 0, 0, 0, 0
            );
        }

        // Apply damage if we hit the target
        if (hitTarget) {
            target.hurt(this.damageSources().mobProjectile(this, this), (float) baseDamage);
            
            // Impact particles
            serverLevel.sendParticles(
                    ParticleTypes.CRIT_HIT,
                    targetPos.x, targetPos.y, targetPos.z,
                    8, 0.2, 0.2, 0.2, 0.1
            );
        } else if (blockedByWall) {
            // Wall impact particles
            Vec3 wallHitPos = blockHit.getLocation();
            serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    wallHitPos.x, wallHitPos.y, wallHitPos.z,
                    5, 0.1, 0.1, 0.1, 0.02
            );
        }

        // Sound effect
        this.playSound(SoundEvents.SNOWBALL_THROW, 1.0f, 1.5f);

        shootCooldown = SHOOT_INTERVAL;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) {
            tag.putUUID("owner", owner.getUUID());
        }
        tag.putInt("shootCooldown", shootCooldown);
        tag.putInt("decayTicks", decayTicks);
        tag.putDouble("baseDamage", baseDamage);
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
        if (tag.contains("baseDamage")) {
            baseDamage = tag.getDouble("baseDamage");
        }
    }
}
