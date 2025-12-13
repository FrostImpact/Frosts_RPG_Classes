package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.item.ShortbowItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.Frostimpact.rpgclasses.item.ShortbowItem;

public class PacketFireShortbow implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFireShortbow> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "fire_shortbow"));

    public static final StreamCodec<FriendlyByteBuf, PacketFireShortbow> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> {}, // No data to write
            (buf) -> new PacketFireShortbow() // No data to read
    );

    public PacketFireShortbow() {
        // Empty packet - just a signal to fire
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFireShortbow message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack mainHand = player.getMainHandItem();
                
                // Verify player is holding a shortbow
                if (mainHand.getItem() instanceof ShortbowItem) {
                    // Fire the arrow on server side
                    net.Frostimpact.rpgclasses.event.item.ShortbowHandler.fireShortbowArrow(player, mainHand);
                }
            }
        });
    }
}