package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.item.StaffItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketFireStaff implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketFireStaff> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "fire_staff"));

    public static final StreamCodec<FriendlyByteBuf, PacketFireStaff> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> {},
            (buf) -> new PacketFireStaff()
    );

    public PacketFireStaff() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketFireStaff message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack mainHand = player.getMainHandItem();
                
                if (mainHand.getItem() instanceof StaffItem) {
                    net.Frostimpact.rpgclasses.event.item.StaffHandler.fireStaffProjectile(player, mainHand);
                }
            }
        });
    }
}