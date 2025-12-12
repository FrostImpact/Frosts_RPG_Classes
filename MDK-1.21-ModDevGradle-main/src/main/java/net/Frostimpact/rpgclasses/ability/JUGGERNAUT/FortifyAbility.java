package net.Frostimpact.rpgclasses.ability.JUGGERNAUT;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class FortifyAbility extends Ability {

    private static final int DURATION_TICKS = 100; // 5 seconds

    public FortifyAbility() {
        super("fortify");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        boolean isShieldMode = rpgData.isJuggernautShieldMode();

        // Always grant Resistance I
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                DURATION_TICKS,
                0 // Resistance I
        ));

        // If in SHIELD mode, activate special effects
        if (isShieldMode) {
            rpgData.setFortifyActive(true);
            rpgData.setFortifyTicks(DURATION_TICKS);

            // Apply Slowness I
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    DURATION_TICKS,
                    0 // Slowness I
            ));
        }

        ServerLevel level = (ServerLevel) player.level();

        // === PARTICLE SHIELD EFFECT ===
        // Create a circular shield pattern
        int particleCount = 40;
        double radius = 2.0;

        for (int i = 0; i < particleCount; i++) {
            double angle = (2 * Math.PI * i) / particleCount;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;

            // Ground level particles
            level.sendParticles(
                    ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX() + xOffset,
                    player.getY() + 0.5,
                    player.getZ() + zOffset,
                    1, 0, 0, 0, 0
            );

            // Mid level particles
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    player.getX() + xOffset * 0.8,
                    player.getY() + 1.0,
                    player.getZ() + zOffset * 0.8,
                    1, 0, 0, 0, 0
            );

            // Top level particles
            level.sendParticles(
                    ParticleTypes.TOTEM_OF_UNDYING,
                    player.getX() + xOffset * 0.6,
                    player.getY() + 1.5,
                    player.getZ() + zOffset * 0.6,
                    1, 0, 0, 0, 0
            );
        }

        // Central burst
        level.sendParticles(
                ParticleTypes.FIREWORK,
                player.getX(), player.getY() + 1, player.getZ(),
                20, 0.5, 0.5, 0.5, 0.1
        );

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5f, 1.5f);

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}