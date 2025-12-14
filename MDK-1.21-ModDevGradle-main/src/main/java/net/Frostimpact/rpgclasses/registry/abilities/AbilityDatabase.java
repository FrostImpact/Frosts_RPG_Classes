package net.Frostimpact.rpgclasses.registry.abilities;

import java.util.HashMap;
import java.util.Map;

public class AbilityDatabase {

    private static final Map<String, AbilityStats> ABILITY_MAP = new HashMap<>();

    public static void registerAll() {

        // BLADEDANCER
        register("dash", new AbilityStats("Dash", 5, 15));
        register("blade_dance", new AbilityStats("Blade Dance", 12, 35));
        register("parry", new AbilityStats("Parry", 6, 15));
        register("blade_waltz", new AbilityStats("Blade Waltz", 30, 50));

        // JUGGERNAUT
        register("swap", new AbilityStats("Swap", 3, 10));
        register("crush", new AbilityStats("Crush", 5, 25));
        register("fortify", new AbilityStats("Fortify", 10, 25));
        register("leap", new AbilityStats("Leap", 12, 35));

        // MANAFORGE
        register("magic_missile", new AbilityStats("Magic Missile", 2, 10));
        register("surge", new AbilityStats("Surge", 20, 40));
        register("open_rift", new AbilityStats("Open Rift", 15, 35));
        register("coalescence", new AbilityStats("Coalescence", 25, 30));

        // MARKSMAN
        register("seekers", new AbilityStats("Seekers", 5, 5)); // Variable mana: 5 per charge
        register("vault", new AbilityStats("Vault", 8, 15));
        register("updraft", new AbilityStats("Updraft", 12, 15));
        register("arrow_rain", new AbilityStats("Arrow Rain", 15, 30));

        // MERCENARY
        register("cloak", new AbilityStats("Cloak", 7, 10));
        register("stun_bolt", new AbilityStats("Stun Bolt", 6, 15));
        register("cycle_quiver", new AbilityStats("Cycle Quiver", 2, 0));
        register("hired_gun", new AbilityStats("Hired Gun", 40, 40));

        // RULER
        register("call_to_arms", new AbilityStats("Call to Arms", 12, 20));
        register("invigorate", new AbilityStats("Invigorate", 20, 40));
        register("regroup", new AbilityStats("Regroup", 15, 30));
        register("rally", new AbilityStats("Rally", 25, 10));

        // ARTIFICER
        register("turret", new AbilityStats("Turret", 7, 20));
        register("tower", new AbilityStats("Tower", 14, 20));
        register("reposition", new AbilityStats("Reposition", 4, 10));
        register("self_destruct", new AbilityStats("Self-Destruct", 20, 40));

        // MIRAGE
        register("reflections", new AbilityStats("Reflections", 4, 15));
        register("shadowstep", new AbilityStats("Shadowstep", 12, 10));
        register("recall", new AbilityStats("Recall", 8, 20));
        register("fracture_line", new AbilityStats("Fracture Line", 20, 40));

        // ALCHEMIST
        register("flask", new AbilityStats("Flask", 9, 20));
        register("volatile_mix", new AbilityStats("Volatile Mix", 15, 25));
        register("distill", new AbilityStats("Distill", 12, 30));
        register("injection", new AbilityStats("Injection", 18, 30));
    }

    private static void register(String id, AbilityStats stats) {
        ABILITY_MAP.put(id, stats);
    }

    public static AbilityStats getAbility(String id) {
        return ABILITY_MAP.get(id);
    }
}