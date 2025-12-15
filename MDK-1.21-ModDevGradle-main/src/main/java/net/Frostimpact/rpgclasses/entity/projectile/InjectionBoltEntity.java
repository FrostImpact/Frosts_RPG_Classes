package net.Frostimpact.rpgclasses.entity.projectile;

import net.Frostimpact.rpgclasses.registry.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class InjectionBoltEntity extends Projectile {

    private static final int MAX_LIFETIME = 100; // 5 seconds
    
    private int tickCount = 0;
    private boolean hasHit = false;
    private String reagentType = "CRYOSTAT"; // CRYOSTAT, CATALYST, FRACTURE, SANCTIFIED

    public InjectionBoltEntity(EntityType<? extends InjectionBoltEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public InjectionBoltEntity(EntityType<? extends InjectionBoltEntity> type, Level level, LivingEntity shooter) {
        this(type, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY(), shooter.getZ());
    }

    public void setReagentType(String reagentType) {
        this.reagentType = reagentType;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("Age");
        this.hasHit = tag.getBoolean("HasHit");
        this.reagentType = tag.getString("ReagentType");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.tickCount);
        tag.putBoolean("HasHit", this.hasHit);
        tag.putString("ReagentType", this.reagentType);
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

        // Particles based on reagent type
        if (this.level() instanceof ServerLevel serverLevel) {
            switch (reagentType) {
                case "CRYOSTAT":
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                            this.getX(), this.getY(), this.getZ(),
                            2, 0.05, 0.05, 0.05, 0.01);
                    break;
                case "CATALYST":
                    serverLevel.sendParticles(ParticleTypes.SMOKE,
                            this.getX(), this.getY(), this.getZ(),
                            2, 0.05, 0.05, 0.05, 0.01);
                    break;
                case "FRACTURE":
                    serverLevel.sendParticles(ParticleTypes.CRIT,
                            this.getX(), this.getY(), this.getZ(),
                            2, 0.05, 0.05, 0.05, 0.01);
                    break;
                case "SANCTIFIED":
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            this.getX(), this.getY(), this.getZ(),
                            2, 0.05, 0.05, 0.05, 0.01);
                    break;
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
                // Check for appropriate debuff and trigger reagent effect
                boolean triggered = false;

                switch (reagentType) {
                    case "CRYOSTAT":
                        // Check for slowness or freeze
                        if (livingTarget.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || 
                            livingTarget.hasEffect(ModEffects.FREEZE)) {
                            triggerCryostatExplosion(livingTarget);
                            triggered = true;
                        }
                        break;
                    case "CATALYST":
                        // Check for poison or wither
                        if (livingTarget.hasEffect(MobEffects.POISON) || 
                            livingTarget.hasEffect(MobEffects.WITHER)) {
                            triggerCatalystExplosion(livingTarget);
                            triggered = true;
                        }
                        break;
                    case "FRACTURE":
                        // Check for weakness or corrosion
                        if (livingTarget.hasEffect(MobEffects.WEAKNESS) || 
                            livingTarget.hasEffect(ModEffects.CORROSION)) {
                            triggerFractureExplosion(livingTarget);
                            triggered = true;
                        }
                        break;
                    case "SANCTIFIED":
                        // Check for glowing
                        if (livingTarget.hasEffect(MobEffects.GLOWING)) {
                            triggerSanctifiedSmite(livingTarget);
                            triggered = true;
                        }
                        break;
                }

                if (!triggered) {
                    // Just apply a small amount of damage if no effect triggered
                    livingTarget.hurt(this.damageSources().magic(), 2.0f);
                }

                // Impact particles
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.WITCH,
                            target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                            15, 0.3, 0.3, 0.3, 0.1);
                }
            }

            this.hasHit = true;
        }
    }

    private void triggerCryostatExplosion(LivingEntity target) {
        // Release an explosion that stuns in a radius for 4s
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
            LivingEntity.class,
            target.getBoundingBox().inflate(4.0),
            entity -> entity != this.getOwner()
        );

        for (LivingEntity entity : nearbyEntities) {
            // Stun = slowness + jump reduction
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3)); // 4s, very slow
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
        }

        // Explosion particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    5, 2.0, 2.0, 2.0, 0);
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    40, 2.0, 2.0, 2.0, 0.1);
        }

        this.level().playSound(null, target.blockPosition(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5f, 0.8f);
    }

    private void triggerCatalystExplosion(LivingEntity target) {
        // Release an explosion that deals instant damage in a radius
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
            LivingEntity.class,
            target.getBoundingBox().inflate(4.0),
            entity -> entity != this.getOwner()
        );

        DamageSource damageSource = this.damageSources().explosion(this, this.getOwner());
        for (LivingEntity entity : nearbyEntities) {
            entity.hurt(damageSource, 8.0f);
        }

        // Explosion particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.LAVA,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    30, 2.0, 2.0, 2.0, 0.1);
        }

        this.level().playSound(null, target.blockPosition(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5f, 1.0f);
    }

    private void triggerFractureExplosion(LivingEntity target) {
        // Release an explosion that applies BRITTLE in a radius for 4s
        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
            LivingEntity.class,
            target.getBoundingBox().inflate(4.0),
            entity -> entity != this.getOwner()
        );

        for (LivingEntity entity : nearbyEntities) {
            entity.addEffect(new MobEffectInstance(ModEffects.BRITTLE, 80, 0)); // 4s
        }

        // Explosion particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    3, 2.0, 2.0, 2.0, 0);
            serverLevel.sendParticles(ParticleTypes.CRIT,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    40, 2.0, 2.0, 2.0, 0.1);
        }

        this.level().playSound(null, target.blockPosition(),
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.5f, 1.2f);
    }

    private void triggerSanctifiedSmite(LivingEntity target) {
        // Conjure a holy smite that does massive damage to undead enemies
        float damage = 5.0f;
        
        if (target.getType().is(EntityTypeTags.UNDEAD)) {
            damage = 20.0f; // Massive damage to undead
        }

        target.hurt(this.damageSources().magic(), damage);

        // Holy smite particles
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    50, 0.5, 1.0, 0.5, 0.2);
            serverLevel.sendParticles(ParticleTypes.FLASH,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    3, 0, 0, 0, 0);
        }

        this.level().playSound(null, target.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.5f);
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.hasHit) return;
        super.onHit(result);

        if (!this.level().isClientSide) {
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
