package net.Frostimpact.rpgclasses.client;

import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketUseAbility;
import net.Frostimpact.rpgclasses.util.KeyBinding;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public class ClientEvents {

    // This method listens for key presses during the game (on NeoForge bus)
    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (KeyBinding.DASH_KEY.consumeClick()) {
            ModMessages.sendToServer(new PacketUseAbility("dash"));
        }
    }
}