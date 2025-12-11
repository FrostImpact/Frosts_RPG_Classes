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

import java.util.HashMap;
import java.util.Map;

public class PacketSyncCooldowns implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketSyncCooldowns> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "sync_cooldowns"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncCooldowns> STREAM_CODEC = StreamCodec.of(
            PacketSyncCooldowns::encode,
            PacketSyncCooldowns::decode
    );

    private final Map<String, Integer> cooldowns;
    private final int mana;
    private final int maxMana;

    public PacketSyncCooldowns(Map<String, Integer> cooldowns, int mana, int maxMana) {
        this.cooldowns = cooldowns;
        this.mana = mana;
        this.maxMana = maxMana;
    }

    private static void encode(FriendlyByteBuf buf, PacketSyncCooldowns message) {
        // Write mana data
        buf.writeInt(message.mana);
        buf.writeInt(message.maxMana);

        // Write cooldown map size
        buf.writeInt(message.cooldowns.size());

        // Write each cooldown entry
        for (Map.Entry<String, Integer> entry : message.cooldowns.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    private static PacketSyncCooldowns decode(FriendlyByteBuf buf) {
        // Read mana data
        int mana = buf.readInt();
        int maxMana = buf.readInt();

        // Read cooldown map
        int size = buf.readInt();
        Map<String, Integer> cooldowns = new HashMap<>();

        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            int value = buf.readInt();
            cooldowns.put(key, value);
        }

        return new PacketSyncCooldowns(cooldowns, mana, maxMana);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncCooldowns message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                PlayerRPGData rpg = Minecraft.getInstance().player.getData(ModAttachments.PLAYER_RPG);

                // Update mana
                rpg.setMana(message.mana);
                rpg.setMaxMana(message.maxMana);

                // Clear and update all cooldowns
                rpg.clearAllCooldowns();
                for (Map.Entry<String, Integer> entry : message.cooldowns.entrySet()) {
                    rpg.setAbilityCooldown(entry.getKey(), entry.getValue());
                }
            }
        });
    }
}