package net.Frostimpact.rpgclasses.ability;
import net.Frostimpact.rpgclasses.ability.MARKSMAN.*;
import net.Frostimpact.rpgclasses.ability.BLADEDANCER.BLADE_DANCE.BladeDanceAbility;
import net.Frostimpact.rpgclasses.ability.BLADEDANCER.DashAbility;
import net.Frostimpact.rpgclasses.ability.BLADEDANCER.ParryAbility;
import net.Frostimpact.rpgclasses.ability.BLADEDANCER.FinalWaltzAbility;
import net.Frostimpact.rpgclasses.ability.JUGGERNAUT.SwapAbility;
import net.Frostimpact.rpgclasses.ability.JUGGERNAUT.CrushAbility;
import net.Frostimpact.rpgclasses.ability.JUGGERNAUT.FortifyAbility;
import net.Frostimpact.rpgclasses.ability.JUGGERNAUT.LeapAbility;
import net.Frostimpact.rpgclasses.ability.MANAFORGE.MagicMissileAbility;
import net.Frostimpact.rpgclasses.ability.MANAFORGE.SurgeAbility;
import net.Frostimpact.rpgclasses.ability.MANAFORGE.OpenRiftAbility;
import net.Frostimpact.rpgclasses.ability.MANAFORGE.CoalescenceAbility;
import net.Frostimpact.rpgclasses.ability.MERCENARY.CloakAbility;
import net.Frostimpact.rpgclasses.ability.MERCENARY.CycleQuiverAbility;
import net.Frostimpact.rpgclasses.ability.MERCENARY.HiredGunAbility;
import net.Frostimpact.rpgclasses.ability.MERCENARY.StunBoltAbility;
import net.Frostimpact.rpgclasses.ability.RULER.CallToArmsAbility;
import net.Frostimpact.rpgclasses.ability.RULER.InvigorateAbility;
import net.Frostimpact.rpgclasses.ability.RULER.RegroupAbility;
import net.Frostimpact.rpgclasses.ability.RULER.RallyAbility;
import net.Frostimpact.rpgclasses.ability.MIRAGE.ReflectionsAbility;
import net.Frostimpact.rpgclasses.ability.MIRAGE.ShadowstepAbility;
import net.Frostimpact.rpgclasses.ability.MIRAGE.RecallAbility;
import net.Frostimpact.rpgclasses.ability.MIRAGE.FractureLineAbility;
import net.Frostimpact.rpgclasses.ability.ALCHEMIST.FlaskAbility;
import net.Frostimpact.rpgclasses.ability.ALCHEMIST.VolatileMixAbility;
import net.Frostimpact.rpgclasses.ability.ALCHEMIST.DistillAbility;
import net.Frostimpact.rpgclasses.ability.ALCHEMIST.InjectionAbility;

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

        // MANAFORGE Abilities
        register(new MagicMissileAbility());
        register(new SurgeAbility());
        register(new OpenRiftAbility());
        register(new CoalescenceAbility());

        //MARKSMAN Abilities
        register(new SeekersAbility());
        register(new VaultAbility());
        register(new UpdraftAbility());
        register(new ArrowRainAbility());

        // MERCENARY Abilities
        register(new CloakAbility());
        register(new StunBoltAbility());
        register(new CycleQuiverAbility());
        register(new HiredGunAbility());

        // RULER Abilities
        register(new CallToArmsAbility());
        register(new InvigorateAbility());
        register(new RegroupAbility());
        register(new RallyAbility());

        // ARTIFICER Abilities
        register(new net.Frostimpact.rpgclasses.ability.ARTIFICER.TurretAbility());
        register(new net.Frostimpact.rpgclasses.ability.ARTIFICER.TowerAbility());
        register(new net.Frostimpact.rpgclasses.ability.ARTIFICER.RepositionAbility());
        register(new net.Frostimpact.rpgclasses.ability.ARTIFICER.SelfDestructAbility());

        // MIRAGE Abilities
        register(new ReflectionsAbility());
        register(new ShadowstepAbility());
        register(new RecallAbility());
        register(new FractureLineAbility());

        // ALCHEMIST Abilities
        register(new FlaskAbility());
        register(new VolatileMixAbility());
        register(new DistillAbility());
        register(new InjectionAbility());
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