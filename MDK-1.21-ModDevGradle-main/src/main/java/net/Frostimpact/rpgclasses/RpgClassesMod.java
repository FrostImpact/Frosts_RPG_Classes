package net.Frostimpact.rpgclasses;

import net.Frostimpact.rpgclasses.client.ClientEvents;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent; // <--- CRITICAL IMPORT

@Mod(RpgClassesMod.MOD_ID)
public class RpgClassesMod {
    public static final String MOD_ID = "rpgclasses";

    public RpgClassesMod(IEventBus modEventBus) {

        ModAttachments.register(modEventBus);
        ModMessages.register(modEventBus);

        // 1. Listen for KeyMapping registration on the MOD BUS
        modEventBus.addListener(this::registerKeyMappings);

        // 2. Register the dynamic input events (like key presses) on the NEOFORGE BUS
        // This is where your ClientEvents instance should go.
        NeoForge.EVENT_BUS.register(new ClientEvents());

        modEventBus.addListener(this::registerKeyMappings);

        // CRITICAL FIX: Register an INSTANCE of the event handlers to the NeoForge Bus
        // This is the bus that listens for PlayerLoggedInEvent and PlayerTickEvent
        NeoForge.EVENT_BUS.register(new ClientEvents());
        NeoForge.EVENT_BUS.register(new ServerEvents()); // <--- ADD THIS LINE!
    }

    // New method in RpgClassesMod to handle the KeyMappings event
    private void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        // You can't use an instance of KeyBinding here, you register it directly.
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.DASH_KEY);
    }
}