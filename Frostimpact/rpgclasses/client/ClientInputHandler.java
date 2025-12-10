package net.Frostimpact.rpgclasses.client;

import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketDash;
import net.Frostimpact.rpgclasses.util.KeyBinding;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public class ClientInputHandler {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (KeyBinding.DASH_KEY.consumeClick()) {
            ModMessages.sendToServer(new PacketDash());
        }
    }
}