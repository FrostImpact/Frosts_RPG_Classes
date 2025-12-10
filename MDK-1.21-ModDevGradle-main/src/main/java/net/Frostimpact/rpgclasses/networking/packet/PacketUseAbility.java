package net.Frostimpact.rpgclasses.networking.packet;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.ability.AbilityRegistry;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketUseAbility implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<PacketUseAbility> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "use_ability"));

    public static final StreamCodec<FriendlyByteBuf, PacketUseAbility> STREAM_CODEC = StreamCodec.of(
            (buf, message) -> buf.writeUtf(message.abilityId),
            (buf) -> new PacketUseAbility(buf.readUtf())
    );

    private final String abilityId;

    public PacketUseAbility(String abilityId) {
        this.abilityId = abilityId;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PacketUseAbility message, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                PlayerRPGData rpgData = player.getData(ModAttachments.PLAYER_RPG);

                // Get the ability
                Ability ability = AbilityRegistry.getAbility(message.abilityId);

                if (ability == null) {
                    player.sendSystemMessage(Component.literal("§cUnknown ability: " + message.abilityId));
                    return;
                }

                // Check if player can use it
                if (!ability.canUse(rpgData)) {
                    if (rpgData.getAbilityCooldown(ability.getId()) > 0) {
                        int secondsLeft = (rpgData.getAbilityCooldown(ability.getId()) + 19) / 20;
                        player.sendSystemMessage(Component.literal("§c" + ability.getName() + " on cooldown! (" + secondsLeft + "s)"));
                    } else if (rpgData.getMana() < ability.getManaCost()) {
                        player.sendSystemMessage(Component.literal("§cNot enough mana! Need " + ability.getManaCost()));
                    }
                    return;
                }

                // Execute the ability
                if (ability.execute(player, rpgData)) {
                    // Success - ability handles its own effects
                } else {
                    player.sendSystemMessage(Component.literal("§cFailed to use " + ability.getName()));
                }
            }
        });
    }
}

