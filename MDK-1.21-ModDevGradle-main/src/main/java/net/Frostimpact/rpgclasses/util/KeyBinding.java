package net.Frostimpact.rpgclasses.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class KeyBinding {

    // BLADEDANCER Keys
    public static final KeyMapping DASH_KEY = new KeyMapping(
            "key.rpgclasses.dash",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "key.category.rpgclasses"
    );

    public static final KeyMapping BLADE_DANCE_KEY = new KeyMapping(
            "key.rpgclasses.blade_dance",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C,
            "key.category.rpgclasses"
    );

    public static final KeyMapping PARRY_KEY = new KeyMapping(
            "key.rpgclasses.parry",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            "key.category.rpgclasses"
    );

    public static final KeyMapping FINAL_WALTZ_KEY = new KeyMapping(
            "key.rpgclasses.blade_waltz",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Z,
            "key.category.rpgclasses"
    );

    // JUGGERNAUT Keys
    public static final KeyMapping SWAP_KEY = new KeyMapping(
            "key.rpgclasses.swap",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "key.category.rpgclasses"
    );

    public static final KeyMapping CRUSH_KEY = new KeyMapping(
            "key.rpgclasses.crush",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C,
            "key.category.rpgclasses"
    );

    public static final KeyMapping FORTIFY_KEY = new KeyMapping(
            "key.rpgclasses.fortify",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            "key.category.rpgclasses"
    );

    public static final KeyMapping LEAP_KEY = new KeyMapping(
            "key.rpgclasses.leap",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Z,
            "key.category.rpgclasses"
    );

    // MANAFORGE Keys
    public static final KeyMapping MAGIC_MISSILE_KEY = new KeyMapping(
            "key.rpgclasses.magic_missile",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "key.category.rpgclasses"
    );

    public static final KeyMapping SURGE_KEY = new KeyMapping(
            "key.rpgclasses.surge",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C,
            "key.category.rpgclasses"
    );

    public static final KeyMapping OPEN_RIFT_KEY = new KeyMapping(
            "key.rpgclasses.open_rift",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            "key.category.rpgclasses"
    );

    public static final KeyMapping COALESCENCE_KEY = new KeyMapping(
            "key.rpgclasses.coalescence",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Z,
            "key.category.rpgclasses"
    );

    // MARKSMAN Keys
    public static final KeyMapping SEEKERS_KEY = new KeyMapping(
            "key.rpgclasses.seekers",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "key.category.rpgclasses"
    );

    public static final KeyMapping VAULT_KEY = new KeyMapping(
            "key.rpgclasses.vault",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C,
            "key.category.rpgclasses"
    );

    public static final KeyMapping UPDRAFT_KEY = new KeyMapping(
            "key.rpgclasses.updraft",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            "key.category.rpgclasses"
    );

    public static final KeyMapping ARROW_RAIN_KEY = new KeyMapping(
            "key.rpgclasses.arrow_rain",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Z,
            "key.category.rpgclasses"
    );

    // MERCENARY Keys
    public static final KeyMapping CLOAK_KEY = new KeyMapping(
            "key.rpgclasses.cloak",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_LSHIFT,
            "key.category.rpgclasses"
    );

    public static final KeyMapping STUN_BOLT_KEY = new KeyMapping(
            "key.rpgclasses.stun_bolt",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "key.category.rpgclasses"
    );

    public static final KeyMapping CYCLE_QUIVER_KEY = new KeyMapping(
            "key.rpgclasses.cycle_quiver",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C,
            "key.category.rpgclasses"
    );

    public static final KeyMapping HIRED_GUN_KEY = new KeyMapping(
            "key.rpgclasses.hired_gun",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Z,
            "key.category.rpgclasses"
    );

    // RULER Keys
    public static final KeyMapping CALL_TO_ARMS_KEY = new KeyMapping(
            "key.rpgclasses.call_to_arms",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "key.category.rpgclasses"
    );

    public static final KeyMapping INVIGORATE_KEY = new KeyMapping(
            "key.rpgclasses.invigorate",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C,
            "key.category.rpgclasses"
    );

    public static final KeyMapping REGROUP_KEY = new KeyMapping(
            "key.rpgclasses.regroup",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            "key.category.rpgclasses"
    );

    public static final KeyMapping RALLY_KEY = new KeyMapping(
            "key.rpgclasses.rally",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_Z,
            "key.category.rpgclasses"
    );
}