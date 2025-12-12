package net.Frostimpact.rpgclasses.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.synched.SynchedEntityData;
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

    public MagicMissileEntity(EntityType<? extends MagicMissileEntity> type, Level level) {
        super(type, level);
        this.noCulling = true; // Always render even if off-screen slightly
    }

    public MagicMissileEntity(EntityType<? extends MagicMissileEntity> type, Level level, LivingEntity shooter) {
        this(type, level);
        this.setOwner(shooter);
        // Start position: Eye height of shooter
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }

    // --- ESSENTIAL METHODS FOR "PROJECTILE" BASE CLASS ---

    //@Override
    //protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Required by 1.21+, but we don't need any data synced for now.
    //}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // Save/Load lifetime so missile doesn't reset on server restart
        this.tickCount = tag.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.tickCount);
    }

    // -----------------------------------------------------

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public void tick() {
        super.tick();
        this.tickCount++;

        // 1. Check for expiration
        if (this.tickCount > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // 2. Handle Movement & Collision (Since we aren't using ThrowableProjectile, we do this manually)
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        // Apply movement
        Vec3 currentMovement = this.getDeltaMovement();
        double nextX = this.getX() + currentMovement.x;
        double nextY = this.getY() + currentMovement.y;
        double nextZ = this.getZ() + currentMovement.z;

        // Update position
        this.setPos(nextX, nextY, nextZ);

        // Keep it flying straight (No gravity logic here keeps it straight)
        // If you wanted it to slow down (drag), you would multiply currentMovement by 0.99 here.

        // 3. Particles
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();

            // Primary Trail
            serverLevel.sendParticles(ParticleTypes.WITCH, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);

            // Secondary Trail
            serverLevel.sendParticles(ParticleTypes.DRAGON_BREATH, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);

            // Occasional Sparkle
            if (this.tickCount % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            Entity owner = this.getOwner();

            // Safety check: Don't hit yourself immediately
            if (owner != null && target == owner) return;

            // Deal Damage
            target.hurt(this.damageSources().magic(), DAMAGE); // Using .magic() source fits the theme better!

            // Particles on Impact
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY(0.5), target.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        // Stop the missile when it hits anything
        if (!this.level().isClientSide) {
            // Impact Sound
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.5f);

            // Impact Particles (Explosion style)
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.1);
                serverLevel.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
            }

            this.discard(); // Kill the entity
        }
    }

    // Helper to allow the projectile to hit entities
    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !entity.isSpectator();
    }
}