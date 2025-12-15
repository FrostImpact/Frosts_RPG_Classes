package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PacketSyncEnemyDebuffs(List<String> debuffs) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncEnemyDebuffs> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "sync_enemy_debuffs"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncEnemyDebuffs> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> {
                buf.writeInt(message.debuffs.size());
                for (String debuff : message.debuffs) {
                    buf.writeUtf(debuff);
                }
            },
            (buf) -> {
                int size = buf.readInt();
                List<String> debuffs = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    debuffs.add(buf.readUtf());
                }
                return new PacketSyncEnemyDebuffs(debuffs);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncEnemyDebuffs message, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This runs on the Client - store in client-side data
            if (context.player() != null) {
                PlayerRPGData rpgData = context.player().getData(ModAttachments.PLAYER_RPG);
                if (rpgData != null) {
                    // Validate list size to prevent memory issues from malicious packets
                    if (message.debuffs.size() <= 20) { // Max 20 debuffs is reasonable
                        rpgData.setAlchemistEnemyDebuffs(message.debuffs);
                    }
                }
            }
        });
    }
}
