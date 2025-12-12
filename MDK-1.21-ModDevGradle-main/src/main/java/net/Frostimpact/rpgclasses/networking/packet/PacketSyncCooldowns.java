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
    private final int juggernautCharge;
    private final int juggernautMaxCharge;
    private final boolean isShieldMode;
    private final int manaforgeArcana;

    private static long lastLogTime = 0;
    private static int lastLoggedArcana = -1;

    public PacketSyncCooldowns(Map<String, Integer> cooldowns, int mana, int maxMana) {
        this(cooldowns, mana, maxMana, 0, 100, true, 0);
    }

    public PacketSyncCooldowns(Map<String, Integer> cooldowns, int mana, int maxMana,
                               int juggernautCharge, int juggernautMaxCharge, boolean isShieldMode) {
        this(cooldowns, mana, maxMana, juggernautCharge, juggernautMaxCharge, isShieldMode, 0);
    }

    public PacketSyncCooldowns(Map<String, Integer> cooldowns, int mana, int maxMana,
                               int juggernautCharge, int juggernautMaxCharge,
                               boolean isShieldMode, int manaforgeArcana) {
        this.cooldowns = cooldowns;
        this.mana = mana;
        this.maxMana = maxMana;
        this.juggernautCharge = juggernautCharge;
        this.juggernautMaxCharge = juggernautMaxCharge;
        this.isShieldMode = isShieldMode;
        this.manaforgeArcana = manaforgeArcana;
    }

    private static void encode(FriendlyByteBuf buf, PacketSyncCooldowns message) {
        buf.writeInt(message.mana);
        buf.writeInt(message.maxMana);
        buf.writeInt(message.juggernautCharge);
        buf.writeInt(message.juggernautMaxCharge);
        buf.writeBoolean(message.isShieldMode);
        buf.writeInt(message.manaforgeArcana);

        buf.writeInt(message.cooldowns.size());
        for (Map.Entry<String, Integer> entry : message.cooldowns.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    private static PacketSyncCooldowns decode(FriendlyByteBuf buf) {
        int mana = buf.readInt();
        int maxMana = buf.readInt();
        int juggernautCharge = buf.readInt();
        int juggernautMaxCharge = buf.readInt();
        boolean isShieldMode = buf.readBoolean();
        int manaforgeArcana = buf.readInt();

        int size = buf.readInt();
        Map<String, Integer> cooldowns = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            int value = buf.readInt();
            cooldowns.put(key, value);
        }

        return new PacketSyncCooldowns(cooldowns, mana, maxMana, juggernautCharge,
                juggernautMaxCharge, isShieldMode, manaforgeArcana);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketSyncCooldowns message, IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                if (Minecraft.getInstance().player != null) {
                    PlayerRPGData rpg = Minecraft.getInstance().player.getData(ModAttachments.PLAYER_RPG);

                    if (rpg != null) {
                        // Update mana
                        rpg.setMana(message.mana);
                        rpg.setMaxMana(message.maxMana);

                        // Update juggernaut data
                        rpg.setJuggernautCharge(message.juggernautCharge);
                        rpg.setJuggernautShieldMode(message.isShieldMode);

                        // Update manaforge arcana
                        int oldArcana = rpg.getManaforgeArcana();
                        rpg.setManaforgeArcana(message.manaforgeArcana);

                        // Debug logging - only log when arcana changes or every 5 seconds
                        long currentTime = System.currentTimeMillis();
                        if (message.manaforgeArcana != lastLoggedArcana || (currentTime - lastLogTime) > 5000) {
                            System.out.println("RPG Classes [PACKET]: Received arcana update: " + oldArcana + " -> " + message.manaforgeArcana);
                            lastLoggedArcana = message.manaforgeArcana;
                            lastLogTime = currentTime;
                        }

                        // Clear and update all cooldowns
                        rpg.clearAllCooldowns();
                        for (Map.Entry<String, Integer> entry : message.cooldowns.entrySet()) {
                            rpg.setAbilityCooldown(entry.getKey(), entry.getValue());
                        }
                    } else {
                        System.err.println("RPG Classes [PACKET]: Player RPG data is null!");
                    }
                }
            } catch (Exception e) {
                System.err.println("RPG Classes: Error handling cooldown sync packet: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}