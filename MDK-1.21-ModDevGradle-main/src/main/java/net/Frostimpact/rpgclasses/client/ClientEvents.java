package net.Frostimpact.rpgclasses.client;

// ... imports ...

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public class ClientEvents {

    // DELETE THIS ENTIRE METHOD (It's now in RpgClassesMod):
    /*
    @SubscribeEvent
    public void onKeyMappingRegister(RegisterKeyMappingsEvent event) {
        event.register(KeyBinding.DASH_KEY);
    }
    */

    // Keep this method, as it runs during the game on the NeoForge bus.
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (net.Frostimpact.rpgclasses.util.KeyBinding.DASH_KEY.consumeClick()) {
            net.Frostimpact.rpgclasses.networking.ModMessages.sendToServer(
                    new net.Frostimpact.rpgclasses.networking.packet.PacketDash()
            );
        }
    }
}