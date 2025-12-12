package net.Frostimpact.rpgclasses.ability;

import net.Frostimpact.rpgclasses.ability.BLADEDANCER.BLADE_DANCE.BladeDanceAbility;
import net.Frostimpact.rpgclasses.ability.BLADEDANCER.DashAbility;
import net.Frostimpact.rpgclasses.ability.BLADEDANCER.ParryAbility;
import net.Frostimpact.rpgclasses.ability.BLADEDANCER.FinalWaltzAbility;
import net.Frostimpact.rpgclasses.ability.JUGGERNAUT.SwapAbility;
import net.Frostimpact.rpgclasses.ability.JUGGERNAUT.CrushAbility;
import net.Frostimpact.rpgclasses.ability.JUGGERNAUT.FortifyAbility;
import net.Frostimpact.rpgclasses.ability.JUGGERNAUT.LeapAbility;

import java.util.HashMap;
import java.util.Map;

public class AbilityRegistry {

    private static final Map<String, Ability> ABILITIES = new HashMap<>();

    public static void registerAll() {
        // BLADEDANCER Abilities
        register(new DashAbility());
        register(new BladeDanceAbility());
        register(new ParryAbility());
        register(new FinalWaltzAbility());

        // JUGGERNAUT Abilities
        register(new SwapAbility());
        register(new CrushAbility());
        register(new FortifyAbility());
        register(new LeapAbility());
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