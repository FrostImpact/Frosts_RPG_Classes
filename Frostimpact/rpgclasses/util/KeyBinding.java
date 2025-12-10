package net.Frostimpact.rpgclasses.util;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext; // Add this import if still missing
import net.neoforged.neoforge.client.settings.KeyModifier;

public class KeyBinding {

    public static final KeyMapping DASH_KEY = new KeyMapping(
            "key.rpgclasses.dash",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_V,
            "key.category.rpgclasses"
    );

    // DELETE the old 'public static void register()' method entirely.
    // It is no longer needed.
}