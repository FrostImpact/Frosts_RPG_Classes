package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncAlchemistState(
        boolean concoctionActive,
        boolean injectionActive,
        String clickPattern,
        boolean buffMode,
        String selectedReagent
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncAlchemistState> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "sync_alchemist_state"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncAlchemistState> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> {
                buf.writeBoolean(message.concoctionActive);
                buf.writeBoolean(message.injectionActive);
                buf.writeUtf(message.clickPattern);
                buf.writeBoolean(message.buffMode);
                buf.writeUtf(message.selectedReagent);
            },
            (buf) -> new PacketSyncAlchemistState(
                    buf.readBoolean(),
                    buf.readBoolean(),
                    buf.readUtf(),
                    buf.readBoolean(),
                    buf.readUtf()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncAlchemistState message, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This runs on the Client
            if (context.player() != null) {
                PlayerRPGData rpgData = context.player().getData(ModAttachments.PLAYER_RPG);
                if (rpgData != null) {
                    rpgData.setAlchemistConcoction(message.concoctionActive);
                    rpgData.setAlchemistInjectionActive(message.injectionActive);
                    rpgData.setAlchemistClickPattern(message.clickPattern);
                    rpgData.setAlchemistBuffMode(message.buffMode);
                    rpgData.setAlchemistSelectedReagent(message.selectedReagent);
                }
            }
        });
    }
}
