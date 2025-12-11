package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * This class handles events that occur on both client and server.
 * It registers itself automatically via @EventBusSubscriber.
 */
@EventBusSubscriber(modid = RpgClassesMod.MOD_ID)
public class ModEvents {

    /**
     * This event runs when a player respawns (Clone).
     * It preserves RPG data from the old player to the new one.
     */
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        // Get the old data (from the dead body)
        PlayerRPGData oldData = event.getOriginal().getData(ModAttachments.PLAYER_RPG);
        // Get the new data (for the new body)
        PlayerRPGData newData = event.getEntity().getData(ModAttachments.PLAYER_RPG);

        // Copy the class over (always preserve)
        newData.setCurrentClass(oldData.getCurrentClass());

        newData.setMana(newData.getMaxMana());

        // BLADEMANCER: Reset tempo on death
        newData.resetTempo();

    }


    /**
     * This event runs when a living entity takes damage.
     * We use it to handle Blade Dance blade removal.
     */
    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        // Check if the damaged entity is a player
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            // If Blade Dance is active, remove a blade and heal
            if (rpg.isBladeDanceActive() && rpg.getBladeDanceBlades() > 0) {
                // Remove one sword entity first
                if (!rpg.getBladeDanceSwordIds().isEmpty()) {
                    Integer swordId = rpg.getBladeDanceSwordIds().remove(rpg.getBladeDanceSwordIds().size() - 1);
                    net.minecraft.world.entity.Entity entity = player.level().getEntity(swordId);
                    if (entity != null) {
                        entity.discard();
                    }
                }

                // Remove blade from counter
                rpg.removeBlade();

                // Heal 2 HP
                player.heal(2.0f);

                // Visual/audio feedback
                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.GLASS_BREAK,
                        SoundSource.PLAYERS, 0.8f, 1.5f);

                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cBlade shattered! §a+2 HP §7(" + rpg.getBladeDanceBlades() + " blades remaining)"
                ));

                // If no blades left, end the ability early
                if (rpg.getBladeDanceBlades() <= 0) {
                    // Clean up any remaining sword entities
                    for (Integer swordId : rpg.getBladeDanceSwordIds()) {
                        net.minecraft.world.entity.Entity entity = player.level().getEntity(swordId);
                        if (entity != null) {
                            entity.discard();
                        }
                    }
                    rpg.clearBladeDanceSwords();

                    rpg.setBladeDanceActive(false);
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§cAll blades destroyed!"));
                }
            }
        }
    }
     //This event runs when a player attacks an entity.
     //We use it to handle the TEMPO passive ability.

    @SubscribeEvent
    public static void onPlayerAttack(net.neoforged.neoforge.event.entity.player.AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            // Only trigger TEMPO for Bladedancer class
            if (rpg.getCurrentClass().equals("BLADEDANCER")) {

                // Add a tempo stack
                rpg.addTempoStack();

                // Check tempo stacks
                if (rpg.getTempoStacks() == 3) {
                    // Grant Strength I
                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                            net.minecraft.world.effect.MobEffects.DAMAGE_BOOST,
                            999999, // Infinite duration (removed manually)
                            0,      // Strength I
                            false,  // Not ambient
                            false,  // Don't show particles
                            true    // Show icon
                    ));
                    rpg.setTempoActive(true);

                    // Visual feedback
                    player.level().playSound(null, player.blockPosition(),
                            net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.5f);

                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§6⚔ TEMPO ACTIVE! §7(Strength I)"));

                } else if (rpg.getTempoStacks() >= 4) {
                    // 4th hit - remove Strength and reset
                    player.removeEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST);
                    rpg.resetTempo();

                    // Visual feedback
                    player.level().playSound(null, player.blockPosition(),
                            net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP,
                            net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 0.8f);

                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7TEMPO reset"));
                }
            }
        }
    }
}