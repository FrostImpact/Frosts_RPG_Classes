package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.ability.MANAFORGE.SurgeAbility;
import net.Frostimpact.rpgclasses.ability.MANAFORGE.OpenRiftAbility;
import net.Frostimpact.rpgclasses.ability.MANAFORGE.CoalescenceAbility;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManaforgeHandler {

    private static final int ARCANA_DECAY_TICKS = 200; // 10 seconds without attacking

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            if (!rpg.getCurrentClass().equals("MANAFORGE")) return;

            ServerLevel level = player.serverLevel();

            // === HANDLE SURGE CHARGING/FIRING ===
            if (rpg.isSurgeActive()) {
                SurgeAbility.tickSurge(player, rpg);
            }

            // === HANDLE RIFT ===
            if (rpg.isRiftActive()) {
                OpenRiftAbility.tickRift(player, rpg);
            }

            // === HANDLE COALESCENCE ===
            if (rpg.isCoalescenceActive()) {
                CoalescenceAbility.tickCoalescence(player, rpg);
            }

            // === ARCANA DECAY ===
            int arcana = rpg.getManaforgeArcana();
            int timeSinceAttack = rpg.getManaforgeLastAttackTick();

            // Decay if max ARCANA reached
            if (arcana >= 100) {
                rpg.setManaforgeArcana(Math.max(0, arcana - 1));

                // Leak particles when at max
                if (level.getGameTime() % 5 == 0) {
                    level.sendParticles(
                            ParticleTypes.DRAGON_BREATH,
                            player.getX() + (Math.random() - 0.5),
                            player.getY() + 1 + Math.random(),
                            player.getZ() + (Math.random() - 0.5),
                            1, 0, 0.1, 0, 0.02
                    );
                }
            }

            // Decay if no attack in 10 seconds
            if (timeSinceAttack >= ARCANA_DECAY_TICKS && arcana > 0) {
                rpg.setManaforgeArcana(Math.max(0, arcana - 1));

                // Dissipating particles
                if (level.getGameTime() % 10 == 0) {
                    level.sendParticles(
                            ParticleTypes.WITCH,
                            player.getX(),
                            player.getY() + 1.5,
                            player.getZ(),
                            2, 0.3, 0.3, 0.3, 0.05
                    );
                }
            }

            // Increment timer
            rpg.setManaforgeLastAttackTick(timeSinceAttack + 1);

            // === VISUAL EFFECTS FOR ARCANA ===
            if (arcana > 0 && level.getGameTime() % 20 == 0) {
                // Ambient purple aura based on ARCANA level
                float intensity = arcana / 100.0f;
                int particleCount = (int)(intensity * 5);

                for (int i = 0; i < particleCount; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double radius = 0.5;
                    double x = player.getX() + Math.cos(angle) * radius;
                    double z = player.getZ() + Math.sin(angle) * radius;
                    double y = player.getY() + 0.5 + (Math.random() * 1.5);

                    level.sendParticles(
                            ParticleTypes.ENCHANT,
                            x, y, z,
                            0, 0, 0.1, 0, 0.05
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            if (!rpg.getCurrentClass().equals("MANAFORGE")) return;

            // ACCUMULATION PASSIVE - Gain ARCANA on hit
            if (!rpg.isCoalescenceActive()) {
                int currentArcana = rpg.getManaforgeArcana();

                // Gain 5 ARCANA per hit
                int newArcana = Math.min(100, currentArcana + 5);
                rpg.setManaforgeArcana(newArcana);

                // Reset decay timer
                rpg.setManaforgeLastAttackTick(0);

                // Visual feedback
                if (newArcana % 10 == 0 || newArcana == 100) {
                    ServerLevel level = player.serverLevel();

                    level.sendParticles(
                            ParticleTypes.DRAGON_BREATH,
                            player.getX(), player.getY() + 1.5, player.getZ(),
                            3, 0.2, 0.2, 0.2, 0.05
                    );

                    // Sound feedback at milestones
                    if (newArcana == 100) {
                        level.playSound(null, player.blockPosition(),
                                net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP,
                                net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 2.0f);

                        player.sendSystemMessage(Component.literal(
                                "§5✦ MAX ARCANA! §7[100/100]"
                        ));
                    } else if (newArcana == 50) {
                        player.sendSystemMessage(Component.literal(
                                "§5ARCANA: §7[" + newArcana + "/100]"
                        ));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerDamaged(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            if (!rpg.getCurrentClass().equals("MANAFORGE")) return;

            // COALESCENCE - Store damage instead of taking it
            if (rpg.isCoalescenceActive()) {
                float damage = event.getAmount();
                float currentStored = rpg.getCoalescenceStoredDamage();
                rpg.setCoalescenceStoredDamage(currentStored + damage);

                // Cancel the damage
                event.setCanceled(true);

                // Visual feedback
                ServerLevel level = player.serverLevel();

                level.sendParticles(
                        ParticleTypes.WITCH,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        10, 0.4, 0.4, 0.4, 0.1
                );

                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        5, 0.3, 0.3, 0.3, 0.2
                );

                level.playSound(null, player.blockPosition(),
                        net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.8f);

                player.sendSystemMessage(Component.literal(
                        "§5✦ " + String.format("%.1f", damage) + " damage absorbed!"
                ));
            }
        }
    }
}