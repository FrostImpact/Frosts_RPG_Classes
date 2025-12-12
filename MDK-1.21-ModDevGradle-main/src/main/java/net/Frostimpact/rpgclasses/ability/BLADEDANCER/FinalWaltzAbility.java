package net.Frostimpact.rpgclasses.ability.BLADEDANCER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class FinalWaltzAbility extends Ability {

    private static final int DURATION_TICKS = 200; // 10 seconds

    public FinalWaltzAbility() {
        super("blade_waltz");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Activate Final Waltz
        rpgData.setFinalWaltzActive(true);
        rpgData.setFinalWaltzTicks(DURATION_TICKS);
        rpgData.setFinalWaltzOverflow(0);

        // If player already has TEMPO stacks, preserve them
        if (rpgData.isTempoActive()) {
            player.sendSystemMessage(Component.literal("§6⚔ FINAL WALTZ! §7TEMPO preserved!"));
        }

        // Visual feedback - glowing effect
        player.addEffect(new MobEffectInstance(
                MobEffects.GLOWING,
                DURATION_TICKS,
                0,
                false,
                true,
                true
        ));

        // Sound effect
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.6f, 1.8f);

        // Epic particle burst
        ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 0.5, 1, 0.5, 0.2
        );

        player.sendSystemMessage(Component.literal("§5✦ FINAL WALTZ ACTIVATED! §7TEMPO can now overflow!"));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}