package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketOpenClassGui implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketOpenClassGui> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "open_class_gui"));

    public static final StreamCodec<FriendlyByteBuf, PacketOpenClassGui> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> {},
            (buf) -> new PacketOpenClassGui()
    );

    public PacketOpenClassGui() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketOpenClassGui message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft.getInstance().setScreen(
                    new net.Frostimpact.rpgclasses.client.gui.ClassSelectionScreen()
            );
        });
    }
}