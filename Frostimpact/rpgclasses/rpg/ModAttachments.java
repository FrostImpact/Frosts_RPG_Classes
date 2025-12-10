package net.Frostimpact.rpgclasses.rpg;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    // Create the Registry for Attachments
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RpgClassesMod.MOD_ID);

    // Register your PlayerRPGData
    // Note: Your PlayerRPGData class MUST implement INBTSerializable<CompoundTag> for this to work!
    public static final Supplier<AttachmentType<PlayerRPGData>> PLAYER_RPG = ATTACHMENT_TYPES.register(
            "player_rpg",
            () -> AttachmentType.serializable(() -> new PlayerRPGData()).build()
    );

    // Call this in your main mod constructor
    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}