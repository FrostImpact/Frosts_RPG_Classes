package net.Frostimpact.rpgclasses.ability.JUGGERNAUT;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class SwapAbility extends Ability {

    public SwapAbility() {
        super("swap");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        boolean isShieldMode = rpgData.isJuggernautShieldMode();
        int charge = rpgData.getJuggernautCharge();

        if (isShieldMode) {
            // SHIELD → SHATTER
            rpgData.setJuggernautShieldMode(false);
            rpgData.startChargeDecay();

            // Refresh LEAP cooldown
            rpgData.setAbilityCooldown("leap", 0);

            // AOE damage explosion
            double radius = 4.0;
            float baseDamage = charge / 10.0f; // 1 damage per 10 charge

            player.level().getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    player.getBoundingBox().inflate(radius),
                    entity -> entity != player && entity.isAlive()
            ).forEach(entity -> {
                entity.hurt(player.damageSources().playerAttack(player), baseDamage);
            });

            // Visual effects
            ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                    net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                    player.getX(), player.getY(), player.getZ(),
                    3, 0.5, 0.5, 0.5, 0.1
            );

            //player.level().playSound(null, player.blockPosition(),
                    //SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.6f, 1.2f);

            player.sendSystemMessage(Component.literal("§c⚔ SHATTER MODE §7| Charge: " + charge));

        } else {
            // SHATTER → SHIELD
            rpgData.setJuggernautShieldMode(true);
            rpgData.stopChargeDecay();

            // Grant absorption if charge > 50
            if (charge >= 50) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.ABSORPTION,
                        999999, // Infinite until removed
                        2, // 6 hearts (Absorption III = 3 hearts * 2)
                        false,
                        true,
                        true
                ));
                player.sendSystemMessage(Component.literal("§b🛡 SHIELD MODE §7| §a+6 Absorption Hearts"));
            } else {
                player.sendSystemMessage(Component.literal("§b🛡 SHIELD MODE"));
            }

            // Remove Strength buffs from SHATTER mode
            player.removeEffect(MobEffects.DAMAGE_BOOST);

            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.8f);
        }

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}