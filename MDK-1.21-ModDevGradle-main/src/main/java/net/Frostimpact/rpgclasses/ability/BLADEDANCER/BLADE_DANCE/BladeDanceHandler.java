package net.Frostimpact.rpgclasses.ability.BLADEDANCER.BLADE_DANCE;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class BladeDanceHandler {

    // 1. Handle Duration & Animation
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpgData = player.getData(ModAttachments.PLAYER_RPG);

            if (rpgData.isBladeDanceActive()) {
                int ticks = rpgData.getBladeDanceTicks();

                if (ticks > 0) {
                    // Decrease timer
                    rpgData.setBladeDanceTicks(ticks - 1);

                    // Optional: Add Animation Logic here to spin the swords around the player
                    // animateSwords(player, rpgData);
                } else {
                    // Time is up! Cleanup.
                    cleanupBlades(player, rpgData);
                }
            }
        }
    }

    // 2. Handle Disconnect (Player Leaving)
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpgData = player.getData(ModAttachments.PLAYER_RPG);

            // Force cleanup if they leave while ability is active
            if (rpgData.isBladeDanceActive()) {
                cleanupBlades(player, rpgData);
            }
        }
    }

    // Shared Cleanup Method
    private static void cleanupBlades(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();
        List<Integer> swordIds = rpgData.getBladeDanceSwordIds();

        // 1. Kill entities by ID (Fastest method)
        if (swordIds != null && !swordIds.isEmpty()) {
            for (int id : swordIds) {
                Entity entity = level.getEntity(id);
                if (entity != null && entity.getTags().contains("rpg_blade_dance")) {
                    entity.discard(); // Removes entity from world
                }
            }
        }

        // 2. Fail-safe: Scan for any stragglers with the player's UUID tag
        // (Useful if the ID list got desynced or cleared incorrectly, preventing floating swords on logout)
        String ownerTag = "owner:" + player.getStringUUID();

        // Use getEntities to find specific ArmorStands with our tags
        List<Entity> stragglers = level.getEntities(
                (Entity) null, // No specific source entity to start search from
                player.getBoundingBox().inflate(100), // Search within 100 blocks of player
                e -> e instanceof ArmorStand &&
                        e.getTags().contains("rpg_blade_dance") &&
                        e.getTags().contains(ownerTag)
        );

        for (Entity entity : stragglers) {
            if (entity.isAlive()) { // Check if not already removed by ID check above
                entity.discard();
            }
        }

        // Reset Data State
        rpgData.clearBladeDanceSwords();
        rpgData.setBladeDanceActive(false);
        rpgData.setBladeDanceTicks(0);
    }
}