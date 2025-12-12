package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.ParticleTypes;
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
            ServerLevel serverLevel = (ServerLevel) player.level(); // Cast only once

            if (!rpg.getCurrentClass().equals("JUGGERNAUT")) return;

            // Handle charge decay in SHATTER mode
            if (!rpg.isJuggernautShieldMode() && rpg.isChargeDecaying()) {
                rpg.tickChargeDecay();

                if (rpg.getJuggernautCharge() == 0 && rpg.isJuggernautShieldMode()) {
                    player.removeEffect(MobEffects.DAMAGE_BOOST);
                    player.sendSystemMessage(Component.literal("§c⚠ Charge depleted → §b🛡 SHIELD MODE"));
                    player.level().playSound(null, player.blockPosition(),
                            SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.8f);
                }
            }

            // Apply Strength buffs in SHATTER mode
            if (!rpg.isJuggernautShieldMode()) {
                int charge = rpg.getJuggernautCharge();

                if (charge > 50) {
                    if (!player.hasEffect(MobEffects.DAMAGE_BOOST) ||
                            player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() < 1) {
                        player.removeEffect(MobEffects.DAMAGE_BOOST);
                        player.addEffect(new MobEffectInstance(
                                MobEffects.DAMAGE_BOOST, 999999, 1, false, false, true
                        ));
                    }
                } else if (charge > 0) {
                    if (!player.hasEffect(MobEffects.DAMAGE_BOOST) ||
                            player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() != 0) {
                        player.removeEffect(MobEffects.DAMAGE_BOOST);
                        player.addEffect(new MobEffectInstance(
                                MobEffects.DAMAGE_BOOST, 999999, 0, false, false, true
                        ));
                    }
                } else {
                    player.removeEffect(MobEffects.DAMAGE_BOOST);
                }
            } else {
                if (player.hasEffect(MobEffects.DAMAGE_BOOST)) {
                    MobEffectInstance effect = player.getEffect(MobEffects.DAMAGE_BOOST);
                    if (effect != null && effect.getDuration() > 900000) {
                        player.removeEffect(MobEffects.DAMAGE_BOOST);
                    }
                }
            }


            // Handle Fortify duration
            if (rpg.isFortifyActive()) {
                rpg.setFortifyTicks(rpg.getFortifyTicks() - 1);

                // Periodic shield particle effect (END_ROD)
                if (player.level().getGameTime() % 10 == 0) {
                    double radius = 1.5;
                    int particles = 8;
                    for (int i = 0; i < particles; i++) {
                        double angle = (2 * Math.PI * i) / particles;
                        double xOffset = Math.cos(angle) * radius;
                        double zOffset = Math.sin(angle) * radius;

                        serverLevel.sendParticles(
                                ParticleTypes.END_ROD,
                                player.getX() + xOffset,
                                player.getY() + 1.0,
                                player.getZ() + zOffset,
                                1, 0, 0, 0, 0
                        );
                    }
                }

                if (rpg.getFortifyTicks() <= 0) {
                    rpg.setFortifyActive(false);
                }
            }


            // Handle Leap landing
            if (rpg.isLeapActive()) {
                double currentY = player.getY();
                double startY = rpg.getLeapStartY();

                if (player.onGround() && currentY <= startY) {

                    // === LEAP LANDING: EARTHQUAKE EFFECT ===

                    // 1. Central Explosion burst
                    serverLevel.sendParticles(
                            ParticleTypes.EXPLOSION_EMITTER,
                            player.getX(), player.getY(), player.getZ(),
                            1, 0, 0, 0, 0
                    );

                    // 2. Large smoke plume rising up
                    serverLevel.sendParticles(
                            ParticleTypes.LARGE_SMOKE,
                            player.getX(), player.getY(), player.getZ(),
                            20, 2.0, 1.5, 2.0, 0.1
                    );

                    // 3. Expanding shockwave rings with ground cracks
                    int numRings = 4;
                    for (int ring = 1; ring <= numRings; ring++) {
                        int particlesPerRing = 12 * ring;
                        double ringRadius = 1.2 * ring;

                        for (int i = 0; i < particlesPerRing; i++) {
                            double angle = (2 * Math.PI * i) / particlesPerRing;
                            double xOffset = Math.cos(angle) * ringRadius;
                            double zOffset = Math.sin(angle) * ringRadius;

                            // Ground Crack (CRIT particles)
                            serverLevel.sendParticles(
                                    ParticleTypes.CRIT,
                                    player.getX() + xOffset,
                                    player.getY() + 0.1,
                                    player.getZ() + zOffset,
                                    1, 0, 0, 0, 0
                            );

                            // Dust clouds (POOF) between rings
                            if (ring < numRings && i % 4 == 0) {
                                serverLevel.sendParticles(
                                        ParticleTypes.POOF,
                                        player.getX() + xOffset * 0.9,
                                        player.getY() + 0.2,
                                        player.getZ() + zOffset * 0.9,
                                        1, 0.1, 0.1, 0.1, 0.05
                                );
                            }
                        }
                    }

                    if (rpg.isLeapShieldMode()) {
                        double radius = 5.0; // Buff radius

                        // === BUFF CIRCLE INDICATOR & ALLY BUFFS ===
                        int circleParticles = 50;
                        for (int i = 0; i < circleParticles; i++) {
                            double angle = (2 * Math.PI * i) / circleParticles;
                            double xOffset = Math.cos(angle) * radius;
                            double zOffset = Math.sin(angle) * radius;

                            serverLevel.sendParticles(
                                    ParticleTypes.END_ROD,
                                    player.getX() + xOffset,
                                    player.getY() + 0.1,
                                    player.getZ() + zOffset,
                                    1, 0, 0, 0, 0
                            );
                        }

                        player.level().getEntitiesOfClass(
                                net.minecraft.world.entity.LivingEntity.class,
                                player.getBoundingBox().inflate(radius),
                                ally -> ally.isAlive()
                        ).forEach(ally -> {
                            ally.removeEffect(MobEffects.ABSORPTION);
                            ally.addEffect(new MobEffectInstance(
                                    MobEffects.ABSORPTION, 999999, 2, false, true, true
                            ));

                            serverLevel.sendParticles(
                                    ParticleTypes.TOTEM_OF_UNDYING,
                                    ally.getX(), ally.getY() + ally.getBbHeight() / 2.0, ally.getZ(),
                                    10, 0.2, 0.2, 0.2, 0.05
                            );

                            if (ally != player) {
                                ally.sendSystemMessage(Component.literal("§a+6 Absorption §7from Juggernaut Leap"));
                            } else {
                                player.sendSystemMessage(Component.literal("§a+6 Absorption §7applied to yourself"));
                            }
                        });
                    }

                    rpg.setLeapActive(false);
                    rpg.setLeapStartY(0);
                }
            }

            // === CRUSH IMPACT LOGIC ===
            if (rpg.isCrushActive()) {
                if (player.onGround() && player.getDeltaMovement().y <= 0.01) {

                    performCrushImpact(player, serverLevel, rpg.isCrushPowered());

                    // Reset State
                    rpg.setCrushActive(false);
                    rpg.setCrushPowered(false);
                }
            }
        }
    }

    // === HELPER METHOD FOR CRUSH IMPACT ===
    private static void performCrushImpact(ServerPlayer player, ServerLevel level, boolean isPowered) {
        double radius = 5.0;
        float damage = isPowered ? 10.0f : 6.0f;

        // 1. DAMAGE ENEMIES
        level.getEntitiesOfClass(
                net.minecraft.world.entity.LivingEntity.class,
                player.getBoundingBox().inflate(radius),
                entity -> entity != player && entity.isAlive()
        ).forEach(entity -> {
            entity.hurt(player.damageSources().playerAttack(player), damage);
            if (isPowered) {
                entity.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
            }
        });

        // 2. CENTRAL EXPLOSION
        //level.sendParticles(
                //ParticleTypes.EXPLOSION_EMITTER, // Huge explosion visual
                //player.getX(), player.getY(), player.getZ(),
                //1, 0, 0, 0, 0
        //);

        // 3. RADIATING CRIT SHOCKWAVE
        // Launches particles outward from center
        int waveParticles = 150;
        for (int i = 0; i < waveParticles; i++) {
            double angle = (2 * Math.PI * i) / waveParticles;

            // Start at player feet
            double startX = player.getX();
            double startZ = player.getZ();

            // High velocity to radiate outward quickly
            double velocityX = Math.cos(angle) * 1.5; // Fast speed
            double velocityZ = Math.sin(angle) * 1.5;

            level.sendParticles(
                    ParticleTypes.CRIT,
                    startX, player.getY() + 0.1, startZ,
                    0, // Count 0 for velocity control
                    velocityX, 0.05, velocityZ, // XYZ Velocity (0.05 Y for slight lift)
                    1.0
            );
        }

        // 4. STATIC EDGE RING (The "End" of the AOE)
        // Creates a solid ring exactly at the max radius so players see the limit
        int ringParticles = 100;
        for (int i = 0; i < ringParticles; i++) {
            double angle = (2 * Math.PI * i) / ringParticles;
            double ringX = player.getX() + Math.cos(angle) * radius;
            double ringZ = player.getZ() + Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.CRIT, // Matching particle type
                    ringX, player.getY() + 0.1, ringZ,
                    1, 0, 0, 0, 0 // No velocity, just sits there
            );
        }


    }

    @SubscribeEvent
    public static void onPlayerDamaged(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            if (!rpg.getCurrentClass().equals("JUGGERNAUT")) return;

            if (rpg.isJuggernautShieldMode()) {
                float damage = event.getAmount();
                int chargeGain = (int) (damage * 2);

                if (rpg.isFortifyActive()) {
                    chargeGain += 10;

                }

                int oldCharge = rpg.getJuggernautCharge();
                rpg.addJuggernautCharge(chargeGain);
                int newCharge = rpg.getJuggernautCharge();

                if (player.level().getGameTime() % 20 == 0) {
                    player.sendSystemMessage(Component.literal("§b+" + (newCharge - oldCharge) + " CHARGE §7[" + newCharge + "]"));
                }
            }
        }
    }
}