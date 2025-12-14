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

public class AfterimageEntity extends PathfinderMob {

    private Player owner;
    private Vec3 glideDirection = Vec3.ZERO;
    private boolean isGliding = false;
    private int lifetimeAfterTeleport = -1; // -1 means no timer, otherwise counts down from 80 ticks (4s)
    private Vec3 glideStartPosition = Vec3.ZERO;
    private double maxGlideDistance = 10.0;

    public AfterimageEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.setCustomNameVisible(true);
        this.setNoGravity(true); // Afterimages float/glide
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
    }

    public boolean isGliding() {
        return isGliding;
    }

    public void stopGliding() {
        this.isGliding = false;
        this.glideDirection = Vec3.ZERO;
    }

    public void startLifetimeTimer() {
        this.lifetimeAfterTeleport = 80; // 4 seconds = 80 ticks
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
            // Check if owner is too far away (>20 blocks)
            if (owner != null && !owner.isRemoved()) {
                double distanceToOwner = this.distanceTo(owner);
                if (distanceToOwner > 20.0) {
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

            // Handle gliding movement
            if (isGliding && glideDirection.lengthSqr() > 0) {
                // Check if we've traveled more than max distance
                double distanceTraveled = this.position().distanceTo(glideStartPosition);
                if (distanceTraveled >= maxGlideDistance) {
                    stopGliding();
                } else {
                    // Move in glide direction
                    Vec3 movement = glideDirection.normalize().scale(0.3); // Glide speed
                    this.setDeltaMovement(movement);
                    
                    // Check for collision/wall
                    if (this.horizontalCollision || this.verticalCollision) {
                        stopGliding();
                    }
                }
            } else if (isGliding) {
                stopGliding();
            }
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
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
    }
}
