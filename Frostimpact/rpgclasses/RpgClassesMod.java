package net.Frostimpact.rpgclasses;

import net.Frostimpact.rpgclasses.client.ClientEvents;
import net.Frostimpact.rpgclasses.event.ServerEvents;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(RpgClassesMod.MOD_ID)
public class RpgClassesMod {
    public static final String MOD_ID = "rpgclasses";

    public RpgClassesMod(IEventBus modEventBus) {

        ModAttachments.register(modEventBus);
        ModMessages.register(modEventBus);

        // 1. Listen for KeyMapping registration on the MOD BUS (ONLY ONCE!)
        modEventBus.addListener(this::registerKeyMappings);

        // 2. Register event handlers on the NEOFORGE BUS (ONLY ONCE EACH!)
        NeoForge.EVENT_BUS.register(new ClientEvents());
        NeoForge.EVENT_BUS.register(new ServerEvents());
    }

    // Method to handle the KeyMappings event
    private void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.DASH_KEY);
    }
}