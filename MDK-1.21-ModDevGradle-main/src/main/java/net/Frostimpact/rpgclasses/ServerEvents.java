package net.Frostimpact.rpgclasses;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
// DELETE: import net.neoforged.fml.common.EventBusSubscriber; // DELETE THIS LINE
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

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

    // ... (Your other methods must also be non-static) ...
}