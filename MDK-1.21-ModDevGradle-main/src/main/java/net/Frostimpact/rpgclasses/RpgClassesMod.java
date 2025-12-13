package net.Frostimpact.rpgclasses;

import net.Frostimpact.rpgclasses.ability.AbilityRegistry;
import net.Frostimpact.rpgclasses.client.ClientEvents;
import net.Frostimpact.rpgclasses.event.ServerEvents;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.registry.ModEntities;
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
        // Register attachments (player data)
        ModAttachments.register(modEventBus);

        // Register networking
        ModMessages.register(modEventBus);

        // Register entities
        ModEntities.register(modEventBus);

        // Register items
        net.Frostimpact.rpgclasses.registry.ModItems.register(modEventBus);

        // Register key mappings
        modEventBus.addListener(this::registerKeyMappings);

        // Register client events only on client side
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(new ClientEvents());
        }

        // Server events work on both sides
        NeoForge.EVENT_BUS.register(new ServerEvents());
    }

    private void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        // BLADEDANCER Keys
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.DASH_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.BLADE_DANCE_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.PARRY_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.FINAL_WALTZ_KEY);

        // JUGGERNAUT Keys
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.CRUSH_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.FORTIFY_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.LEAP_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.SWAP_KEY);

        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.SEEKERS_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.VAULT_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.UPDRAFT_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.ARROW_RAIN_KEY);

        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.CLOAK_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.STUN_BOLT_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.CYCLE_QUIVER_KEY);
        event.register(net.Frostimpact.rpgclasses.util.KeyBinding.HIRED_GUN_KEY);

        System.out.println("RPG Classes: Keybinds Registered!");

        // Register ability database
        AbilityDatabase.registerAll();
        System.out.println("RPG Classes: Database Registered!");

        // Register abilities
        AbilityRegistry.registerAll();
        System.out.println("RPG Classes: Abilities Registered!");
    }
}