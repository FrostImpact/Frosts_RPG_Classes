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

public class SeekerArrowEntity extends Projectile {

    private static final float DAMAGE = 6.0f;
    private static final int MAX_LIFETIME = 100; // 5 seconds
    private static final double HOMING_STRENGTH = 0.15;
    private static final double HOMING_RANGE = 16.0;

    private int tickCount = 0;
    private boolean hasHit = false;
    private LivingEntity target = null;

    public SeekerArrowEntity(EntityType<? extends SeekerArrowEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public SeekerArrowEntity(EntityType<? extends SeekerArrowEntity> type, Level level, LivingEntity shooter) {
        this(type, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY(), shooter.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

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

        if (this.tickCount > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // Find target if we don't have one
        if (this.target == null || !this.target.isAlive()) {
            this.target = findNearestTarget();
        }

        // Home toward target
        if (this.target != null) {
            Vec3 toTarget = this.target.position()
                    .add(0, this.target.getBbHeight() / 2, 0)
                    .subtract(this.position())
                    .normalize();

            Vec3 currentVel = this.getDeltaMovement();
            Vec3 newVel = currentVel.add(toTarget.scale(HOMING_STRENGTH)).normalize().scale(currentVel.length());
            this.setDeltaMovement(newVel);
        }

        // Check for hits
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        // Apply movement
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.position().add(movement));

        // Particles - lime green trail
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.GLOW,
                    this.getX(), this.getY(), this.getZ(),
                    2, 0.05, 0.05, 0.05, 0.01);
        }
    }

    private LivingEntity findNearestTarget() {
        return this.level().getNearestEntity(
                LivingEntity.class,
                net.minecraft.world.entity.ai.targeting.TargetingConditions.DEFAULT,
                (LivingEntity) this.getOwner(),
                this.getX(), this.getY(), this.getZ(),
                this.getBoundingBox().inflate(HOMING_RANGE)
        );
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.hasHit) return;
        super.onHitEntity(result);

        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            if (target == this.getOwner()) return;

            if (target instanceof LivingEntity livingTarget) {
                // FIX: Changed .arrow(...) to .projectile(...) because SeekerArrowEntity
                // does not extend AbstractArrow.
                livingTarget.hurt(this.damageSources().thrown(this, this.getOwner()), DAMAGE);

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.GLOW,
                            target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                            15, 0.3, 0.3, 0.3, 0.1);
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
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARROW_HIT, SoundSource.PLAYERS, 1.0f, 1.2f);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.GLOW,
                        this.getX(), this.getY(), this.getZ(),
                        10, 0.2, 0.2, 0.2, 0.1);
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
        return entity instanceof LivingEntity;
    }
}