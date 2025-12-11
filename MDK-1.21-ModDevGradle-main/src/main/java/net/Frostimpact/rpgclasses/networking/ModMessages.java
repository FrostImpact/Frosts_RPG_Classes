package net.Frostimpact.rpgclasses.networking;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.networking.packet.PacketUseAbility;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncMana;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncCooldowns;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModMessages {

    // Called from RpgClassesMod constructor
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModMessages::registerPayloads);
    }

    // The actual registration event
    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(RpgClassesMod.MOD_ID)
                .versioned("1.0");

        // 1. Register PacketUseAbility (Client -> Server)
        registrar.playBidirectional(
                PacketUseAbility.TYPE,
                PacketUseAbility.STREAM_CODEC,
                PacketUseAbility::handle
        );

        // 2. Register PacketSyncMana (Server -> Client)
        registrar.playToClient(
                PacketSyncMana.TYPE,
                PacketSyncMana.STREAM_CODEC,
                PacketSyncMana::handle
        );

        // 3. Register PacketSyncCooldowns (Server -> Client)
        registrar.playToClient(
                PacketSyncCooldowns.TYPE,
                PacketSyncCooldowns.STREAM_CODEC,
                PacketSyncCooldowns::handle
        );
    }

    // Helper to send to server (Used for PacketUseAbility)
    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    // Helper to send to a specific player (Used for PacketSyncMana)
    public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
}