package net.Frostimpact.rpgclasses.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public class ClientEvents {

    // This method listens for key presses during the game (on NeoForge bus)
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (net.Frostimpact.rpgclasses.util.KeyBinding.DASH_KEY.consumeClick()) {
            net.Frostimpact.rpgclasses.networking.ModMessages.sendToServer(
                    new net.Frostimpact.rpgclasses.networking.packet.PacketDash()
            );
        }
    }
}