package net.Frostimpact.rpgclasses.registry;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.event.item.ClassBookItem;
import net.Frostimpact.rpgclasses.item.ShortbowItem;
import net.Frostimpact.rpgclasses.item.StaffItem;
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

    public static final DeferredHolder<Item, Item> STAFF = ITEMS.register("staff",
            () -> new StaffItem(new Item.Properties().durability(250)));

    public static final DeferredHolder<Item, Item> CLASS_BOOK = ITEMS.register("class_book",
            () -> new ClassBookItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}