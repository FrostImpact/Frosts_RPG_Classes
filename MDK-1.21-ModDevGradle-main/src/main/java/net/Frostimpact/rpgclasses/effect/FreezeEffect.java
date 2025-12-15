package net.Frostimpact.rpgclasses.effect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class FreezeEffect extends MobEffect {

    public FreezeEffect() {
        super(MobEffectCategory.HARMFUL, 0x87CEEB); // Light blue color
        
        // Completely prevent movement by setting speed to 0
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            ResourceLocation.fromNamespaceAndPath("rpgclasses", "freeze_movement_stop"),
            -1.0,
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false; // No periodic effects
    }
}
