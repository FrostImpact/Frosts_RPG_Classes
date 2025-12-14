package net.Frostimpact.rpgclasses.entity.summon;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import org.joml.Vector3f;

import java.util.List;

public class AfterimageEntity extends PathfinderMob {

    private static final int LIFETIME_AFTER_TELEPORT_TICKS = 120;
    private static final int INVISIBILITY_DURATION_TICKS = 200;
    private static final double MAX_DISTANCE_FROM_OWNER = 20.0;
    private static final double DEFAULT_MAX_GLIDE_DISTANCE = 10.0;
    private static final double GLIDE_SPEED = 0.15;
    private static final int PARTICLE_SPAWN_INTERVAL = 1;

    // --- DIMENSIONS ---
    private static final double HEAD_RADIUS = 0.20;
    private static final double HEAD_HEIGHT_SIZE = 0.40;
    private static final double TORSO_WIDTH = 0.45;
    private static final double TORSO_HEIGHT = 0.70;
    private static final double TORSO_DEPTH = 0.20;
    private static final double LIMB_WIDTH = 0.20;
    private static final double LIMB_HEIGHT = 0.70;
    private static final double LIMB_DEPTH = 0.20;
    private static final double HEAD_BASE_Y = 1.45;
    private static final double SHOULDER_Y = 1.35;
    private static final double HIP_Y = 0.65;
    private static final double ARM_OFFSET = 0.35;
    private static final double LEG_OFFSET = 0.11;

    private static final Vector3f LIGHT_BLUE_COLOR = new Vector3f(0.39f, 0.78f, 1.0f);
    private static final Vector3f RED_COLOR = new Vector3f(1.0f, 0.32f, 0.32f);

    private Player owner;
    private Vec3 glideDirection = Vec3.ZERO;
    private boolean isGliding = false;
    private int lifetimeAfterTeleport = -1;
    private Vec3 glideStartPosition = Vec3.ZERO;
    private double maxGlideDistance = DEFAULT_MAX_GLIDE_DISTANCE;

    // Swing logic variables
    private int swingTime = 0;
    private static final int SWING_DURATION = 8;
    private static final float SWING_DAMAGE = 6.0f;
    private static final double SWING_RANGE = 2.5;

    private int particleSpawnTick = 0;

    public AfterimageEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomNameVisible(true);
        this.setNoGravity(true);
    }

    // --- IMMUNITY AND COLLISION ---
    @Override
    public boolean isPushable() { return false; }

    @Override
    public void push(Entity entity) { }

    @Override
    public void knockback(double strength, double x, double z) { }
    // -----------------------------

    public void setOwner(Player owner) {
        this.owner = owner;
        this.setCustomName(net.minecraft.network.chat.Component.literal("§9Afterimage"));
        if (owner != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(owner.getMaxHealth() * 0.5);
            this.setHealth(owner.getMaxHealth() * 0.5f);
        }
    }

    public Player getOwner() {
        return this.owner;
    }

    public void setGlideDirection(Vec3 direction) {
        this.glideDirection = direction;
        this.isGliding = true;
        this.glideStartPosition = this.position();
    }

    // --- SWING LOGIC ---
    public void performSwingAnimation() {
        this.swingTime = SWING_DURATION;

        if (!this.level().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) this.level();

            // 1. Visual: AoE Swipe (Semi-Circle)
            Vec3 viewVector = this.getViewVector(1.0f);
            double centerY = this.getY() + 1.0;
            double startAngle = Math.atan2(viewVector.z, viewVector.x) - Math.PI / 2;

            for (int i = 0; i < 5; i++) {
                double angleOffset = (i - 2) * (Math.PI / 4);
                double currentAngle = startAngle + angleOffset;
                double pX = this.getX() + Math.cos(currentAngle) * 1.5;
                double pZ = this.getZ() + Math.sin(currentAngle) * 1.5;
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, pX, centerY, pZ, 0, Math.cos(currentAngle), 0, Math.sin(currentAngle), 0);
            }

            // 2. Logic: AOE Damage Scan
            List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class,
                    this.getBoundingBox().inflate(SWING_RANGE));

            for (LivingEntity target : targets) {
                // ADDED: && !(target instanceof AfterimageEntity)
                if (target != this && target != owner && !target.isAlliedTo(owner) && !(target instanceof AfterimageEntity)) {
                    target.hurt(this.damageSources().mobAttack(this), SWING_DAMAGE);
                }
            }
        }
    }

    public boolean isSwinging() {
        return swingTime > 0;
    }

    public void startLifetimeTimer() { this.lifetimeAfterTeleport = LIFETIME_AFTER_TELEPORT_TICKS; }

    @Override
    protected void registerGoals() {}

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            if (!this.hasEffect(MobEffects.INVISIBILITY)) {
                this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISIBILITY_DURATION_TICKS, 0, false, false, false));
            }

            if (owner == null || owner.isRemoved() || (owner != null && this.distanceTo(owner) > MAX_DISTANCE_FROM_OWNER)) {
                this.discard();
                return;
            }

            if (lifetimeAfterTeleport >= 0) {
                lifetimeAfterTeleport--;
                if (lifetimeAfterTeleport <= 0) {
                    this.discard();
                    return;
                }
            }

            if (isGliding && glideDirection.lengthSqr() > 0) {
                if (this.position().distanceTo(glideStartPosition) >= maxGlideDistance || this.horizontalCollision || this.verticalCollision) {
                    stopGliding();
                } else {
                    Vec3 movement = glideDirection.normalize().scale(GLIDE_SPEED);
                    this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
                    this.setDeltaMovement(movement);
                }
            } else if (isGliding) {
                stopGliding();
                this.setDeltaMovement(Vec3.ZERO);
            }

            particleSpawnTick++;
            if (particleSpawnTick >= PARTICLE_SPAWN_INTERVAL) {
                particleSpawnTick = 0;
                if (this.level() instanceof ServerLevel serverLevel) {
                    spawnBody(serverLevel);
                }
            }

            if (swingTime > 0) {
                swingTime--;
            }
        }
    }

    public void stopGliding() {
        this.isGliding = false;
        this.glideDirection = Vec3.ZERO;
    }

    private void spawnBody(ServerLevel level) {
        double centerX = this.getX();
        double baseY = this.getY();
        double centerZ = this.getZ();

        boolean hasTeleportedTo = this.getPersistentData().getBoolean("teleported_to");
        Vector3f color = hasTeleportedTo ? RED_COLOR : LIGHT_BLUE_COLOR;
        DustParticleOptions dust = new DustParticleOptions(color, 0.35f);

        double rotationRadians = 0;
        if (isSwinging()) {
            double progress = (double)(SWING_DURATION - swingTime) / SWING_DURATION;
            rotationRadians = progress * Math.PI * 2;
        }

        // 1. HEAD
        drawRotatedBox(level, centerX, baseY + HEAD_BASE_Y, centerZ, HEAD_RADIUS * 2, HEAD_HEIGHT_SIZE, HEAD_RADIUS * 2, dust, rotationRadians);

        // 2. TORSO
        drawRotatedBox(level, centerX, baseY + HIP_Y, centerZ, TORSO_WIDTH, TORSO_HEIGHT, TORSO_DEPTH, dust, rotationRadians);

        // 3. ARMS
        double armBaseY = baseY + SHOULDER_Y - LIMB_HEIGHT;

        // Left Arm
        drawRotatedLimb(level, centerX, armBaseY, centerZ, -ARM_OFFSET, 0, LIMB_WIDTH, LIMB_HEIGHT, LIMB_DEPTH, dust, rotationRadians);

        // Right Arm
        double rightArmY = armBaseY;
        if (isSwinging()) {
            rightArmY += 0.6; // Raise arm
        }
        drawRotatedLimb(level, centerX, rightArmY, centerZ, ARM_OFFSET, 0, LIMB_WIDTH, LIMB_HEIGHT, LIMB_DEPTH, dust, rotationRadians);

        // 4. LEGS
        double legBaseY = baseY + HIP_Y - LIMB_HEIGHT;
        drawRotatedLimb(level, centerX, legBaseY, centerZ, -LEG_OFFSET, 0, LIMB_WIDTH, LIMB_HEIGHT, LIMB_DEPTH, dust, rotationRadians);
        drawRotatedLimb(level, centerX, legBaseY, centerZ, LEG_OFFSET, 0, LIMB_WIDTH, LIMB_HEIGHT, LIMB_DEPTH, dust, rotationRadians);
    }

    private void drawRotatedLimb(ServerLevel level, double cx, double y, double cz, double offsetX, double offsetZ, double w, double h, double d, DustParticleOptions p, double angle) {
        double rotatedX = offsetX * Math.cos(angle) - offsetZ * Math.sin(angle);
        double rotatedZ = offsetX * Math.sin(angle) + offsetZ * Math.cos(angle);
        drawRotatedBox(level, cx + rotatedX, y, cz + rotatedZ, w, h, d, p, angle);
    }

    private void drawRotatedBox(ServerLevel level, double cx, double by, double cz, double w, double h, double d, DustParticleOptions p, double angle) {
        double minX = -w/2; double maxX = w/2;
        double minZ = -d/2; double maxZ = d/2;
        double minY = by;   double maxY = by + h;
        double density = 8.0;

        // Top/Bottom
        for (double i = minX; i <= maxX; i += 1.0/density) {
            for (double k = minZ; k <= maxZ; k += 1.0/density) {
                spawnRotatedParticle(level, cx, minY, cz, i, k, angle, p);
                spawnRotatedParticle(level, cx, maxY, cz, i, k, angle, p);
            }
        }
        // Front/Back
        for (double i = minX; i <= maxX; i += 1.0/density) {
            for (double j = minY; j <= maxY; j += 1.0/density) {
                spawnRotatedParticle(level, cx, j, cz, i, minZ, angle, p);
                spawnRotatedParticle(level, cx, j, cz, i, maxZ, angle, p);
            }
        }
        // Left/Right
        for (double k = minZ; k <= maxZ; k += 1.0/density) {
            for (double j = minY; j <= maxY; j += 1.0/density) {
                spawnRotatedParticle(level, cx, j, cz, minX, k, angle, p);
                spawnRotatedParticle(level, cx, j, cz, maxX, k, angle, p);
            }
        }
    }

    private void spawnRotatedParticle(ServerLevel level, double cx, double y, double cz, double localX, double localZ, double angle, DustParticleOptions p) {
        double rotX = localX * Math.cos(angle) - localZ * Math.sin(angle);
        double rotZ = localX * Math.sin(angle) + localZ * Math.cos(angle);
        level.sendParticles(p, cx + rotX, y, cz + rotZ, 1, 0, 0, 0, 0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) tag.putUUID("owner", owner.getUUID());
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
                net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) this.level();
                this.owner = serverLevel.getPlayerByUUID(ownerUUID);
            }
        }
        if (tag.contains("isGliding")) this.isGliding = tag.getBoolean("isGliding");
        if (tag.contains("glideX")) {
            double x = tag.getDouble("glideX");
            double y = tag.getDouble("glideY");
            double z = tag.getDouble("glideZ");
            this.glideDirection = new Vec3(x, y, z);
        }
        if (tag.contains("lifetimeAfterTeleport")) this.lifetimeAfterTeleport = tag.getInt("lifetimeAfterTeleport");
        if (tag.contains("glideStartX")) {
            double x = tag.getDouble("glideStartX");
            double y = tag.getDouble("glideStartY");
            double z = tag.getDouble("glideStartZ");
            this.glideStartPosition = new Vec3(x, y, z);
        }
        if (tag.contains("maxGlideDistance")) this.maxGlideDistance = tag.getDouble("maxGlideDistance");
        if (tag.contains("swingTime")) this.swingTime = tag.getInt("swingTime");
        if (tag.contains("particleSpawnTick")) this.particleSpawnTick = tag.getInt("particleSpawnTick");
    }
}