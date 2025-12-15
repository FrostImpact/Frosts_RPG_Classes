package net.Frostimpact.rpgclasses.ability.ALCHEMIST;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;

public class VolatileMixAbility extends Ability {

    public VolatileMixAbility() {
        super("volatile_mix");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Set the volatile mix flag - next flask will create a lingering field
        rpgData.setAlchemistVolatileMixActive(true);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§5⚗ VOLATILE MIX ready! Next FLASK will leave a lingering field."));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}
