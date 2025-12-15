package net.Frostimpact.rpgclasses.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CorrosionEffect extends MobEffect {

    public CorrosionEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513); // Brown color
        
        // Reduce armor effectiveness by 30%
        this.addAttributeModifier(
            Attributes.ARMOR,
            "corrosion_armor_reduction",
            -0.3,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // No periodic effects
    }
}
