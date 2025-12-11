package net.Frostimpact.rpgclasses.ability;

import net.Frostimpact.rpgclasses.ability.BLADEDANCER.BladeDanceAbility;
import net.Frostimpact.rpgclasses.ability.BLADEDANCER.DashAbility;

import java.util.HashMap;
import java.util.Map;

public class AbilityRegistry {

    private static final Map<String, Ability> ABILITIES = new HashMap<>();

    public static void registerAll() {
        register(new DashAbility());
        // Add more abilities here as you create them
        register(new BladeDanceAbility());
        // register(new ParryAbility());
        // register(new BladeWaltzAbility());
    }

    private static void register(Ability ability) {
        ABILITIES.put(ability.getId(), ability);
        System.out.println("Registered ability: " + ability.getName());
    }

    public static Ability getAbility(String id) {
        return ABILITIES.get(id);
    }

    public static boolean hasAbility(String id) {
        return ABILITIES.containsKey(id);
    }
}
