package net.Frostimpact.rpgclasses;

import net.Frostimpact.rpgclasses.ability.AbilityRegistry;
import net.Frostimpact.rpgclasses.client.ClientEvents;
import net.Frostimpact.rpgclasses.event.ServerEvents;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.registry.abilities.AbilityDatabase;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(RpgClassesMod.MOD_ID)
public class RpgClassesMod {

    public static final String MOD_ID = "rpgclasses";

    public RpgClassesMod(IEventBus modEventBus) {
        ModAttachments.register(modEventBus);
        ModMessages.register(modEventBus);
        modEventBus.addListener(this::registerKeyMappings);

        // Register client events only on client side
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(new ClientEvents());
            // EntityHealthBar uses @EventBusSubscriber so it registers automatically
        }

        // Server events work on both sides
        NeoForge.EVENT_BUS.register(new ServerEvents());
    }

    private void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.DASH_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.BLADE_DANCE_KEY);
        System.out.println("RPG Classes: Keybind Registered!");

        AbilityDatabase.registerAll();
        System.out.println("RPG Classes: Database Registered!");

        AbilityRegistry.registerAll();
        System.out.println("RPG Classes: Abilities Registered!");
    }
}