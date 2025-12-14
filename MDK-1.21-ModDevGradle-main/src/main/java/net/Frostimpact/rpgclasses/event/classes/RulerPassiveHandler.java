package net.Frostimpact.rpgclasses.event.classes;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.entity.summon.KnightSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.ArcherSummonEntity;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class RulerPassiveHandler {

    private static final double BANNER_RADIUS = 12.0;
    private static final int DEMORALIZE_DEATH_TICKS = 200; // 10 seconds

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            if (!rpg.getCurrentClass().equals("RULER")) return;

            ServerLevel level = player.serverLevel();

            // === KING'S BANNER POSITION UPDATE ===
            if (!rpg.isRulerRallyActive()) {
                rpg.setRulerBannerPosition(player.position().add(0, 1, 0));
            } else {
                // Tick down rally duration
                int rallyTicks = rpg.getRulerRallyTicks();
                if (rallyTicks > 0) {
                    rpg.setRulerRallyTicks(rallyTicks - 1);
                } else {
                    // Rally ended - move banner back to player
                    rpg.setRulerRallyActive(false);
                    rpg.setRulerBannerPosition(player.position().add(0, 1, 0));

                    player.sendSystemMessage(Component.literal("§6⚔ RALLY ended! Banner returns."));

                    level.playSound(null, player.blockPosition(),
                            SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 1.2f);
                }
            }

            Vec3 bannerPos = rpg.getRulerBannerPosition();

            // === ENHANCED BANNER VISUAL ===
            if (level.getGameTime() % 5 == 0) { // Render every 5 ticks for better performance
                renderEnhancedBanner(level, bannerPos, level.getGameTime());
            }

            // === DEMORALIZE SYSTEM ===
            List<LivingEntity> allSummons = level.getEntitiesOfClass(
                    LivingEntity.class,
                    player.getBoundingBox().inflate(100),
                    entity -> entity instanceof KnightSummonEntity || entity instanceof ArcherSummonEntity
            );

            for (LivingEntity summon : allSummons) {
                double distToBanner = summon.position().distanceTo(bannerPos);
                boolean inRange = distToBanner <= BANNER_RADIUS;

                if (summon instanceof KnightSummonEntity knight) {
                    handleDemoralizeForSummon(knight, inRange, level);
                } else if (summon instanceof ArcherSummonEntity archer) {
                    handleDemoralizeForSummon(archer, inRange, level);
                }
            }
        }
    }

    private static void handleDemoralizeForSummon(LivingEntity summon, boolean inRange, ServerLevel level) {
        boolean hasGlory = summon.hasEffect(MobEffects.MOVEMENT_SPEED) &&
                summon.hasEffect(MobEffects.ABSORPTION);

        if (hasGlory) {
            return;
        }

        if (!inRange) {
            if (!summon.getPersistentData().getBoolean("demoralized")) {
                summon.getPersistentData().putBoolean("demoralized", true);
                summon.getPersistentData().putInt("demoralize_ticks", DEMORALIZE_DEATH_TICKS);

                level.sendParticles(
                        ParticleTypes.ASH,
                        summon.getX(), summon.getY() + summon.getBbHeight() / 2, summon.getZ(),
                        10, 0.3, 0.3, 0.3, 0.05
                );
            }

            int ticks = summon.getPersistentData().getInt("demoralize_ticks");
            if (ticks > 0) {
                summon.getPersistentData().putInt("demoralize_ticks", ticks - 1);

                if (ticks % 40 == 0) {
                    level.sendParticles(
                            ParticleTypes.SMOKE,
                            summon.getX(), summon.getY() + summon.getBbHeight(), summon.getZ(),
                            5, 0.2, 0.2, 0.2, 0.02
                    );
                }
            } else {
                level.sendParticles(
                        ParticleTypes.POOF,
                        summon.getX(), summon.getY() + summon.getBbHeight() / 2, summon.getZ(),
                        20, 0.3, 0.3, 0.3, 0.1
                );
                summon.discard();
            }
        } else {
            if (summon.getPersistentData().getBoolean("demoralized")) {
                summon.getPersistentData().putBoolean("demoralized", false);
                summon.getPersistentData().putInt("demoralize_ticks", 0);

                level.sendParticles(
                        ParticleTypes.GLOW,
                        summon.getX(), summon.getY() + summon.getBbHeight() / 2, summon.getZ(),
                        10, 0.3, 0.3, 0.3, 0.05
                );
            }
        }
    }

    /**
     * ENHANCED BANNER VISUAL - Much more impressive and visible
     */
    private static void renderEnhancedBanner(ServerLevel level, Vec3 bannerPos, long gameTime) {
        double time = gameTime * 0.05; // Slower rotation

        // === 1. GOLDEN GROUND CIRCLE (Main boundary indicator) ===
        int circleParticles = 80;
        for (int i = 0; i < circleParticles; i++) {
            double angle = (2 * Math.PI * i) / circleParticles;
            double x = bannerPos.x + Math.cos(angle) * BANNER_RADIUS;
            double z = bannerPos.z + Math.sin(angle) * BANNER_RADIUS;

            // Primary golden ring
            level.sendParticles(
                    ParticleTypes.FLAME,
                    x, bannerPos.y + 0.2, z,
                    1, 0, 0, 0, 0
            );

            // Secondary glow particles
            if (i % 2 == 0) {
                level.sendParticles(
                        ParticleTypes.END_ROD,
                        x, bannerPos.y + 0.3, z,
                        1, 0, 0, 0, 0
                );
            }
        }

        // === 2. INNER ROTATING RINGS (3 layers) ===
        /*
        for (int layer = 0; layer < 3; layer++) {
            double layerRadius = 4.0 - (layer * 1.2);
            int layerParticles = 30 - (layer * 8);
            double layerHeight = 0.5 + (layer * 0.8);
            double rotation = time + (layer * Math.PI / 3);

            for (int i = 0; i < layerParticles; i++) {
                double angle = rotation + ((2 * Math.PI * i) / layerParticles);
                double x = bannerPos.x + Math.cos(angle) * layerRadius;
                double z = bannerPos.z + Math.sin(angle) * layerRadius;

                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        x, bannerPos.y + layerHeight, z,
                        1, 0, 0, 0, 0
                );
            }
        }

         */

        // === 3. BANNER POLE (Tall pillar of flames) ===
        int poleHeight = 12; // Much taller
        for (int i = 0; i < poleHeight; i++) {
            double y = bannerPos.y + (i * 0.4);

            // Central flame pillar
            level.sendParticles(
                    ParticleTypes.CRIT,
                    bannerPos.x, y, bannerPos.z,
                    5, 0, 0.02, 0, 0
            );

            // Spiral effect around pole
            if (i % 2 == 0) {
                double spiralAngle = (time * 2) + (i * 0.5);
                double spiralRadius = 0.3;
                double sx = bannerPos.x + Math.cos(spiralAngle) * spiralRadius;
                double sz = bannerPos.z + Math.sin(spiralAngle) * spiralRadius;

                level.sendParticles(
                        ParticleTypes.FIREWORK,
                        sx, y, sz,
                        1, 0, 0, 0, 0
                );
            }
        }

        // === 4. BANNER FLAG (Top portion) ===
        double flagHeight = bannerPos.y + (poleHeight * 0.4);

        // Flag particles (waving effect)
        for (int i = 0; i < 6; i++) {
            // CHANGED: Increased 'j' from 4 to 8 to make the flag WIDER
            for (int j = 0; j < 6; j++) {
                double wave = Math.sin((time * 3) + (i * 0.3)) * 0.2;

                // The 'j' variable controls the horizontal spread
                double x = bannerPos.x + 0.5 + wave + (j * 0.15);
                double y = flagHeight - (i * 0.2);

                level.sendParticles(
                        ParticleTypes.END_ROD,
                        x, y, bannerPos.z,
                        1, 0, 0, 0, 0
                );
            }
        }

        // === 5. FLOATING EMBERS (Ambient particles) ===
        if (gameTime % 2 == 0) {
            for (int i = 0; i < 5; i++) {
                double angle = Math.random() * 2 * Math.PI;
                double dist = Math.random() * BANNER_RADIUS * 0.8;
                double x = bannerPos.x + Math.cos(angle) * dist;
                double z = bannerPos.z + Math.sin(angle) * dist;
                double y = bannerPos.y + (Math.random() * 3);

                level.sendParticles(
                        ParticleTypes.LAVA,
                        x, y, z,
                        0, 0, 0.05, 0, 0.02
                );
            }
        }

        /*
        // === 6. ENERGY PULSES (Periodic bursts) ===
        if (gameTime % 40 == 0) { // Every 2 seconds
            // Expanding energy wave
            for (int ring = 0; ring < 3; ring++) {
                double pulseRadius = 4.0 + (ring * 2.0);
                int pulseParticles = 40;

                for (int i = 0; i < pulseParticles; i++) {
                    double angle = (2 * Math.PI * i) / pulseParticles;
                    double x = bannerPos.x + Math.cos(angle) * pulseRadius;
                    double z = bannerPos.z + Math.sin(angle) * pulseRadius;

                    level.sendParticles(
                            ParticleTypes.TOTEM_OF_UNDYING,
                            x, bannerPos.y + 0.5, z,
                            1, 0, 0.1, 0, 0.1
                    );
                }
            }
        }

         */

        // === 7. TOP CROWN EFFECT ===
        double crownHeight = bannerPos.y + (poleHeight * 0.4) + 0.5;
        int crownPoints = 8;

        for (int i = 0; i < crownPoints; i++) {
            double angle = (2 * Math.PI * i) / crownPoints + (time * 0.5);
            double radius = 0.5;
            double x = bannerPos.x + Math.cos(angle) * radius;
            double z = bannerPos.z + Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    x, crownHeight, z,
                    1, 0, 0.15, 0, 0
            );
        }

        // === 8. AMBIENT SOUND (Periodic) ===
        if (gameTime % 100 == 0) {
            level.playSound(null, bannerPos.x, bannerPos.y, bannerPos.z,
                    SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.3f, 1.5f);
        }
    }
}