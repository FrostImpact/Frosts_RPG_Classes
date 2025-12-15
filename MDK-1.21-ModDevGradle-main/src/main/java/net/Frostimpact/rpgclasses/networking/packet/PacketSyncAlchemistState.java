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

public record PacketSyncAlchemistState(
        boolean concoctionActive,
        int concoctionTicks,  // ADDED: Send ticks too!
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
                buf.writeInt(message.concoctionTicks);  // ADDED
                buf.writeBoolean(message.injectionActive);
                buf.writeUtf(message.clickPattern);
                buf.writeBoolean(message.buffMode);
                buf.writeUtf(message.selectedReagent);
            },
            (buf) -> new PacketSyncAlchemistState(
                    buf.readBoolean(),
                    buf.readInt(),  // ADDED
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
            System.out.println("[CLIENT] ========================================");
            System.out.println("[CLIENT] Received AlchemistState packet:");
            System.out.println("  - Concoction: " + message.concoctionActive);
            System.out.println("  - Ticks: " + message.concoctionTicks);
            System.out.println("  - Injection: " + message.injectionActive);
            System.out.println("  - Pattern: '" + message.clickPattern + "'");
            System.out.println("  - Buff Mode: " + message.buffMode);
            System.out.println("  - Reagent: " + message.selectedReagent);

            // CRITICAL FIX: Use Minecraft.getInstance().player instead of context.player()
            // context.player() might return a different instance on the client
            if (Minecraft.getInstance().player != null) {
                System.out.println("[CLIENT] Getting player data...");

                PlayerRPGData rpgData = Minecraft.getInstance().player.getData(ModAttachments.PLAYER_RPG);

                if (rpgData != null) {
                    System.out.println("[CLIENT] BEFORE update:");
                    System.out.println("  - Current Concoction state: " + rpgData.isAlchemistConcoction());
                    System.out.println("  - Current Ticks: " + rpgData.getAlchemistConcoctionTicks());

                    // Update all fields
                    rpgData.setAlchemistConcoction(message.concoctionActive);
                    rpgData.setAlchemistConcoctionTicks(message.concoctionTicks);  // ADDED
                    rpgData.setAlchemistInjectionActive(message.injectionActive);
                    rpgData.setAlchemistClickPattern(message.clickPattern);
                    rpgData.setAlchemistBuffMode(message.buffMode);
                    rpgData.setAlchemistSelectedReagent(message.selectedReagent);

                    System.out.println("[CLIENT] AFTER update:");
                    System.out.println("  - New Concoction state: " + rpgData.isAlchemistConcoction());
                    System.out.println("  - New Ticks: " + rpgData.getAlchemistConcoctionTicks());

                    // VERIFICATION: Read it back immediately to confirm
                    boolean verify = rpgData.isAlchemistConcoction();
                    int verifyTicks = rpgData.getAlchemistConcoctionTicks();
                    System.out.println("[CLIENT] VERIFICATION READ:");
                    System.out.println("  - Verified Concoction: " + verify);
                    System.out.println("  - Verified Ticks: " + verifyTicks);

                    if (verify != message.concoctionActive) {
                        System.err.println("[CLIENT] ❌ ERROR: Data did NOT persist! Expected: " +
                                message.concoctionActive + " but got: " + verify);
                    } else {
                        System.out.println("[CLIENT] ✅ SUCCESS: Data verified and persisted!");
                    }

                    System.out.println("[CLIENT] ========================================");
                } else {
                    System.err.println("[CLIENT] ❌ ERROR: RPG data is null!");
                }
            } else {
                System.err.println("[CLIENT] ❌ ERROR: Player is null!");
            }
        });
    }
}