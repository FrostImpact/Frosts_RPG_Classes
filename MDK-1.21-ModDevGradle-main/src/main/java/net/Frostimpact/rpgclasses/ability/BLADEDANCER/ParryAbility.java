package net.Frostimpact.rpgclasses.ability.BLADEDANCER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class ParryAbility extends Ability {

    private static final int PARRY_DURATION_TICKS = 8; // 0.4 seconds

    public ParryAbility() {
        super("parry");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Activate parry window
        rpgData.setParryActive(true);
        rpgData.setParryTicks(PARRY_DURATION_TICKS);
        rpgData.setParrySuccessful(false);

        // Grant brief invulnerability (Resistance V for 0.4s simulates I-frames)
        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                PARRY_DURATION_TICKS,
                4, // Resistance V (reduces damage by 100%)
                false,
                false,
                true
        ));

        // Visual/audio feedback for activation
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8f, 1.8f);

        // Particle effect
        ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                player.getX(), player.getY() + 1, player.getZ(),
                5, 0.5, 0.5, 0.5, 0.1
        );

        // Consume resources
        int cooldown = rpgData.isFinalWaltzActive() ? 40 : getCooldownTicks(); // 2s during Final Waltz, 6s normally
        rpgData.setAbilityCooldown(id, cooldown);
        rpgData.useMana(getManaCost());

        return true;
    }
}