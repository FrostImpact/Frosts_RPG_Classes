package net.Frostimpact.rpgclasses.client;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.util.KeyBinding;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

/**
 * This class handles CLIENT-SIDE ONLY events on the MOD BUS.
 * It registers itself automatically via @EventBusSubscriber.
 *
 * Key input handling is done in ClientInputHandler (registered to NeoForge Event Bus).
 */
@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    /**
     * Register custom key bindings to the game.
     * This runs once during mod initialization on the MOD BUS.
     */
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(KeyBinding.DASH_KEY);
    }
}