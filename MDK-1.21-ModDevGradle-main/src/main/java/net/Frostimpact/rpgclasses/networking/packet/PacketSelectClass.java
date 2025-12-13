package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSelectClass implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSelectClass> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "select_class"));

    public static final StreamCodec<FriendlyByteBuf, PacketSelectClass> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> buf.writeUtf(message.className),
            (buf) -> new PacketSelectClass(buf.readUtf())
    );

    private final String className;

    public PacketSelectClass(String className) {
        this.className = className;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSelectClass message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);
                rpg.setCurrentClass(message.className);
                
                // Reset state
                rpg.clearAllCooldowns();
                rpg.setMana(rpg.getMaxMana());
                player.removeAllEffects();
                
                // Sync to client
                ModMessages.sendToPlayer(new PacketSyncClass(message.className), player);
                
                player.sendSystemMessage(Component.literal("§a✓ Class set to: §6" + message.className));
            }
        });
    }
}