package net.Frostimpact.rpgclasses.ability.MERCENARY;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class CloakAbility extends Ability {

    private static final int DURATION_TICKS = 999999; // Permanent while sneaking

    public CloakAbility() {
        super("cloak");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Activate CLOAK
        rpgData.setCloakActive(true);
        
        // Grant invisibility
        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY,
                DURATION_TICKS,
                0,
                false,
                false,
                true
        ));

        // Activation effects
        ServerLevel level = player.serverLevel();
        
        // Smoke effect
        level.sendParticles(
                ParticleTypes.SMOKE,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.5, 0.5, 0.5, 0.05
        );

        // Sound
        level.playSound(null, player.blockPosition(),
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.5f, 2.0f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§8✦ CLOAK activated"));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    public static void deactivateCloak(ServerPlayer player, PlayerRPGData rpgData) {
        if (!rpgData.isCloakActive()) return;

        rpgData.setCloakActive(false);
        player.removeEffect(MobEffects.INVISIBILITY);

        // Deactivation effect
        ServerLevel level = player.serverLevel();
        level.sendParticles(
                ParticleTypes.CLOUD,
                player.getX(), player.getY() + 1, player.getZ(),
                15, 0.3, 0.3, 0.3, 0.05
        );

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7CLOAK ended"));
    }
}