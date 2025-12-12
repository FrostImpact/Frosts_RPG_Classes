package net.Frostimpact.rpgclasses.ability.JUGGERNAUT;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerLevel; // Added for explicit casting
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes; // Added for particle types
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity; // Added for LivingEntity

public class CrushAbility extends Ability {

    private static final double RADIUS = 5.0;
    private static final float BASE_DAMAGE = 6.0f;
    private static final float BONUS_DAMAGE = 4.0f;
    private static final int CHARGE_COST = 10;

    // Suggestion: Add constant for SLOWNESS DURATION and AMPLIFIER for readability
    private static final int SLOW_DURATION_TICKS = 60; // 3 seconds (20 ticks/sec)
    private static final int SLOW_AMPLIFIER = 1;      // Slowness II (0 = I, 1 = II)

    public CrushAbility() {
        super("crush");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // --- 1. Setup and Resource Check ---
        boolean isShieldMode = rpgData.isJuggernautShieldMode();
        int charge = rpgData.getJuggernautCharge();

        float damage = BASE_DAMAGE;
        boolean applyBonus;

        // Check if in SHATTER mode (not ShieldMode) with enough charge
        if (!isShieldMode && charge >= CHARGE_COST) {
            rpgData.removeJuggernautCharge(CHARGE_COST);
            damage += BONUS_DAMAGE;
            applyBonus = true;
        } else {
            applyBonus = false;
        }

        // --- 2. Deal AOE Damage and Effects ---
        // Explicitly cast to ServerLevel for particle sending
        ServerLevel serverLevel = (ServerLevel) player.level();

        float finalDamage = damage;
        serverLevel.getEntitiesOfClass(
                LivingEntity.class, // Using explicit import
                player.getBoundingBox().inflate(RADIUS),
                entity -> entity != player && entity.isAlive() // Filter: exclude self, include only alive entities
        ).forEach(entity -> {
            // Deal damage
            entity.hurt(player.damageSources().playerAttack(player), finalDamage);

            // Apply slowness if bonus was active
            if (applyBonus) {
                entity.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN,
                        SLOW_DURATION_TICKS,
                        SLOW_AMPLIFIER
                ));
            }
        });

        // --- 3. Visual Effects ---
        // The cast to ServerLevel is required here for sendParticles
        if (applyBonus) {
            serverLevel.sendParticles(
                    ParticleTypes.SWEEP_ATTACK, // Using explicit import
                    player.getX(), player.getY(), player.getZ(),
                    20, 2.0, 0.5, 2.0, 0.1
            );
            serverLevel.sendParticles(
                    ParticleTypes.LAVA, // Using explicit import
                    player.getX(), player.getY(), player.getZ(),
                    10, 2.0, 0.5, 2.0, 0.1
            );
        } else {
            serverLevel.sendParticles(
                    ParticleTypes.SWEEP_ATTACK, // Using explicit import
                    player.getX(), player.getY(), player.getZ(),
                    10, 2.0, 0.5, 2.0, 0.1
            );
        }

        // --- 4. Sound and Cooldown/Mana ---
        // Sound is played once for the ability
        ((ServerLevel) player.level()).playSound(
                null, // No tracking player
                player.getX(), player.getY(), player.getZ(), // Sound location
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.PLAYERS,
                applyBonus ? 1.0f : 0.7f,
                applyBonus ? 0.8f : 1.0f);

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}