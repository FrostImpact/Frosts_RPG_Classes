package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSyncMana implements CustomPacketPayload {

    // Unique identifier for this packet
    public static final CustomPacketPayload.Type<PacketSyncMana> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "mana_sync"));

    // Codec to read/write the packet data
    public static final StreamCodec<FriendlyByteBuf, PacketSyncMana> STREAM_CODEC = StreamCodec.of(
            // Encoder: Writes the status string to the buffer
            (buf, message) -> buf.writeUtf(message.statusText),
            // Decoder: Reads the status string and creates a new packet instance
            (buf) -> new PacketSyncMana(buf.readUtf())
    );

    private final String statusText;

    public PacketSyncMana(String statusText) {
        this.statusText = statusText;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // Client-side handling logic
    public static void handle(PacketSyncMana message, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Check if the current client is the player receiving the packet
            if (Minecraft.getInstance().player != null) {
                // Display the text in the Actionbar
                Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal(message.statusText),
                        true // Set 'overlay' to true to make it appear in the actionbar
                );
            }
        });
    }
}