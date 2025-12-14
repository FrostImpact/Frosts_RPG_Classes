package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.event.classes.AlchemistEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketAlchemistClick implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketAlchemistClick> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "alchemist_click"));

    public static final StreamCodec<FriendlyByteBuf, PacketAlchemistClick> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> buf.writeUtf(message.clickType),
            (buf) -> new PacketAlchemistClick(buf.readUtf())
    );

    private final String clickType; // "L" or "R"

    public PacketAlchemistClick(String clickType) {
        this.clickType = clickType;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketAlchemistClick message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                AlchemistEventHandler.handleClick(player, message.clickType);
            }
        });
    }
}
