package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncMana;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncClass;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;

public class ServerEvents {

    // Fracture Line explosion particle colors (cached for performance)
    private static final DustParticleOptions LIGHT_BLUE_DUST =
            new DustParticleOptions(new Vector3f(0.39f, 0.78f, 1.0f), 2.0f);
    private static final DustParticleOptions WHITE_DUST =
            new DustParticleOptions(new Vector3f(0.9f, 0.95f, 1.0f), 2.5f);
    private static final DustParticleOptions CYAN_DUST =
            new DustParticleOptions(new Vector3f(0.0f, 0.8f, 0.9f), 1.5f);

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            // Force set the class and initialize
            rpg.setCurrentClass("RULER"); // Set to RULER for testing
            rpg.setJuggernautShieldMode(true);
            rpg.setJuggernautCharge(0);

            // IMPORTANT: Initialize banner position at player's feet + 1 block
            rpg.setRulerBannerPosition(player.position().add(0, 1, 0));
            rpg.setRulerRallyActive(false);
            rpg.setRulerRallyTicks(0);

            // Set mana to max
            rpg.setMana(rpg.getMaxMana());

            // IMPORTANT: Sync class to client immediately
            ModMessages.sendToPlayer(new PacketSyncClass("RULER"), player);

            // Send confirmation
            player.sendSystemMessage(Component.literal("§a[RPG Classes] Class set to: " + rpg.getCurrentClass()));

            System.out.println("[SERVER] Player " + player.getName().getString() + " logged in. Class: " + rpg.getCurrentClass());
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            // Re-initialize banner position after respawn
            if (rpg.getCurrentClass().equals("RULER")) {
                rpg.setRulerBannerPosition(player.position().add(0, 1, 0));
                rpg.setRulerRallyActive(false);
                rpg.setRulerRallyTicks(0);
            }

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

                // --- MIRAGE HANDLING ---
                if (rpg.getCurrentClass().equals("MIRAGE")) {
                    // Shadowstep reactivation window countdown
                    if (rpg.isMirageShadowstepActive()) {
                        rpg.setMirageShadowstepTicks(rpg.getMirageShadowstepTicks() - 1);
                        if (rpg.getMirageShadowstepTicks() <= 0) {
                            rpg.setMirageShadowstepActive(false);
                            rpg.setAbilityCooldown("shadowstep", 240); // 12s cooldown
                            player.sendSystemMessage(Component.literal("§9Shadowstep §7window expired"));
                        }
                    }

                    // Fracture Line charging
                    if (rpg.isMirageFractureLineCharging()) {
                        rpg.setMirageFractureLineTicks(rpg.getMirageFractureLineTicks() - 1);
                        if (rpg.getMirageFractureLineTicks() <= 0) {
                            // Start dash
                            net.Frostimpact.rpgclasses.ability.MIRAGE.FractureLineAbility.startDash(player, rpg);
                        }
                    }

                    // Fracture Line dash with enhanced particle trail
                    if (rpg.isMirageFractureLineActive()) {
                        rpg.setMirageFractureLineTicks(rpg.getMirageFractureLineTicks() - 1);

                        // Spawn dramatic particles along the dash path
                        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            serverLevel.sendParticles(
                                    net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                                    player.getX(), player.getY() + 1, player.getZ(),
                                    15, 0.3, 0.5, 0.3, 0.05
                            );
                            serverLevel.sendParticles(
                                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                                    player.getX(), player.getY() + 1, player.getZ(),
                                    10, 0.2, 0.4, 0.2, 0.02
                            );
                            serverLevel.sendParticles(
                                    net.minecraft.core.particles.ParticleTypes.WITCH,
                                    player.getX(), player.getY() + 0.5, player.getZ(),
                                    8, 0.3, 0.3, 0.3, 0.03
                            );
                        }

                        if (rpg.getMirageFractureLineTicks() <= 0) {
                            rpg.setMirageFractureLineActive(false);
                        }
                    }

                    // Handle afterimage explosions from Fracture Line
                    java.util.List<Integer> afterimageIds = new java.util.ArrayList<>(rpg.getMirageAfterimageIds());
                    java.util.List<Integer> idsToRemove = new java.util.ArrayList<>();
                    for (Integer id : afterimageIds) {
                        if (player.level().getEntity(id) instanceof net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity afterimage) {
                            if (afterimage.getPersistentData().getBoolean("fracture_explode")) {
                                int timer = afterimage.getPersistentData().getInt("fracture_timer");
                                timer--;
                                afterimage.getPersistentData().putInt("fracture_timer", timer);

                                if (timer <= 0) {
                                    // Explode with flashy and impactful visual effects
                                    net.minecraft.world.phys.Vec3 pos = afterimage.position();

                                    // Spawn dramatic explosion particles with velocity and spread (optimized particle count)
                                    if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                                        // Light blue dust explosion particles with velocity
                                        serverLevel.sendParticles(
                                                LIGHT_BLUE_DUST,
                                                pos.x, pos.y + 1, pos.z,
                                                40, 1.5, 1.5, 1.5, 0.3
                                        );

                                        // White/cyan flash particles for impact
                                        serverLevel.sendParticles(
                                                WHITE_DUST,
                                                pos.x, pos.y + 1, pos.z,
                                                30, 1.2, 1.2, 1.2, 0.25
                                        );

                                        // Explosion particles for flashy effect
                                        serverLevel.sendParticles(
                                                net.minecraft.core.particles.ParticleTypes.EXPLOSION,
                                                pos.x, pos.y + 1, pos.z,
                                                8, 0.5, 0.5, 0.5, 0.0
                                        );

                                        // Flash particles for bright burst
                                        serverLevel.sendParticles(
                                                net.minecraft.core.particles.ParticleTypes.FLASH,
                                                pos.x, pos.y + 1, pos.z,
                                                2, 0.1, 0.1, 0.1, 0.0
                                        );

                                        // End rod particles with velocity for dynamic effect
                                        serverLevel.sendParticles(
                                                net.minecraft.core.particles.ParticleTypes.END_ROD,
                                                pos.x, pos.y + 1, pos.z,
                                                25, 1.5, 1.5, 1.5, 0.4
                                        );

                                        // Cyan dust cloud spreading outward
                                        serverLevel.sendParticles(
                                                CYAN_DUST,
                                                pos.x, pos.y + 1, pos.z,
                                                35, 2.0, 2.0, 2.0, 0.35
                                        );

                                        // Glow particles for lingering effect
                                        serverLevel.sendParticles(
                                                net.minecraft.core.particles.ParticleTypes.GLOW,
                                                pos.x, pos.y + 1, pos.z,
                                                20, 1.0, 1.0, 1.0, 0.2
                                        );
                                    }

                                    player.level().explode(
                                            afterimage,
                                            pos.x, pos.y, pos.z,
                                            2.5f, // Slightly larger explosion
                                            net.minecraft.world.level.Level.ExplosionInteraction.NONE
                                    );
                                    afterimage.discard();
                                    idsToRemove.add(afterimage.getId());
                                }
                            }
                        }
                    }
                    // Remove exploded afterimages
                    for (Integer id : idsToRemove) {
                        rpg.getMirageAfterimageIds().remove(id);
                    }

                    // Clean up dead afterimages from the list
                    idsToRemove.clear();
                    afterimageIds = new java.util.ArrayList<>(rpg.getMirageAfterimageIds());
                    for (Integer id : afterimageIds) {
                        if (player.level().getEntity(id) == null) {
                            idsToRemove.add(id);
                        }
                    }
                    // Remove dead afterimages
                    for (Integer id : idsToRemove) {
                        rpg.getMirageAfterimageIds().remove(id);
                    }

                    // Handle recall - stop afterimages when they reach the recall position
                    if (rpg.isMirageRecallActive()) {
                        net.minecraft.world.phys.Vec3 recallPos = rpg.getMirageRecallPosition();
                        boolean allReached = true;
                        for (Integer id : rpg.getMirageAfterimageIds()) {
                            if (player.level().getEntity(id) instanceof net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity afterimage) {
                                double distance = afterimage.position().distanceTo(recallPos);
                                if (distance > 1.0) {
                                    allReached = false;
                                } else {
                                    afterimage.stopGliding();
                                }
                            }
                        }
                        if (allReached) {
                            rpg.setMirageRecallActive(false);
                        }
                    }
                }

                // Mana Regeneration
                if (player.level().getGameTime() % 5 == 0) {
                    if (rpg.getMana() < rpg.getMaxMana()) {
                        rpg.useMana(-1);
                    }
                }

                // Sync every 5 ticks
                if (player.level().getGameTime() % 5 == 0) {
                    int currentMana = rpg.getMana();
                    int maxMana = rpg.getMaxMana();

                    ModMessages.sendToPlayer(new net.Frostimpact.rpgclasses.networking.packet.PacketSyncCooldowns(
                            rpg.getAllCooldowns(),
                            currentMana,
                            maxMana,
                            rpg.getJuggernautCharge(),
                            rpg.getJuggernautMaxCharge(),
                            rpg.isJuggernautShieldMode(),
                            rpg.getManaforgeArcana(),
                            rpg.getTempoStacks(),
                            rpg.isTempoActive(),
                            rpg.getMarksmanSeekerCharges()
                    ), player);
                }
            }
        }
    }
}