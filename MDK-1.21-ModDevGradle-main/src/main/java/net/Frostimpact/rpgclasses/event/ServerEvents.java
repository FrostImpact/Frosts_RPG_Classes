package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncMana;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
            rpg.setCurrentClass("BLADEDANCER");

            // 2. Send a confirmation message
            event.getEntity().sendSystemMessage(Component.literal("§a[TEMP FIX] Your class is set to: §6BLADEDANCER"));
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

                // Mana Regeneration (1 mana per second = every 20 ticks)
                if (player.level().getGameTime() % 10 == 0) {
                    if (rpg.getMana() < rpg.getMaxMana()) {
                        rpg.useMana(-1); // Negative to add mana
                    }
                }

                // --- SEND ACTIONBAR STATUS EVERY 5 TICKS (4 times per second) ---
                if (player.level().getGameTime() % 5 == 0) {
                    int currentMana = rpg.getMana();
                    int maxMana = rpg.getMaxMana();

                    // Build the custom status string
                    String status = String.format("§bMANA: §f%d / %d §7| §cHP: §f%d / %d §7| §6CLASS: §f%s",
                            currentMana, maxMana,
                            (int)player.getHealth(), (int)player.getMaxHealth(),
                            rpg.getCurrentClass());

                    // Send the packet to this specific player
                    ModMessages.sendToPlayer(new PacketSyncMana(status), player);
                }
            }
        }
    }
}