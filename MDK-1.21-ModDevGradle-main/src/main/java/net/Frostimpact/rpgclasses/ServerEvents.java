package net.Frostimpact.rpgclasses;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
// DELETE: import net.neoforged.fml.common.EventBusSubscriber; // DELETE THIS LINE
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.Frostimpact.rpgclasses.networking.ModMessages; // <--- NEW IMPORT
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncMana; // <--- NEW IMPORT
import net.minecraft.server.level.ServerPlayer; // <--- NEW IMPORT

// DELETE THE ENTIRE @EventBusSubscriber ANNOTATION
public class ServerEvents {

    // --- TEMPORARY FIX: SET CLASS ON LOGIN ---
    // IMPORTANT: Remove 'static' and ensure correct imports
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
    // ------------------------------------------

    // Note: You must also remove 'static' from your other methods (e.g., onPlayerTick)
    // if you are deleting the @EventBusSubscriber annotation!

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        // Must also be non-static now
        // ... (existing tick logic here) ...
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) { // Make sure it's ServerPlayer for sending packets

            if (!player.level().isClientSide) {

                PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

                // ... (Your existing Cooldown/Mana Regen logic here) ...

                // --- SEND ACTIONBAR STATUS EVERY 5 TICKS (4 times per second) ---
                if (player.level().getGameTime() % 5 == 0) {
                    int currentMana = rpg.getMana();
                    int maxMana = rpg.getMaxMana(); // Assuming you have this getter

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

    // ... (Your other methods must also be non-static) ...
}