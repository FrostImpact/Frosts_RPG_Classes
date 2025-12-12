package net.Frostimpact.rpgclasses.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MagicMissileEntity extends Projectile {

    private static final float DAMAGE = 8.0f;
    private static final int MAX_LIFETIME = 100; // 5 seconds
    private int tickCount = 0;
    private boolean hasHit = false;

    public MagicMissileEntity(EntityType<? extends MagicMissileEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public MagicMissileEntity(EntityType<? extends MagicMissileEntity> type, Level level, LivingEntity shooter) {
        this(type, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Required override - no synched data needed
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("Age");
        this.hasHit = tag.getBoolean("HasHit");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.tickCount);
        tag.putBoolean("HasHit", this.hasHit);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.hasHit) {
            this.discard();
            return;
        }

        this.tickCount++;

        // Check for expiration
        if (this.tickCount > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // Check for hits
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        // Apply movement
        Vec3 movement = this.getDeltaMovement();
        Vec3 nextPos = this.position().add(movement);
        this.setPos(nextPos.x, nextPos.y, nextPos.z);

        // Update rotation to face direction of travel
        this.setYRot((float) (Math.atan2(movement.x, movement.z) * (180 / Math.PI)));
        this.setXRot((float) (Math.atan2(movement.y, Math.sqrt(movement.x * movement.x + movement.z * movement.z)) * (180 / Math.PI)));

        // Spawn particles
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();

            // Primary trail (purple/cyan magic particles)
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    pos.x, pos.y, pos.z,
                    1, 0.05, 0.05, 0.05, 0.01);

            // Secondary trail (dragon breath for ethereal effect)
            if (this.tickCount % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH,
                        pos.x, pos.y, pos.z,
                        1, 0.05, 0.05, 0.05, 0.005);
            }

            // Sparkles
            if (this.tickCount % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        pos.x, pos.y, pos.z,
                        1, 0.1, 0.1, 0.1, 0.02);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.hasHit) return;

        super.onHitEntity(result);

        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();

            // Don't hit the owner
            if (target == owner) return;

            if (target instanceof LivingEntity livingTarget) {
                // Deal damage
                livingTarget.hurt(this.damageSources().magic(), DAMAGE);

                // Impact particles
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                            10, 0.2, 0.2, 0.2, 0.1);

                    serverLevel.sendParticles(ParticleTypes.WITCH,
                            target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                            5, 0.3, 0.3, 0.3, 0.05);
                }
            }

            this.hasHit = true;
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.hasHit) return;

        super.onHit(result);

        if (!this.level().isClientSide) {
            // Impact sound
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.5f);

            // Impact particles
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH,
                        this.getX(), this.getY(), this.getZ(),
                        15, 0.2, 0.2, 0.2, 0.1);

                serverLevel.sendParticles(ParticleTypes.FLASH,
                        this.getX(), this.getY(), this.getZ(),
                        1, 0, 0, 0, 0);
            }

            this.hasHit = true;
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) return false;
        if (entity.isSpectator()) return false;
        if (entity == this.getOwner()) return false;

        // Only hit living entities
        return entity instanceof LivingEntity;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        // Render up to 64 blocks away
        return distance < 4096.0;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}