package net.Frostimpact.rpgclasses.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class BrittleEffect extends MobEffect {

    public BrittleEffect() {
        super(MobEffectCategory.HARMFUL, 0xC0C0C0); // Silver/grey color
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // No periodic effects
    }

    // Note: The 20% increased damage taken will be handled in damage event listeners
}
