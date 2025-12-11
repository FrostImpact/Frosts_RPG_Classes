package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncMana;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;


public class ServerEvents {

    // --- TEMPORARY FIX: SET CLASS ON LOGIN ---
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Only run this on the server side
        if (!event.getEntity().level().isClientSide) {

            PlayerRPGData rpg = event.getEntity().getData(ModAttachments.PLAYER_RPG);

            // 1. Force set the class
            rpg.setCurrentClass("BLADEDANCER");

            // 2. Send a confirmation message
            event.getEntity().sendSystemMessage(Component.literal("§a[TEMP FIX] Your class is set to: §6BLADEDANCER"));
        }
    }

    // --- PLAYER TICK EVENT (COMBINED VERSION) ---
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            if (!player.level().isClientSide) {

                PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

                // Tick ALL ability cooldowns at once
                rpg.tickCooldowns();

                // Handle Blade Dance
                if (rpg.isBladeDanceActive()) {
                    // Tick down duration
                    rpg.setBladeDanceTicks(rpg.getBladeDanceTicks() - 1);

                    // Tick down damage cooldown
                    if (rpg.getBladeDanceDamageCooldown() > 0) {
                        rpg.setBladeDanceDamageCooldown(rpg.getBladeDanceDamageCooldown() - 1);
                    }

                    // Deal damage to nearby enemies every 10 ticks (0.5 seconds)
                    if (player.level().getGameTime() % 10 == 0 && rpg.getBladeDanceDamageCooldown() == 0) {
                        double radius = 3.0; // 3 block radius

                        player.level().getEntitiesOfClass(
                                net.minecraft.world.entity.LivingEntity.class,
                                player.getBoundingBox().inflate(radius),
                                entity -> entity != player && entity.isAlive()
                        ).forEach(entity -> {
                            // Deal damage (2 damage per blade)
                            float damage = 2.0f * rpg.getBladeDanceBlades();
                            entity.hurt(player.damageSources().playerAttack(player), damage);

                            // Play hit sound
                            player.level().playSound(null, entity.blockPosition(),
                                    net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_WEAK,
                                    net.minecraft.sounds.SoundSource.PLAYERS, 0.5f, 1.2f);
                        });

                        // Set cooldown to prevent immediate re-hit
                        rpg.setBladeDanceDamageCooldown(5);
                    }

                    // Update sword positions every tick
                    if (!rpg.getBladeDanceSwordIds().isEmpty()) {
                        double angle = (player.level().getGameTime() * 0.15) % (2 * Math.PI);
                        int swordIndex = 0;

                        for (Integer swordId : rpg.getBladeDanceSwordIds()) {
                            net.minecraft.world.entity.Entity entity = player.level().getEntity(swordId);

                            if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand sword) {
                                double bladeAngle = angle + (swordIndex * 2 * Math.PI / rpg.getBladeDanceBlades());
                                double x = player.getX() + Math.cos(bladeAngle) * 1.8;
                                double y = player.getY();
                                double z = player.getZ() + Math.sin(bladeAngle) * 1.8;

                                sword.setPos(x, y, z);
                                sword.setYRot((float) Math.toDegrees(bladeAngle + Math.PI / 2));

                                swordIndex++;
                            }
                        }
                    }

                    // Check if duration ended
                    if (rpg.getBladeDanceTicks() <= 0) {
                        // Remove sword entities
                        for (Integer swordId : rpg.getBladeDanceSwordIds()) {
                            net.minecraft.world.entity.Entity entity = player.level().getEntity(swordId);
                            if (entity != null) {
                                entity.discard();
                            }
                        }
                        rpg.clearBladeDanceSwords();

                        // If all blades remain, grant Swiftness II for 2 seconds
                        if (rpg.getBladeDanceBlades() == 4) {
                            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                                    40, // 2 seconds
                                    1   // Swiftness II
                            ));
                            player.sendSystemMessage(Component.literal("§aPerfect Blade Dance! +Swiftness II"));
                        }

                        // Deactivate
                        rpg.setBladeDanceActive(false);
                        rpg.setBladeDanceBlades(0);

                        // Play end sound
                        player.level().playSound(null, player.blockPosition(),
                                net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_NODAMAGE,
                                net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.8f);
                    }
                }

                // Mana Regeneration (1 mana per second = every 20 ticks)
                if (player.level().getGameTime() % 5 == 0) {
                    if (rpg.getMana() < rpg.getMaxMana()) {
                        rpg.useMana(-1); // Negative to add mana
                    }
                }

                // --- SEND ACTIONBAR STATUS AND SYNC COOLDOWNS EVERY 5 TICKS (4 times per second) ---
                if (player.level().getGameTime() % 5 == 0) {
                    int currentMana = rpg.getMana();
                    int maxMana = rpg.getMaxMana();

                    // Build the custom status string for action bar
                    String status = String.format("§bMANA: §f%d / %d §7| §cHP: §f%d / %d §7| §6CLASS: §f%s",
                            currentMana, maxMana,
                            (int)player.getHealth(), (int)player.getMaxHealth(),
                            rpg.getCurrentClass());

                    // Send the action bar packet
                    ModMessages.sendToPlayer(new PacketSyncMana(status), player);

                    // Send cooldown sync packet
                    ModMessages.sendToPlayer(new net.Frostimpact.rpgclasses.networking.packet.PacketSyncCooldowns(
                            rpg.getAllCooldowns(),
                            currentMana,
                            maxMana
                    ), player);
                }
            }
        }
    }
}