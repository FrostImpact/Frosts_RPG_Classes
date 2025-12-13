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
    private final int tempoStacks;
    private final boolean tempoActive;
    private final int seekerCharges;

    public PacketSyncCooldowns(Map<String, Integer> cooldowns, int mana, int maxMana,
                               int juggernautCharge, int juggernautMaxCharge,
                               boolean isShieldMode, int manaforgeArcana) {
        this(cooldowns, mana, maxMana, juggernautCharge, juggernautMaxCharge, isShieldMode, manaforgeArcana, 0, false, 0);
    }

    public PacketSyncCooldowns(Map<String, Integer> cooldowns, int mana, int maxMana,
                               int juggernautCharge, int juggernautMaxCharge,
                               boolean isShieldMode, int manaforgeArcana,
                               int tempoStacks, boolean tempoActive, int seekerCharges) {
        this.cooldowns = cooldowns;
        this.mana = mana;
        this.maxMana = maxMana;
        this.juggernautCharge = juggernautCharge;
        this.juggernautMaxCharge = juggernautMaxCharge;
        this.isShieldMode = isShieldMode;
        this.manaforgeArcana = manaforgeArcana;
        this.tempoStacks = tempoStacks;
        this.tempoActive = tempoActive;
        this.seekerCharges = seekerCharges;
    }

    private static void encode(FriendlyByteBuf buf, PacketSyncCooldowns message) {
        buf.writeInt(message.mana);
        buf.writeInt(message.maxMana);
        buf.writeInt(message.juggernautCharge);
        buf.writeInt(message.juggernautMaxCharge);
        buf.writeBoolean(message.isShieldMode);
        buf.writeInt(message.manaforgeArcana);
        buf.writeInt(message.tempoStacks);
        buf.writeBoolean(message.tempoActive);
        buf.writeInt(message.seekerCharges);

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
        int tempoStacks = buf.readInt();
        boolean tempoActive = buf.readBoolean();
        int seekerCharges = buf.readInt();

        int size = buf.readInt();
        Map<String, Integer> cooldowns = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readUtf();
            int value = buf.readInt();
            cooldowns.put(key, value);
        }

        return new PacketSyncCooldowns(cooldowns, mana, maxMana, juggernautCharge,
                juggernautMaxCharge, isShieldMode, manaforgeArcana, tempoStacks, tempoActive, seekerCharges);
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
                        rpg.setManaforgeArcana(message.manaforgeArcana);

                        // Update TEMPO data
                        rpg.setTempoStacks(message.tempoStacks);
                        rpg.setTempoActive(message.tempoActive);

                        // Update SEEKER charges
                        rpg.setMarksmanSeekerCharges(message.seekerCharges);

                        // Clear and update all cooldowns
                        rpg.clearAllCooldowns();
                        for (Map.Entry<String, Integer> entry : message.cooldowns.entrySet()) {
                            rpg.setAbilityCooldown(entry.getKey(), entry.getValue());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("RPG Classes: Error handling cooldown sync packet: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}