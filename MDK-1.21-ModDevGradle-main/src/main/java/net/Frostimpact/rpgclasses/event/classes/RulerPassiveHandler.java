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
            // If RALLY is not active, banner follows player
            if (!rpg.isRulerRallyActive()) {
                rpg.setRulerBannerPosition(player.position());
            } else {
                // Tick down rally duration
                int rallyTicks = rpg.getRulerRallyTicks();
                if (rallyTicks > 0) {
                    rpg.setRulerRallyTicks(rallyTicks - 1);
                } else {
                    // Rally ended - move banner back to player
                    rpg.setRulerRallyActive(false);
                    rpg.setRulerBannerPosition(player.position());
                    
                    player.sendSystemMessage(Component.literal("§6⚔ RALLY ended! Banner returns."));
                    
                    level.playSound(null, player.blockPosition(),
                            SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 1.2f);
                }
            }

            Vec3 bannerPos = rpg.getRulerBannerPosition();

            // === RENDER BANNER VISUAL ===
            if (level.getGameTime() % 10 == 0) {
                renderBannerCircle(level, bannerPos);
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
        // Check if summon has GLORY buff
        boolean hasGlory = summon.hasEffect(MobEffects.MOVEMENT_SPEED) && 
                          summon.hasEffect(MobEffects.ABSORPTION);

        if (hasGlory) {
            // GLORY prevents DEMORALIZE
            return;
        }

        if (!inRange) {
            // OUT OF RANGE - Apply DEMORALIZE
            if (!summon.getPersistentData().getBoolean("demoralized")) {
                summon.getPersistentData().putBoolean("demoralized", true);
                summon.getPersistentData().putInt("demoralize_ticks", DEMORALIZE_DEATH_TICKS);

                // Visual feedback
                level.sendParticles(
                        ParticleTypes.ASH,
                        summon.getX(), summon.getY() + summon.getBbHeight() / 2, summon.getZ(),
                        10, 0.3, 0.3, 0.3, 0.05
                );
            }

            // Tick down demoralize timer
            int ticks = summon.getPersistentData().getInt("demoralize_ticks");
            if (ticks > 0) {
                summon.getPersistentData().putInt("demoralize_ticks", ticks - 1);

                // Periodic visual warning
                if (ticks % 40 == 0) {
                    level.sendParticles(
                            ParticleTypes.SMOKE,
                            summon.getX(), summon.getY() + summon.getBbHeight(), summon.getZ(),
                            5, 0.2, 0.2, 0.2, 0.02
                    );
                }
            } else {
                // Time's up - kill summon
                level.sendParticles(
                        ParticleTypes.POOF,
                        summon.getX(), summon.getY() + summon.getBbHeight() / 2, summon.getZ(),
                        20, 0.3, 0.3, 0.3, 0.1
                );
                summon.discard();
            }
        } else {
            // IN RANGE - Remove DEMORALIZE
            if (summon.getPersistentData().getBoolean("demoralized")) {
                summon.getPersistentData().putBoolean("demoralized", false);
                summon.getPersistentData().putInt("demoralize_ticks", 0);

                // Visual feedback
                level.sendParticles(
                        ParticleTypes.GLOW,
                        summon.getX(), summon.getY() + summon.getBbHeight() / 2, summon.getZ(),
                        10, 0.3, 0.3, 0.3, 0.05
                );
            }
        }
    }

    private static void renderBannerCircle(ServerLevel level, Vec3 bannerPos) {
        // Golden circle at ground level
        int particles = 60;
        for (int i = 0; i < particles; i++) {
            double angle = (2 * Math.PI * i) / particles;
            double x = bannerPos.x + Math.cos(angle) * BANNER_RADIUS;
            double z = bannerPos.z + Math.sin(angle) * BANNER_RADIUS;

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    x, bannerPos.y + 0.1, z,
                    1, 0, 0, 0, 0
            );
        }

        // Banner pole particles
        for (int i = 0; i < 8; i++) {
            level.sendParticles(
                    ParticleTypes.FLAME,
                    bannerPos.x, bannerPos.y + (i * 0.3), bannerPos.z,
                    1, 0.05, 0, 0.05, 0
            );
        }
    }
}