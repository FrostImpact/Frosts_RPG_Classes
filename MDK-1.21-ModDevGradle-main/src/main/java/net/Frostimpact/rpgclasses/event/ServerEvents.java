package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncMana;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncClass;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class ServerEvents {

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            // Force set the class and initialize
            rpg.setCurrentClass("MANAFORGE");
            rpg.setJuggernautShieldMode(true);
            rpg.setJuggernautCharge(0);

            // Set mana to max
            rpg.setMana(rpg.getMaxMana());

            // IMPORTANT: Sync class to client immediately
            ModMessages.sendToPlayer(new PacketSyncClass("MANAFORGE"), player);

            // Send confirmation
            player.sendSystemMessage(Component.literal("§a[RPG Classes] Class set to: §6MANAFORGE"));

            System.out.println("[SERVER] Player " + player.getName().getString() + " logged in. Class: " + rpg.getCurrentClass());
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            // Re-sync class after respawn
            ModMessages.sendToPlayer(new PacketSyncClass(rpg.getCurrentClass()), player);

            System.out.println("[SERVER] Player " + player.getName().getString() + " respawned. Re-syncing class: " + rpg.getCurrentClass());
        }
    }

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
                        if (!rpg.isParrySuccessful()) {
                            player.sendSystemMessage(Component.literal("§7Parry window expired"));
                        }
                    }
                }

                // --- FINAL WALTZ HANDLING ---
                if (rpg.isFinalWaltzActive()) {
                    rpg.setFinalWaltzTicks(rpg.getFinalWaltzTicks() - 1);

                    if (player.level().getGameTime() % 10 == 0) {
                        ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                                net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                                player.getX(), player.getY() + 1, player.getZ(),
                                3, 0.3, 0.5, 0.3, 0.05
                        );
                    }

                    if (rpg.getFinalWaltzTicks() <= 0) {
                        rpg.setFinalWaltzActive(false);
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
                    if (rpg.isFinalWaltzActive()) {
                        int overflow = rpg.getFinalWaltzOverflow();
                        int extraBlades = overflow / 2;

                        if (extraBlades > 0) {
                            int newBladeCount = rpg.getBladeDanceBlades() + extraBlades;
                            rpg.setBladeDanceBlades(newBladeCount);

                            player.sendSystemMessage(Component.literal("§5✦ FINAL WALTZ consumed! §6+" + extraBlades + " extra blades!"));

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

                        rpg.setFinalWaltzActive(false);
                        rpg.setFinalWaltzTicks(0);
                        rpg.setFinalWaltzOverflow(0);
                        player.removeEffect(MobEffects.DAMAGE_BOOST);
                        rpg.resetTempo();
                    }

                    rpg.setBladeDanceTicks(rpg.getBladeDanceTicks() - 1);

                    if (rpg.getBladeDanceDamageCooldown() > 0) {
                        rpg.setBladeDanceDamageCooldown(rpg.getBladeDanceDamageCooldown() - 1);
                    }

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

                // Mana Regeneration
                if (player.level().getGameTime() % 5 == 0) {
                    if (rpg.getMana() < rpg.getMaxMana()) {
                        rpg.useMana(-1);
                    }
                }

                // Sync every 5 ticks - NOW INCLUDING ARCANA
                if (player.level().getGameTime() % 5 == 0) {
                    int currentMana = rpg.getMana();
                    int maxMana = rpg.getMaxMana();

                    String status = String.format("§bMANA: §f%d / %d §7| §cHP: §f%d / %d §7| §6CLASS: §f%s",
                            currentMana, maxMana,
                            (int)player.getHealth(), (int)player.getMaxHealth(),
                            rpg.getCurrentClass());

                    ModMessages.sendToPlayer(new PacketSyncMana(status), player);

                    // NEW: Include arcana in the sync packet
                    ModMessages.sendToPlayer(new net.Frostimpact.rpgclasses.networking.packet.PacketSyncCooldowns(
                            rpg.getAllCooldowns(),
                            currentMana,
                            maxMana,
                            rpg.getJuggernautCharge(),
                            rpg.getJuggernautMaxCharge(),
                            rpg.isJuggernautShieldMode(),
                            rpg.getManaforgeArcana() // NEW: Pass arcana value
                    ), player);
                }
            }
        }
    }
}