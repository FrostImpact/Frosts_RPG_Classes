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

public class PacketSyncClass implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncClass> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "sync_class"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncClass> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> buf.writeUtf(message.className),
            (buf) -> new PacketSyncClass(buf.readUtf())
    );

    private final String className;

    public PacketSyncClass(String className) {
        this.className = className;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncClass message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                PlayerRPGData rpg = Minecraft.getInstance().player.getData(ModAttachments.PLAYER_RPG);
                rpg.setCurrentClass(message.className);

                System.out.println("[CLIENT] Class synced: " + message.className);

                // Send feedback message
                Minecraft.getInstance().player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§a[RPG Classes] Class set to: §6" + message.className),
                        false
                );
            }
        });
    }
}
