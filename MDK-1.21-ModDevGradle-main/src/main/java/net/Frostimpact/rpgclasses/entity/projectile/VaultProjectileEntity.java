package net.Frostimpact.rpgclasses.entity.projectile;

import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class VaultProjectileEntity extends Projectile {

    private static final float DAMAGE = 4.0f;
    private static final int MAX_LIFETIME = 100;
    
    private int tickCount = 0;
    private boolean hasHit = false;

    public VaultProjectileEntity(EntityType<? extends VaultProjectileEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public VaultProjectileEntity(EntityType<? extends VaultProjectileEntity> type, Level level, LivingEntity shooter) {
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

        // Apply gravity
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x, movement.y - 0.05, movement.z);

        // Check for hits
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        // Apply movement
        this.setPos(this.position().add(this.getDeltaMovement()));

        // Particles - lime green arc trail
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.GLOW, 
                this.getX(), this.getY(), this.getZ(), 
                3, 0.1, 0.1, 0.1, 0.02);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.hasHit) return;
        super.onHitEntity(result);

        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            if (target == this.getOwner()) return;

            if (target instanceof LivingEntity livingTarget) {
                livingTarget.hurt(this.damageSources().thrown(this, this.getOwner()), DAMAGE);

                // Reset UPDRAFT cooldown if owner is player
                if (this.getOwner() instanceof ServerPlayer shooter) {
                    PlayerRPGData rpg = shooter.getData(ModAttachments.PLAYER_RPG);
                    rpg.setAbilityCooldown("updraft", 0);

                    shooter.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a✦ UPDRAFT refreshed!"));
                }

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.GLOW,
                        target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                        20, 0.3, 0.3, 0.3, 0.1);
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
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 1.5f);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.GLOW, 
                    this.getX(), this.getY(), this.getZ(), 
                    15, 0.2, 0.2, 0.2, 0.1);
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