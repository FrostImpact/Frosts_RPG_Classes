package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * This class handles events that occur on both client and server.
 * It registers itself automatically via @EventBusSubscriber.
 */
@EventBusSubscriber(modid = RpgClassesMod.MOD_ID)
public class ModEvents {

    /**
     * This event runs when a player respawns (Clone).
     * It preserves RPG data from the old player to the new one.
     */
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        // Get the old data (from the dead body)
        PlayerRPGData oldData = event.getOriginal().getData(ModAttachments.PLAYER_RPG);
        // Get the new data (for the new body)
        PlayerRPGData newData = event.getEntity().getData(ModAttachments.PLAYER_RPG);

        // Copy the class over (always preserve)
        newData.setCurrentClass(oldData.getCurrentClass());

        // Option 1: Keep mana on respawn (uncomment if desired)
        // newData.setMana(oldData.getMana());

        // Option 2: Reset to max mana on respawn (uncomment if desired)
        // newData.setMana(newData.getMaxMana());

        // Option 3: Keep current default behavior (mana resets to constructor default: 100)
    }
}