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
}