package net.Frostimpact.rpgclasses.ability.ALCHEMIST;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;

public class FlaskAbility extends Ability {

    public FlaskAbility() {
        super("flask");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Activate CONCOCTION mode for 6 seconds (120 ticks)
        rpgData.setAlchemistConcoction(true);
        rpgData.setAlchemistConcoctionTicks(120);
        rpgData.setAlchemistClickPattern("");
        rpgData.setAlchemistBuffMode(player.isShiftKeyDown());

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§d⚗ CONCOCTION activated! " + 
                (rpgData.isAlchemistBuffMode() ? "§a[BUFF MODE]" : "§c[DEBUFF MODE]")));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}
