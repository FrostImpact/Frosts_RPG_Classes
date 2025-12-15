package net.Frostimpact.rpgclasses.ability.ALCHEMIST;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncAlchemistState;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;

public class FlaskAbility extends Ability {

    public FlaskAbility() {
        super("flask");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        System.out.println("[SERVER] FLASK ABILITY EXECUTED!");
        System.out.println("[SERVER] Player shift key down: " + player.isShiftKeyDown());

        // Activate CONCOCTION mode for 6 seconds (120 ticks)
        rpgData.setAlchemistConcoction(true);
        rpgData.setAlchemistConcoctionTicks(120);
        rpgData.setAlchemistClickPattern("");
        rpgData.setAlchemistBuffMode(player.isShiftKeyDown());

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§d⚗ CONCOCTION activated! " +
                        (rpgData.isAlchemistBuffMode() ? "§a[BUFF MODE]" : "§c[DEBUFF MODE]")));

        System.out.println("[SERVER] Concoction state set - Active: " + rpgData.isAlchemistConcoction() +
                ", Ticks: " + rpgData.getAlchemistConcoctionTicks() +
                ", Buff Mode: " + rpgData.isAlchemistBuffMode());

        // Sync to client - UPDATED to include ticks
        PacketSyncAlchemistState packet = new PacketSyncAlchemistState(
                rpgData.isAlchemistConcoction(),
                // ADDED
                rpgData.getAlchemistConcoctionTicks(),
                rpgData.isAlchemistInjectionActive(),
                rpgData.getAlchemistClickPattern(),
                rpgData.isAlchemistBuffMode(),
                rpgData.getAlchemistSelectedReagent()
        );

        System.out.println("[SERVER] Sending sync packet to client...");
        ModMessages.sendToPlayer(packet, player);

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        System.out.println("[SERVER] FLASK ABILITY COMPLETE!");
        return true;
    }
}