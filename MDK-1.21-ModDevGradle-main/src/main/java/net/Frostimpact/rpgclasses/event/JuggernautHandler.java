package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class JuggernautHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            // Only run for Juggernaut class
            if (!rpg.getCurrentClass().equals("JUGGERNAUT")) return;

            // Handle charge decay in SHATTER mode
            if (!rpg.isJuggernautShieldMode() && rpg.isChargeDecaying()) {
                rpg.tickChargeDecay();

                // Check if charge fully depleted (auto-swapped to SHIELD)
                if (rpg.getJuggernautCharge() == 0 && rpg.isJuggernautShieldMode()) {
                    player.removeEffect(MobEffects.DAMAGE_BOOST);
                    player.sendSystemMessage(Component.literal("§7Charge depleted - switched to §b🛡 SHIELD MODE"));
                    player.level().playSound(null, player.blockPosition(),
                            SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.8f);
                }
            }

            // Apply Strength buffs in SHATTER mode based on charge
            if (!rpg.isJuggernautShieldMode()) {
                int charge = rpg.getJuggernautCharge();

                if (charge > 50) {
                    // Strength II
                    if (!player.hasEffect(MobEffects.DAMAGE_BOOST) ||
                            player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() < 1) {
                        player.removeEffect(MobEffects.DAMAGE_BOOST);
                        player.addEffect(new MobEffectInstance(
                                MobEffects.DAMAGE_BOOST,
                                999999,
                                1, // Strength II
                                false,
                                false,
                                true
                        ));
                    }
                } else if (charge > 0) {
                    // Strength I
                    if (!player.hasEffect(MobEffects.DAMAGE_BOOST) ||
                            player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() != 0) {
                        player.removeEffect(MobEffects.DAMAGE_BOOST);
                        player.addEffect(new MobEffectInstance(
                                MobEffects.DAMAGE_BOOST,
                                999999,
                                0, // Strength I
                                false,
                                false,
                                true
                        ));
                    }
                } else {
                    // No charge - remove Strength
                    player.removeEffect(MobEffects.DAMAGE_BOOST);
                }
            }

            // Handle Fortify duration
            if (rpg.isFortifyActive()) {
                rpg.setFortifyTicks(rpg.getFortifyTicks() - 1);

                if (rpg.getFortifyTicks() <= 0) {
                    rpg.setFortifyActive(false);
                    player.sendSystemMessage(Component.literal("§7Fortify ended"));
                }
            }

            // Handle Leap landing
            if (rpg.isLeapActive()) {
                // Check if player is on ground or falling
                if (player.onGround() || player.getDeltaMovement().y <= 0) {
                    double currentY = player.getY();
                    double startY = rpg.getLeapStartY();

                    // Only trigger if we've moved down (landed)
                    if (currentY < startY - 0.5) {
                        // Landing effects
                        ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                                net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                                player.getX(), player.getY(), player.getZ(),
                                5, 1.0, 0.5, 1.0, 0.1
                        );

                        //player.level().playSound(null, player.blockPosition(),
                                //SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.5f, 0.8f);

                        // SHIELD mode: Grant absorption to nearby allies
                        if (rpg.isLeapShieldMode()) {
                            double radius = 5.0;

                            player.level().getEntitiesOfClass(
                                    net.minecraft.server.level.ServerPlayer.class,
                                    player.getBoundingBox().inflate(radius),
                                    ally -> ally.isAlive()
                            ).forEach(ally -> {
                                ally.addEffect(new MobEffectInstance(
                                        MobEffects.ABSORPTION,
                                        999999,
                                        2, // 6 hearts
                                        false,
                                        true,
                                        true
                                ));
                                ally.sendSystemMessage(Component.literal("§a+6 Absorption Hearts §7from Leap!"));
                            });
                        }

                        rpg.setLeapActive(false);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDamaged(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            // Only run for Juggernaut class
            if (!rpg.getCurrentClass().equals("JUGGERNAUT")) return;

            // INERTIA PASSIVE: Gain charge when taking damage in SHIELD mode
            if (rpg.isJuggernautShieldMode()) {
                float damage = event.getAmount();
                int chargeGain = (int) (damage * 2); // 2 charge per damage point

                // Fortify active: +10 charge per hit
                if (rpg.isFortifyActive()) {
                    chargeGain += 10;
                }

                int oldCharge = rpg.getJuggernautCharge();
                rpg.addJuggernautCharge(chargeGain);
                int newCharge = rpg.getJuggernautCharge();

                // Visual feedback
                if (player.level().getGameTime() % 10 == 0) { // Throttle messages
                    player.sendSystemMessage(Component.literal("§b+§f" + (newCharge - oldCharge) + " §bCHARGE §7[" + newCharge + "/" + rpg.getJuggernautMaxCharge() + "]"));
                }
            }
        }
    }
}