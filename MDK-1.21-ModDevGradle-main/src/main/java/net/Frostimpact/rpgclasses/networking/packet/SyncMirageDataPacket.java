package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncMirageDataPacket(boolean active, int ticks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncMirageDataPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "sync_mirage_data"));

    public static final StreamCodec<FriendlyByteBuf, SyncMirageDataPacket> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> {
                buf.writeBoolean(message.active);
                buf.writeVarInt(message.ticks);
            },
            (buf) -> new SyncMirageDataPacket(
                    buf.readBoolean(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncMirageDataPacket message, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This runs on the Client
            // We use Minecraft.getInstance() or the context player (which is the client player)
            if (context.player() != null) {
                PlayerRPGData rpgData = context.player().getData(ModAttachments.PLAYER_RPG);
                if (rpgData != null) {
                    rpgData.setMirageShadowstepActive(message.active);
                    rpgData.setMirageShadowstepTicks(message.ticks);
                }
            }
        });
    }
}