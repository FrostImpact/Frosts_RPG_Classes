package net.Frostimpact.rpgclasses.ability.JUGGERNAUT;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.chat.Component;
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

            player.sendSystemMessage(Component.literal("§e⚔ FORTIFY §7| +10 Charge per hit | Resistance I"));
        } else {
            player.sendSystemMessage(Component.literal("§e⚔ FORTIFY §7| Resistance I"));
        }

        // Visual effects
        ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                net.minecraft.core.particles.ParticleTypes.TOTEM_OF_UNDYING,
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