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
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID)
public class AlchemistEventHandler {

    private static LivingEntity previousGlowingTarget = null;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpgData = player.getData(ModAttachments.PLAYER_RPG);
            
            if (!rpgData.getCurrentClass().equals("ALCHEMIST")) return;

            // Handle reagent cycling when in injection mode and shifting
            if (rpgData.isAlchemistInjectionActive() && player.isShiftKeyDown()) {
                // Cycle reagent every 10 ticks when holding shift
                if (player.tickCount % 10 == 0) {
                    cycleReagent(player, rpgData);
                    // Sync to client after cycling
                    ModMessages.sendToPlayer(new PacketSyncAlchemistState(
                            rpgData.isAlchemistConcoction(),
                            rpgData.isAlchemistInjectionActive(),
                            rpgData.getAlchemistClickPattern(),
                            rpgData.isAlchemistBuffMode(),
                            rpgData.getAlchemistSelectedReagent()
                    ), player);
                }
            }

            // Handle POTION AFFINITY passive - make nearest enemy glow
            if (player.tickCount % 20 == 0) { // Check every second
                applyPotionAffinity(player);
            }
        }
    }

    // This method will be called via a packet when the player clicks during CONCOCTION mode
    public static void handleClick(ServerPlayer player, String clickType) {
        PlayerRPGData rpgData = player.getData(ModAttachments.PLAYER_RPG);
        
        if (!rpgData.getCurrentClass().equals("ALCHEMIST")) return;
        if (!rpgData.isAlchemistConcoction()) return;

        String currentPattern = rpgData.getAlchemistClickPattern();
        boolean isBuffMode = rpgData.isAlchemistBuffMode();

        // Determine max clicks based on mode
        int maxClicks = isBuffMode ? 2 : 3;

        if (currentPattern.length() < maxClicks) {
            // Add the click to the pattern
            String newPattern = currentPattern + clickType;
            rpgData.setAlchemistClickPattern(newPattern);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§d⚗ Pattern: §e" + newPattern));

            // Sync to client after click
            ModMessages.sendToPlayer(new PacketSyncAlchemistState(
                    rpgData.isAlchemistConcoction(),
                    rpgData.isAlchemistInjectionActive(),
                    newPattern,
                    rpgData.isAlchemistBuffMode(),
                    rpgData.getAlchemistSelectedReagent()
            ), player);

            // Check if pattern is complete
            if (newPattern.length() == maxClicks) {
                // Throw the potion
                throwPotion(player, rpgData, newPattern);
                
                // Reset concoction state
                rpgData.setAlchemistConcoction(false);
                rpgData.setAlchemistClickPattern("");
                rpgData.setAlchemistConcoctionTicks(0);

                // Sync to client after completing pattern
                ModMessages.sendToPlayer(new PacketSyncAlchemistState(
                        false,
                        rpgData.isAlchemistInjectionActive(),
                        "",
                        rpgData.isAlchemistBuffMode(),
                        rpgData.getAlchemistSelectedReagent()
                ), player);
            }
        }
    }

    private static void throwPotion(ServerPlayer player, PlayerRPGData rpgData, String pattern) {
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
        
        // Check if volatile mix is active
        if (rpgData.isAlchemistVolatileMixActive()) {
            potion.setLingering(true);
            rpgData.setAlchemistVolatileMixActive(false);
            
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§5⚗ Volatile FLASK thrown!"));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§d⚗ FLASK thrown!"));
        }
        
        // Set velocity with arc
        float speed = 1.2f;
        Vec3 velocity = lookVec.scale(speed);
        velocity = velocity.add(0, 0.2, 0); // Add upward arc
        potion.setDeltaMovement(velocity);
        potion.hurtMarked = true;

        player.level().addFreshEntity(potion);

        // Sound effect
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
        // Find nearest enemy
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

        // Remove glow from previous target if it's different
        if (previousGlowingTarget != null && previousGlowingTarget != nearest) {
            previousGlowingTarget.setGlowingTag(false);
        }

        if (nearest != null) {
            // Apply glowing effect with duration (2 seconds / 40 ticks)
            nearest.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, false, false));
            previousGlowingTarget = nearest;

            // Collect and send debuffs to client
            List<String> debuffs = new ArrayList<>();
            for (MobEffectInstance effect : nearest.getActiveEffects()) {
                // Only include negative effects (debuffs)
                if (!effect.getEffect().value().isBeneficial()) {
                    String effectName = effect.getEffect().value().getDisplayName().getString();
                    int amplifier = effect.getAmplifier();
                    String level = amplifier > 0 ? " " + (amplifier + 1) : "";
                    debuffs.add(effectName + level);
                }
            }
            ModMessages.sendToPlayer(new PacketSyncEnemyDebuffs(debuffs), player);
        } else {
            previousGlowingTarget = null;
            // Send empty list when no enemy nearby
            ModMessages.sendToPlayer(new PacketSyncEnemyDebuffs(new ArrayList<>()), player);
        }
    }
}
