package net.Frostimpact.rpgclasses.ability;

import net.Frostimpact.rpgclasses.registry.abilities.AbilityDatabase;
import net.Frostimpact.rpgclasses.registry.abilities.AbilityStats;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;

public abstract class Ability {
    protected final String id;
    protected final AbilityStats stats;

    public Ability(String id) {
        this.id = id;
        this.stats = AbilityDatabase.getAbility(id);
        if (this.stats == null) {
            throw new IllegalStateException("No stats found for ability: " + id);
        }
    }

    public abstract boolean execute(ServerPlayer player, PlayerRPGData rpgData);

    public boolean canUse(PlayerRPGData rpgData) {
        int cooldownTicks = stats.getAbility_Cooldown() * 20; // Convert seconds to ticks
        return rpgData.getAbilityCooldown(id) <= 0 &&
                rpgData.getMana() >= stats.getAbility_Mana();
    }

    public String getId() { return id; }
    public String getName() { return stats.getAbility_Name(); }
    public int getCooldownSeconds() { return stats.getAbility_Cooldown(); }
    public int getCooldownTicks() { return stats.getAbility_Cooldown() * 20; }
    public int getManaCost() { return stats.getAbility_Mana(); }
}