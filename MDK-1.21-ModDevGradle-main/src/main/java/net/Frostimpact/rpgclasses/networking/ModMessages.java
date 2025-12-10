package net.Frostimpact.rpgclasses.networking;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.networking.packet.PacketDash;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
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

        registrar.playBidirectional(
                PacketDash.TYPE,
                PacketDash.STREAM_CODEC,
                PacketDash::handle
        );
    }

    // Helper to send to server
    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }
}