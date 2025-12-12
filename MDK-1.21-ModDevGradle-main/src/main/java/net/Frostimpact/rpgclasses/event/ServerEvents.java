package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncMana;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
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
            rpg.setCurrentClass("JUGGERNAUT");

            // 2. Send a confirmation message
            event.getEntity().sendSystemMessage(Component.literal("§a[TEMP FIX] Your class is set to: §6JUGGERNAUT"));
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

                // --- PARRY HANDLING ---
                if (rpg.isParryActive()) {
                    rpg.setParryTicks(rpg.getParryTicks() - 1);

                    if (rpg.getParryTicks() <= 0) {
                        rpg.setParryActive(false);

                        // If parry wasn't successful, provide feedback
                        if (!rpg.isParrySuccessful()) {
                            player.sendSystemMessage(Component.literal("§7Parry window expired"));
                        }
                    }
                }

                // --- FINAL WALTZ HANDLING ---
                if (rpg.isFinalWaltzActive()) {
                    rpg.setFinalWaltzTicks(rpg.getFinalWaltzTicks() - 1);

                    // Visual feedback - particle trail
                    if (player.level().getGameTime() % 10 == 0) {
                        ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                                net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                                player.getX(), player.getY() + 1, player.getZ(),
                                3, 0.3, 0.5, 0.3, 0.05
                        );
                    }

                    // Check if duration ended
                    if (rpg.getFinalWaltzTicks() <= 0) {
                        // End Final Waltz
                        rpg.setFinalWaltzActive(false);

                        // Remove any active TEMPO effects
                        player.removeEffect(MobEffects.DAMAGE_BOOST);
                        rpg.resetTempo();
                        rpg.setFinalWaltzOverflow(0);

                        player.level().playSound(null, player.blockPosition(),
                                SoundEvents.PLAYER_ATTACK_NODAMAGE,
                                SoundSource.PLAYERS, 1.0f, 0.8f);

                        player.sendSystemMessage(Component.literal("§5✦ FINAL WALTZ ended"));
                    }
                }

                // Handle Blade Dance
                if (rpg.isBladeDanceActive()) {
                    // Check if being used during Final Waltz
                    if (rpg.isFinalWaltzActive()) {
                        // End Final Waltz when Blade Dance is activated
                        int overflow = rpg.getFinalWaltzOverflow();
                        int extraBlades = overflow / 2;

                        if (extraBlades > 0) {
                            // Add extra blades
                            int newBladeCount = rpg.getBladeDanceBlades() + extraBlades;
                            rpg.setBladeDanceBlades(newBladeCount);

                            player.sendSystemMessage(Component.literal("§5✦ FINAL WALTZ consumed! §6+" + extraBlades + " extra blades!"));

                            // Spawn extra blade entities
                            for (int i = 0; i < extraBlades; i++) {
                                net.minecraft.world.entity.decoration.ArmorStand swordStand = new net.minecraft.world.entity.decoration.ArmorStand(
                                        net.minecraft.world.entity.EntityType.ARMOR_STAND,
                                        player.level()
                                );

                                swordStand.setPos(player.getX(), player.getY() - 1, player.getZ());
                                swordStand.setInvisible(true);
                                swordStand.setNoGravity(true);
                                swordStand.setInvulnerable(true);
                                swordStand.setShowArms(false);
                                swordStand.setNoBasePlate(true);
                                swordStand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));

                                player.level().addFreshEntity(swordStand);
                                rpg.addBladeDanceSword(swordStand.getId());
                            }
                        }

                        // End Final Waltz
                        rpg.setFinalWaltzActive(false);
                        rpg.setFinalWaltzTicks(0);
                        rpg.setFinalWaltzOverflow(0);
                        player.removeEffect(MobEffects.DAMAGE_BOOST);
                        rpg.resetTempo();
                    }

                    // Tick down duration
                    rpg.setBladeDanceTicks(rpg.getBladeDanceTicks() - 1);

                    // Tick down damage cooldown
                    if (rpg.getBladeDanceDamageCooldown() > 0) {
                        rpg.setBladeDanceDamageCooldown(rpg.getBladeDanceDamageCooldown() - 1);
                    }

                    // Deal damage to nearby enemies every 10 ticks (0.5 seconds)
                    if (player.level().getGameTime() % 10 == 0 && rpg.getBladeDanceDamageCooldown() == 0) {
                        double radius = 3.0;

                        player.level().getEntitiesOfClass(
                                net.minecraft.world.entity.LivingEntity.class,
                                player.getBoundingBox().inflate(radius),
                                entity -> entity != player && entity.isAlive()
                        ).forEach(entity -> {
                            float damage = 2.0f * rpg.getBladeDanceBlades();
                            entity.hurt(player.damageSources().playerAttack(player), damage);

                            player.level().playSound(null, entity.blockPosition(),
                                    SoundEvents.PLAYER_ATTACK_WEAK,
                                    SoundSource.PLAYERS, 0.5f, 1.2f);
                        });

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
                        for (Integer swordId : rpg.getBladeDanceSwordIds()) {
                            net.minecraft.world.entity.Entity entity = player.level().getEntity(swordId);
                            if (entity != null) {
                                entity.discard();
                            }
                        }
                        rpg.clearBladeDanceSwords();

                        if (rpg.getBladeDanceBlades() == 4) {
                            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    MobEffects.MOVEMENT_SPEED,
                                    40,
                                    1
                            ));
                            player.sendSystemMessage(Component.literal("§aPerfect Blade Dance! +Swiftness II"));
                        }

                        rpg.setBladeDanceActive(false);
                        rpg.setBladeDanceBlades(0);

                        player.level().playSound(null, player.blockPosition(),
                                SoundEvents.PLAYER_ATTACK_NODAMAGE,
                                SoundSource.PLAYERS, 1.0f, 0.8f);
                    }
                }

                // Mana Regeneration (1 mana per second = every 20 ticks)
                if (player.level().getGameTime() % 5 == 0) {
                    if (rpg.getMana() < rpg.getMaxMana()) {
                        rpg.useMana(-1);
                    }
                }

                // --- SEND ACTIONBAR STATUS AND SYNC COOLDOWNS EVERY 5 TICKS ---
                if (player.level().getGameTime() % 5 == 0) {
                    int currentMana = rpg.getMana();
                    int maxMana = rpg.getMaxMana();

                    String status = String.format("§bMANA: §f%d / %d §7| §cHP: §f%d / %d §7| §6CLASS: §f%s",
                            currentMana, maxMana,
                            (int)player.getHealth(), (int)player.getMaxHealth(),
                            rpg.getCurrentClass());

                    ModMessages.sendToPlayer(new PacketSyncMana(status), player);

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