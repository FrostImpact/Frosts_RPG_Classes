package net.Frostimpact.rpgclasses.event.classes;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.entity.projectile.AlchemistPotionEntity;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncAlchemistState;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncEnemyDebuffs;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID)
public class AlchemistEventHandler {

    private static final int GLOWING_DURATION_TICKS = 40;
    private static final java.util.concurrent.ConcurrentHashMap<java.util.UUID, LivingEntity> previousGlowingTargets = new java.util.concurrent.ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpgData = player.getData(ModAttachments.PLAYER_RPG);

            if (!rpgData.getCurrentClass().equals("ALCHEMIST")) {
                previousGlowingTargets.remove(player.getUUID());
                return;
            }

            // Handle reagent cycling when in injection mode and shifting
            if (rpgData.isAlchemistInjectionActive() && player.isShiftKeyDown()) {
                if (player.tickCount % 10 == 0) {
                    cycleReagent(player, rpgData);
                    // UPDATED: Include ticks in sync
                    ModMessages.sendToPlayer(new PacketSyncAlchemistState(
                            rpgData.isAlchemistConcoction(),
                            rpgData.getAlchemistConcoctionTicks(),
                            rpgData.isAlchemistInjectionActive(),
                            rpgData.getAlchemistClickPattern(),
                            rpgData.isAlchemistBuffMode(),
                            rpgData.getAlchemistSelectedReagent()
                    ), player);
                }
            }

            // Handle POTION AFFINITY passive
            if (player.tickCount % 20 == 0) {
                applyPotionAffinity(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            previousGlowingTargets.remove(player.getUUID());
        }
    }

    public static void handleClick(ServerPlayer player, String clickType) {
        System.out.println("[SERVER] Handling click: " + clickType + " for player: " + player.getName().getString());

        PlayerRPGData rpgData = player.getData(ModAttachments.PLAYER_RPG);

        if (!rpgData.getCurrentClass().equals("ALCHEMIST")) {
            System.out.println("[SERVER] Player is not ALCHEMIST, ignoring click");
            return;
        }

        if (!rpgData.isAlchemistConcoction()) {
            System.out.println("[SERVER] CONCOCTION is not active, ignoring click");
            return;
        }

        String currentPattern = rpgData.getAlchemistClickPattern();
        if (currentPattern == null) currentPattern = "";

        boolean isBuffMode = rpgData.isAlchemistBuffMode();
        int maxClicks = isBuffMode ? 2 : 3;

        System.out.println("[SERVER] Current pattern: '" + currentPattern + "', Max clicks: " + maxClicks +
                ", Buff mode: " + isBuffMode);

        if (currentPattern.length() < maxClicks) {
            String newPattern = currentPattern + clickType;
            rpgData.setAlchemistClickPattern(newPattern);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§d⚗ Pattern: §e" + newPattern));

            System.out.println("[SERVER] New pattern: '" + newPattern + "'");

            // UPDATED: Include ticks in sync
            ModMessages.sendToPlayer(new PacketSyncAlchemistState(
                    rpgData.isAlchemistConcoction(),
                    rpgData.getAlchemistConcoctionTicks(),
                    rpgData.isAlchemistInjectionActive(),
                    rpgData.getAlchemistClickPattern(),
                    rpgData.isAlchemistBuffMode(),
                    rpgData.getAlchemistSelectedReagent()
            ), player);

            if (newPattern.length() == maxClicks) {
                System.out.println("[SERVER] Pattern complete! Throwing potion...");

                throwPotion(player, rpgData, newPattern);

                rpgData.setAlchemistConcoction(false);
                rpgData.setAlchemistClickPattern("");
                rpgData.setAlchemistConcoctionTicks(0);

                // UPDATED: Include ticks in sync
                ModMessages.sendToPlayer(new PacketSyncAlchemistState(
                        false,
                        rpgData.getAlchemistConcoctionTicks(),
                        rpgData.isAlchemistInjectionActive(),
                        rpgData.getAlchemistClickPattern(),
                        rpgData.isAlchemistBuffMode(),
                        rpgData.getAlchemistSelectedReagent()
                ), player);
            }
        } else {
            System.out.println("[SERVER] Pattern already at max length, ignoring click");
        }
    }

    private static void throwPotion(ServerPlayer player, PlayerRPGData rpgData, String pattern) {
        System.out.println("[SERVER] Throwing potion with pattern: " + pattern);

        Vec3 lookVec = player.getLookAngle();
        Vec3 spawnPos = player.position()
                .add(0, player.getEyeHeight() - 0.1, 0)
                .add(lookVec.scale(0.5));

        AlchemistPotionEntity potion = new AlchemistPotionEntity(
                ModEntities.ALCHEMIST_POTION.get(),
                player.level(),
                player
        );

        potion.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        potion.setEffectType(pattern);

        if (rpgData.isAlchemistVolatileMixActive()) {
            potion.setLingering(true);
            rpgData.setAlchemistVolatileMixActive(false);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§5⚗ Volatile FLASK thrown!"));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§d⚗ FLASK thrown!"));
        }

        float speed = 1.2f;
        Vec3 velocity = lookVec.scale(speed);
        velocity = velocity.add(0, 0.2, 0);
        potion.setDeltaMovement(velocity);
        potion.hurtMarked = true;

        player.level().addFreshEntity(potion);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void cycleReagent(ServerPlayer player, PlayerRPGData rpgData) {
        String current = rpgData.getAlchemistSelectedReagent();
        String next;

        switch (current) {
            case "CRYOSTAT":
                next = "CATALYST";
                break;
            case "CATALYST":
                next = "FRACTURE";
                break;
            case "FRACTURE":
                next = "SANCTIFIED";
                break;
            case "SANCTIFIED":
                next = "CRYOSTAT";
                break;
            default:
                next = "CRYOSTAT";
                break;
        }

        rpgData.setAlchemistSelectedReagent(next);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7Reagent: §e" + next));
    }

    private static void applyPotionAffinity(ServerPlayer player) {
        AABB searchBox = player.getBoundingBox().inflate(15.0);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && !entity.isAlliedTo(player)
        );

        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            double distance = player.distanceToSqr(entity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entity;
            }
        }

        LivingEntity previousTarget = previousGlowingTargets.get(player.getUUID());

        if (previousTarget != null && previousTarget != nearest && !previousTarget.isRemoved()) {
            if (previousTarget.hasEffect(MobEffects.GLOWING)) {
                previousTarget.removeEffect(MobEffects.GLOWING);
            }
        }

        if (nearest != null) {
            nearest.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOWING_DURATION_TICKS, 0, false, false));
            previousGlowingTargets.put(player.getUUID(), nearest);

            List<String> debuffs = new ArrayList<>();
            for (MobEffectInstance effect : nearest.getActiveEffects()) {
                if (!effect.getEffect().value().isBeneficial()) {
                    String effectName = effect.getEffect().value().getDisplayName().getString();
                    int amplifier = effect.getAmplifier();
                    String level = amplifier > 0 ? " " + (amplifier + 1) : "";
                    debuffs.add(effectName + level);
                }
            }
            ModMessages.sendToPlayer(new PacketSyncEnemyDebuffs(debuffs), player);
        } else {
            previousGlowingTargets.remove(player.getUUID());
            ModMessages.sendToPlayer(new PacketSyncEnemyDebuffs(new ArrayList<>()), player);
        }
    }
}