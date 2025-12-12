package net.Frostimpact.rpgclasses.networking;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.networking.packet.PacketUseAbility;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncMana;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncCooldowns;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncClass;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModMessages {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModMessages::registerPayloads);
    }

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(RpgClassesMod.MOD_ID)
                .versioned("1.0");

        // 1. PacketUseAbility (Client -> Server)
        registrar.playBidirectional(
                PacketUseAbility.TYPE,
                PacketUseAbility.STREAM_CODEC,
                PacketUseAbility::handle
        );

        // 2. PacketSyncMana (Server -> Client)
        registrar.playToClient(
                PacketSyncMana.TYPE,
                PacketSyncMana.STREAM_CODEC,
                PacketSyncMana::handle
        );

        // 3. PacketSyncCooldowns (Server -> Client)
        registrar.playToClient(
                PacketSyncCooldowns.TYPE,
                PacketSyncCooldowns.STREAM_CODEC,
                PacketSyncCooldowns::handle
        );

        // 4. PacketSyncClass (Server -> Client) - NEW
        registrar.playToClient(
                PacketSyncClass.TYPE,
                PacketSyncClass.STREAM_CODEC,
                PacketSyncClass::handle
        );

        System.out.println("RPG Classes: All packets registered!");
    }

    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendToPlayer(CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }
}