package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;

// NEOFORGE IMPORTS
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketDash implements CustomPacketPayload {

    // 1. Define the location using your actual MOD_ID
    public static final CustomPacketPayload.Type<PacketDash> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "packet_dash"));

    // 2. Define the Codec (Empty packet)
    public static final StreamCodec<FriendlyByteBuf, PacketDash> STREAM_CODEC = StreamCodec.of(
            // Encoder (write): Does nothing since the packet carries no data
            (buf, message) -> {},
            // Decoder (read): Simply creates a new instance of the packet
            (buf) -> new PacketDash()
    );

    public PacketDash() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 3. The New NeoForge Handle Method
    public static void handle(PacketDash message, IPayloadContext context) {

        // Always enqueue work to the main thread
        context.enqueueWork(() -> {
            // In NeoForge, we cast the player from the context
            if (context.player() instanceof ServerPlayer player) {

                // --- NEW DATA ATTACHMENT SYSTEM ---
                PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

                // Now we can use the data just like before
                if (rpg.getCurrentClass().equals("BLADEDANCER")) {

                    // *** START OF TEMPORARY DISABLE ***
                    // We comment out the 'if' condition that checks for cooldown and mana,
                    // and also comment out the consumption and failure message.

                    /*
                    if (rpg.getDashCooldown() <= 0 && rpg.getMana() >= 15) {
                    */

                    // Success! Perform the Dash
                    // Comment out resource consumption
                    // rpg.setDashCooldown(100);
                    // rpg.useMana(15);

                    // Physical effects (Core Dash Functionality)
                    player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1f, 1f);
                    player.hurtMarked = true;

                    // Push the player forward
                    player.setDeltaMovement(player.getLookAngle().scale(3));

                    // Sync data back to client (Optional, but good practice later)
                    // ModMessages.sendToPlayer(new PacketSyncData(rpg.getMana()), player);

                    /*
                    } else {
                        // Failure Message
                        String msg = (rpg.getDashCooldown() > 0) ? "Dash on Cooldown!" : "Not enough Mana!";
                        player.sendSystemMessage(Component.literal(msg));
                    }
                    */
                    // *** END OF TEMPORARY DISABLE ***
                }
            }
        });
    }
}