package net.Frostimpact.rpgclasses.registry.abilities;

import java.util.HashMap;
import java.util.Map;

public class AbilityDatabase {

    private static final Map<String, AbilityStats> ABILITY_MAP = new HashMap<>();

    public static void registerAll() {

        //BLADEDANCER
        register("dash", new AbilityStats("Dash", 5, 15));
        register("blade_dance", new AbilityStats("Blade Dance", 12, 35));
        register("parry",     new AbilityStats("Parry", 6, 15));
        register("blade_waltz",     new AbilityStats("Blade Waltz", 30, 50));
    }

    private static void register(String id, AbilityStats stats) {
        ABILITY_MAP.put(id, stats);
    }

    public static AbilityStats getAbility(String id) {
        return ABILITY_MAP.get(id);
    }
}
