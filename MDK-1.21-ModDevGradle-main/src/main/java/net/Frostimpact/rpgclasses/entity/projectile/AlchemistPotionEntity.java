package net.Frostimpact.rpgclasses.entity.projectile;

import net.Frostimpact.rpgclasses.registry.ModEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
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

public class AlchemistPotionEntity extends Projectile {

    private static final int MAX_LIFETIME = 100; // 5 seconds
    
    private int tickCount = 0;
    private boolean hasHit = false;
    private String effectType = ""; // Pattern like "LLL", "LRR", etc.
    private boolean lingering = false;

    public AlchemistPotionEntity(EntityType<? extends AlchemistPotionEntity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public AlchemistPotionEntity(EntityType<? extends AlchemistPotionEntity> type, Level level, LivingEntity shooter) {
        this(type, level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY(), shooter.getZ());
    }

    public void setEffectType(String effectType) {
        this.effectType = effectType;
    }

    public void setLingering(boolean lingering) {
        this.lingering = lingering;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("Age");
        this.hasHit = tag.getBoolean("HasHit");
        this.effectType = tag.getString("EffectType");
        this.lingering = tag.getBoolean("Lingering");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.tickCount);
        tag.putBoolean("HasHit", this.hasHit);
        tag.putString("EffectType", this.effectType);
        tag.putBoolean("Lingering", this.lingering);
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

        // Apply movement with gravity
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x, movement.y - 0.05, movement.z); // Add gravity
        this.setPos(this.position().add(this.getDeltaMovement()));

        // Particles - potion trail
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.WITCH,
                    this.getX(), this.getY(), this.getZ(),
                    2, 0.1, 0.1, 0.1, 0.01);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (this.hasHit) return;
        super.onHit(result);

        if (!this.level().isClientSide) {
            // Splash effect - apply to nearby entities
            List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(3.0),
                entity -> entity != this.getOwner()
            );

            for (LivingEntity entity : nearbyEntities) {
                applyEffect(entity);
            }

            // Create lingering cloud if volatile mix is active
            if (lingering) {
                createLingeringCloud();
            }

            // Impact sound and particles
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SPLASH_POTION_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SPLASH,
                        this.getX(), this.getY(), this.getZ(),
                        30, 1.0, 1.0, 1.0, 0.1);
            }

            this.hasHit = true;
            this.discard();
        }
    }

    private void applyEffect(LivingEntity target) {
        int duration = 80; // 4 seconds
        
        switch (effectType) {
            // Debuff patterns (3 clicks)
            case "LLL":
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0));
                break;
            case "LRR":
                target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0));
                break;
            case "LLR":
                target.addEffect(new MobEffectInstance(ModEffects.CORROSION, duration, 0));
                break;
            case "LRL":
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 0));
                break;
            case "RRL":
                target.addEffect(new MobEffectInstance(ModEffects.FREEZE, 30, 0)); // 1.5s
                break;
            case "RLR":
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0));
                break;
            case "RRR":
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0));
                break;
            
            // Buff patterns (2 clicks)
            case "LL":
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0));
                break;
            case "LR":
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 0));
                break;
            case "RL":
                target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 0));
                break;
            case "RR":
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 0));
                break;
        }
    }

    private void createLingeringCloud() {
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        cloud.setRadius(3.0f);
        cloud.setDuration(120); // 6 seconds
        cloud.setWaitTime(0);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        
        // Add the appropriate effect to the cloud
        MobEffectInstance effectToAdd = null;
        int duration = 80;
        
        switch (effectType) {
            case "LLL":
                effectToAdd = new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0);
                break;
            case "LRR":
                effectToAdd = new MobEffectInstance(MobEffects.POISON, duration, 0);
                break;
            case "LLR":
                effectToAdd = new MobEffectInstance(ModEffects.CORROSION, duration, 0);
                break;
            case "LRL":
                effectToAdd = new MobEffectInstance(MobEffects.WITHER, duration, 0);
                break;
            case "RRL":
                effectToAdd = new MobEffectInstance(ModEffects.FREEZE, 30, 0);
                break;
            case "RLR":
                effectToAdd = new MobEffectInstance(MobEffects.GLOWING, duration, 0);
                break;
            case "RRR":
                effectToAdd = new MobEffectInstance(MobEffects.WEAKNESS, duration, 0);
                break;
            case "LL":
                effectToAdd = new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0);
                break;
            case "LR":
                effectToAdd = new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 0);
                break;
            case "RL":
                effectToAdd = new MobEffectInstance(MobEffects.REGENERATION, duration, 0);
                break;
            case "RR":
                effectToAdd = new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 0);
                break;
        }
        
        if (effectToAdd != null) {
            cloud.addEffect(effectToAdd);
        }
        
        if (this.getOwner() instanceof LivingEntity livingOwner) {
            cloud.setOwner(livingOwner);
        }
        
        this.level().addFreshEntity(cloud);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) return false;
        if (entity.isSpectator()) return false;
        return true;
    }
}
