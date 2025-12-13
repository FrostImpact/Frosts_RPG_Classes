package net.Frostimpact.rpgclasses.registry;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.item.ShortbowItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, RpgClassesMod.MOD_ID);

    public static final DeferredHolder<Item, Item> SHORTBOW = ITEMS.register("shortbow",
            () -> new ShortbowItem(new Item.Properties().durability(384))); // Same durability as normal bow

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}