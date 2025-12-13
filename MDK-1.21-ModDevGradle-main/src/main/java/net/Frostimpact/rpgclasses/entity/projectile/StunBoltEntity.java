package net.Frostimpact.rpgclasses.entity.projectile;

import net.Frostimpact.rpgclasses.rpg.PlayerStats;
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

public class StunBoltEntity extends Projectile {

    private static final int PARALYSIS_DURATION = 60; // 3 seconds
    private static final int MAX_LIFETIME = 100; // 5 seconds
    
    private int tickCount = 0;
    private boolean hasHit = false;

    public StunBoltEntity(EntityType<? extends StunBoltEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public StunBoltEntity(EntityType<? extends StunBoltEntity> type, Level level, LivingEntity shooter) {
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

        // Check for hits
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        // Apply movement
        Vec3 movement = this.getDeltaMovement();
        this.setPos(this.position().add(movement));

        // Particles - yellow/electric trail
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    this.getX(), this.getY(), this.getZ(),
                    2, 0.05, 0.05, 0.05, 0.01);

            if (this.tickCount % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        this.getX(), this.getY(), this.getZ(),
                        1, 0.05, 0.05, 0.05, 0.005);
            }
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
                // Apply PARALYSIS
                applyParalysis(livingTarget);

                // Impact particles
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                            target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                            20, 0.3, 0.3, 0.3, 0.1);

                    serverLevel.sendParticles(ParticleTypes.FLASH,
                            target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                            1, 0, 0, 0, 0);
                }

                // Make target glow (PREDATOR EYE)
                livingTarget.setGlowingTag(true);
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
                    SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.8f, 1.5f);

            // Impact particles
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        this.getX(), this.getY(), this.getZ(),
                        15, 0.2, 0.2, 0.2, 0.1);
            }

            this.hasHit = true;
            this.discard();
        }
    }

    private void applyParalysis(LivingEntity target) {
        // Apply stat modifiers for PARALYSIS
        // Speed reduction (0.1 = 90% slower)
        PlayerStats.StatModifier speedDebuff = new PlayerStats.StatModifier(
                "paralysis_speed",
                PlayerStats.StatType.SPEED,
                0.1,
                PlayerStats.ModifierType.MULTIPLY,
                PARALYSIS_DURATION
        );

        // Damage reduction (0.3 = 70% less damage)
        PlayerStats.StatModifier damageDebuff = new PlayerStats.StatModifier(
                "paralysis_damage",
                PlayerStats.StatType.DAMAGE,
                0.3,
                PlayerStats.ModifierType.MULTIPLY,
                PARALYSIS_DURATION
        );

        // Note: This assumes you've added PlayerStats to the attachment system
        // You'll need to get the target's stats and apply these modifiers
        
        target.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§e⚡ PARALYZED!"));
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) return false;
        if (entity.isSpectator()) return false;
        if (entity == this.getOwner()) return false;
        return entity instanceof LivingEntity;
    }
}