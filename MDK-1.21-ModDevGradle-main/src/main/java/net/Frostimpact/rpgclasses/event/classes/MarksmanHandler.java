package net.Frostimpact.rpgclasses.event.classes;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType; // <--- ADDED: Import EntityType
import net.minecraft.world.entity.projectile.Arrow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class MarksmanHandler {

    private static final int MAX_SEEKER_CHARGES = 5;
    private static final int AIRBORNE_TICKS_FOR_CHARGE = 20; // 1 second airborne = 1 charge

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {

            // FIX: Use .getComponent() for modern attachments API
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            if (rpg == null || !rpg.getCurrentClass().equals("MARKSMAN")) return;

            ServerLevel level = player.serverLevel();

            // === GLIDE PASSIVE ===
            if (!player.onGround() && player.getDeltaMovement().y < 0) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING, 2, 0, false, false, true
                ));
            }

            // === AERIAL AFFINITY - Charge gain while airborne ===
            if (!player.onGround()) {
                int airTicks = rpg.getMarksmanAirborneTicks();
                rpg.setMarksmanAirborneTicks(airTicks + 1);

                // Gain charge every second airborne
                if (airTicks > 0 && airTicks % AIRBORNE_TICKS_FOR_CHARGE == 0) {
                    int currentCharges = rpg.getMarksmanSeekerCharges();
                    if (currentCharges < MAX_SEEKER_CHARGES) {
                        rpg.addMarksmanSeekerCharge();

                        // Visual feedback
                        level.sendParticles(
                                ParticleTypes.GLOW,
                                player.getX(), player.getY() + 1, player.getZ(),
                                5, 0.3, 0.3, 0.3, 0.05
                        );

                        level.playSound(null, player.blockPosition(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3f, 1.8f);

                        player.sendSystemMessage(Component.literal(
                                "§a+ SEEKER §7[" + rpg.getMarksmanSeekerCharges() + "/" + MAX_SEEKER_CHARGES + "]"
                        ));

                        // Force immediate sync
                        net.Frostimpact.rpgclasses.networking.ModMessages.sendToPlayer(
                                new net.Frostimpact.rpgclasses.networking.packet.PacketSyncCooldowns(
                                        rpg.getAllCooldowns(),
                                        rpg.getMana(),
                                        rpg.getMaxMana(),
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

                // Airborne particle trail
                if (level.getGameTime() % 5 == 0) {
                    level.sendParticles(
                            ParticleTypes.GLOW,
                            player.getX() + (Math.random() - 0.5) * 0.5,
                            player.getY() + 0.2,
                            player.getZ() + (Math.random() - 0.5) * 0.5,
                            1, 0, -0.1, 0, 0.02
                    );
                }
            } else {
                rpg.setMarksmanAirborneTicks(0);
            }

            // === ARROW RAIN EFFECT ===
            if (rpg.isArrowRainActive()) {
                int ticks = rpg.getArrowRainTicks();

                if (ticks > 0) {
                    rpg.setArrowRainTicks(ticks - 1);

                    net.minecraft.world.phys.Vec3 rainPos = rpg.getArrowRainPosition();
                    double radius = 8.0;

                    // Spawn arrows every 10 ticks
                    if (ticks % 10 == 0) {
                        // Spawn 5 arrows in random locations
                        for (int i = 0; i < 5; i++) {
                            double angle = Math.random() * 2 * Math.PI;
                            double dist = Math.random() * radius;

                            double x = rainPos.x + Math.cos(angle) * dist;
                            double z = rainPos.z + Math.sin(angle) * dist;
                            double y = rainPos.y + 20; // Spawn high above

                            // START OF FIX
                            // In modern Minecraft, use the constructor with EntityType and Level,
                            // then set the position.
                            Arrow arrow = new Arrow(EntityType.ARROW, level);
                            arrow.setOwner(player);
                            arrow.setPos(x, y, z); // Set the position after creation
                            // END OF FIX

                            arrow.setDeltaMovement(0, -2.0, 0); // Fall straight down fast
                            arrow.setBaseDamage(3.0);

                            level.addFreshEntity(arrow);
                        }
                    }

                    // Visual effects
                    if (ticks % 5 == 0) {
                        // Ground indicator circle
                        for (int i = 0; i < 20; i++) {
                            double angle = (2 * Math.PI * i) / 20;
                            double x = rainPos.x + Math.cos(angle) * radius;
                            double z = rainPos.z + Math.sin(angle) * radius;

                            level.sendParticles(
                                    ParticleTypes.GLOW,
                                    x, rainPos.y + 0.1, z,
                                    1, 0, 0, 0, 0
                            );
                        }

                        // Rain cloud particles
                        for (int i = 0; i < 10; i++) {
                            double x = rainPos.x + (Math.random() - 0.5) * radius * 2;
                            double z = rainPos.z + (Math.random() - 0.5) * radius * 2;

                            level.sendParticles(
                                    ParticleTypes.CLOUD,
                                    x, rainPos.y + 18, z,
                                    1, 0.2, 0, 0.2, 0.01
                            );
                        }
                    }

                    // Ambient sound
                    if (ticks % 20 == 0) {
                        level.playSound(null, rainPos.x, rainPos.y, rainPos.z,
                                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.5f, 0.8f);
                    }

                } else {
                    rpg.setArrowRainActive(false);
                    player.sendSystemMessage(Component.literal("§7Arrow rain ended."));
                }
            }
        }
    }
}