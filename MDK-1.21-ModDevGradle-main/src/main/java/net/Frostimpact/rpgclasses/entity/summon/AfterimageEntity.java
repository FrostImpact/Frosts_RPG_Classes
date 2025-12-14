package net.Frostimpact.rpgclasses.entity.summon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class AfterimageEntity extends PathfinderMob {

    private static final int LIFETIME_AFTER_TELEPORT_TICKS = 80; // 4 seconds
    private static final double MAX_DISTANCE_FROM_OWNER = 20.0;
    private static final double DEFAULT_MAX_GLIDE_DISTANCE = 10.0;
    private static final double MOVEMENT_LERP_FACTOR = 0.8; // Increased for smoother movement
    private static final double GLIDE_SPEED = 0.15; // Reduced for more natural walking speed
    private static final int PARTICLE_SPAWN_INTERVAL = 1; // Spawn particles every tick for better outline
    
    // Particle outline dimensions - enhanced for better player representation
    private static final double OUTLINE_TORSO_RADIUS = 0.3;
    private static final double OUTLINE_HEAD_RADIUS = 0.25;
    private static final double OUTLINE_HEAD_HEIGHT = 1.6;
    private static final double OUTLINE_ARM_WIDTH = 0.3;
    private static final double OUTLINE_ARM_EXTEND = 0.6;
    private static final double OUTLINE_LEG_WIDTH = 0.15;
    private static final double OUTLINE_SHOULDER_HEIGHT = 1.2;

    private Player owner;
    private Vec3 glideDirection = Vec3.ZERO;
    private boolean isGliding = false;
    private int lifetimeAfterTeleport = -1; // -1 means no timer, otherwise counts down from LIFETIME_AFTER_TELEPORT_TICKS
    private Vec3 glideStartPosition = Vec3.ZERO;
    private double maxGlideDistance = DEFAULT_MAX_GLIDE_DISTANCE;
    
    // For smooth movement
    private Vec3 previousPosition = Vec3.ZERO;
    private Vec3 targetPosition = Vec3.ZERO;
    private boolean hasTargetPosition = false;
    
    // For swing animation
    private int swingTime = 0;
    private int swingDuration = 6; // Duration of swing animation in ticks
    
    // For particle optimization
    private int particleSpawnTick = 0;

    public AfterimageEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomNameVisible(true);
        this.setNoGravity(true); // Afterimages float/glide
        this.previousPosition = this.position();
        this.targetPosition = this.position();
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.setCustomName(net.minecraft.network.chat.Component.literal("§9Afterimage"));
        
        // Set HP to 50% of owner's max HP
        if (owner != null) {
            float ownerMaxHP = owner.getMaxHealth();
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(ownerMaxHP * 0.5);
            this.setHealth(ownerMaxHP * 0.5f);
        }
    }

    public Player getOwner() {
        return owner;
    }

    public void setGlideDirection(Vec3 direction) {
        this.glideDirection = direction;
        this.isGliding = true;
        this.glideStartPosition = this.position();
        this.previousPosition = this.position();
    }
    
    public void performSwingAnimation() {
        this.swingTime = swingDuration;
    }
    
    public boolean isSwinging() {
        return swingTime > 0;
    }

    public boolean isGliding() {
        return isGliding;
    }

    public void stopGliding() {
        this.isGliding = false;
        this.glideDirection = Vec3.ZERO;
    }

    public void startLifetimeTimer() {
        this.lifetimeAfterTeleport = LIFETIME_AFTER_TELEPORT_TICKS;
    }

    public boolean hasLifetimeTimer() {
        return lifetimeAfterTeleport >= 0;
    }

    public int getLifetimeAfterTeleport() {
        return lifetimeAfterTeleport;
    }

    @Override
    protected void registerGoals() {
        // Afterimages have no AI goals - they're controlled by abilities
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0) // Default, will be set based on owner
                .add(Attributes.MOVEMENT_SPEED, 0.0) // Controlled by gliding
                .add(Attributes.FOLLOW_RANGE, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0); // Immune to knockback
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Check if owner is too far away
            if (owner != null && !owner.isRemoved()) {
                double distanceToOwner = this.distanceTo(owner);
                if (distanceToOwner > MAX_DISTANCE_FROM_OWNER) {
                    this.discard();
                    return;
                }
            } else if (owner == null || owner.isRemoved()) {
                // Owner doesn't exist, remove afterimage
                this.discard();
                return;
            }

            // Handle lifetime timer after teleport
            if (lifetimeAfterTeleport >= 0) {
                lifetimeAfterTeleport--;
                if (lifetimeAfterTeleport <= 0) {
                    this.discard();
                    return;
                }
            }

            // Handle gliding movement with smooth interpolation for natural walking
            if (isGliding && glideDirection.lengthSqr() > 0) {
                // Check if we've traveled more than max distance
                double distanceTraveled = this.position().distanceTo(glideStartPosition);
                if (distanceTraveled >= maxGlideDistance) {
                    stopGliding();
                } else {
                    // Calculate smooth movement with natural walking speed
                    Vec3 movement = glideDirection.normalize().scale(GLIDE_SPEED);
                    Vec3 newPos = this.position().add(movement);
                    this.setPos(newPos.x, newPos.y, newPos.z);
                    
                    // Update velocity for smooth rendering
                    this.setDeltaMovement(movement);
                    
                    // Check for collision/wall
                    if (this.horizontalCollision || this.verticalCollision) {
                        stopGliding();
                    }
                }
            } else if (isGliding) {
                stopGliding();
                this.setDeltaMovement(Vec3.ZERO);
            }
            
            // Spawn particle outline to create ghostly humanoid silhouette (optimized to reduce performance impact)
            particleSpawnTick++;
            if (particleSpawnTick >= PARTICLE_SPAWN_INTERVAL) {
                particleSpawnTick = 0;
                if (this.level() instanceof ServerLevel serverLevel) {
                    spawnParticleOutline(serverLevel);
                }
            }
            
            // Handle swing animation countdown
            if (swingTime > 0) {
                swingTime--;
            }
        }
    }
    
    private void spawnParticleOutline(ServerLevel serverLevel) {
        // Create an enhanced humanoid particle outline using soul fire particles
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        
        // Body particles (torso) - more particles for better definition
        for (int i = 0; i < 5; i++) {
            double offsetY = 0.5 + i * 0.2;
            spawnCircleParticles(serverLevel, x, y + offsetY, z, OUTLINE_TORSO_RADIUS, 6);
        }
        
        // Head particles - more detailed
        spawnCircleParticles(serverLevel, x, y + OUTLINE_HEAD_HEIGHT, z, OUTLINE_HEAD_RADIUS, 8);
        spawnCircleParticles(serverLevel, x, y + OUTLINE_HEAD_HEIGHT - 0.1, z, OUTLINE_HEAD_RADIUS, 6);
        spawnCircleParticles(serverLevel, x, y + OUTLINE_HEAD_HEIGHT - 0.2, z, OUTLINE_HEAD_RADIUS - 0.05, 6);
        
        // Shoulders
        spawnCircleParticles(serverLevel, x - OUTLINE_ARM_WIDTH, y + OUTLINE_SHOULDER_HEIGHT, z, 0.1, 3);
        spawnCircleParticles(serverLevel, x + OUTLINE_ARM_WIDTH, y + OUTLINE_SHOULDER_HEIGHT, z, 0.1, 3);
        
        // Arms particles - more detailed
        spawnLineParticles(serverLevel, x - OUTLINE_ARM_WIDTH, y + OUTLINE_SHOULDER_HEIGHT, z, x - OUTLINE_ARM_EXTEND, y + 0.6, z, 4);
        spawnLineParticles(serverLevel, x + OUTLINE_ARM_WIDTH, y + OUTLINE_SHOULDER_HEIGHT, z, x + OUTLINE_ARM_EXTEND, y + 0.6, z, 4);
        
        // Legs particles - more detailed for better visibility
        spawnLineParticles(serverLevel, x - OUTLINE_LEG_WIDTH, y + 0.5, z, x - OUTLINE_LEG_WIDTH, y, z, 4);
        spawnLineParticles(serverLevel, x + OUTLINE_LEG_WIDTH, y + 0.5, z, x + OUTLINE_LEG_WIDTH, y, z, 4);
        
        // Feet
        spawnCircleParticles(serverLevel, x - OUTLINE_LEG_WIDTH, y, z, 0.1, 3);
        spawnCircleParticles(serverLevel, x + OUTLINE_LEG_WIDTH, y, z, 0.1, 3);
    }
    
    private void spawnCircleParticles(ServerLevel serverLevel, double x, double y, double z, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                x + offsetX, y, z + offsetZ,
                1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }
    
    private void spawnLineParticles(ServerLevel serverLevel, double x1, double y1, double z1, double x2, double y2, double z2, int count) {
        for (int i = 0; i <= count; i++) {
            double t = (double) i / count;
            double x = x1 + (x2 - x1) * t;
            double y = y1 + (y2 - y1) * t;
            double z = z1 + (z2 - z1) * t;
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                x, y, z,
                1, 0.0, 0.0, 0.0, 0.0
            );
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Afterimages can be damaged by enemies
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) {
            tag.putUUID("owner", owner.getUUID());
        }
        tag.putBoolean("isGliding", isGliding);
        tag.putDouble("glideX", glideDirection.x);
        tag.putDouble("glideY", glideDirection.y);
        tag.putDouble("glideZ", glideDirection.z);
        tag.putInt("lifetimeAfterTeleport", lifetimeAfterTeleport);
        tag.putDouble("glideStartX", glideStartPosition.x);
        tag.putDouble("glideStartY", glideStartPosition.y);
        tag.putDouble("glideStartZ", glideStartPosition.z);
        tag.putDouble("maxGlideDistance", maxGlideDistance);
        tag.putInt("swingTime", swingTime);
        tag.putInt("particleSpawnTick", particleSpawnTick);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("owner")) {
            java.util.UUID ownerUUID = tag.getUUID("owner");
            if (!this.level().isClientSide) {
                // Find the owner player by UUID
                net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) this.level();
                this.owner = serverLevel.getPlayerByUUID(ownerUUID);
            }
        }
        if (tag.contains("isGliding")) {
            this.isGliding = tag.getBoolean("isGliding");
        }
        if (tag.contains("glideX")) {
            double x = tag.getDouble("glideX");
            double y = tag.getDouble("glideY");
            double z = tag.getDouble("glideZ");
            this.glideDirection = new Vec3(x, y, z);
        }
        if (tag.contains("lifetimeAfterTeleport")) {
            this.lifetimeAfterTeleport = tag.getInt("lifetimeAfterTeleport");
        }
        if (tag.contains("glideStartX")) {
            double x = tag.getDouble("glideStartX");
            double y = tag.getDouble("glideStartY");
            double z = tag.getDouble("glideStartZ");
            this.glideStartPosition = new Vec3(x, y, z);
        }
        if (tag.contains("maxGlideDistance")) {
            this.maxGlideDistance = tag.getDouble("maxGlideDistance");
        }
        if (tag.contains("swingTime")) {
            this.swingTime = tag.getInt("swingTime");
        }
        if (tag.contains("particleSpawnTick")) {
            this.particleSpawnTick = tag.getInt("particleSpawnTick");
        }
    }
}
