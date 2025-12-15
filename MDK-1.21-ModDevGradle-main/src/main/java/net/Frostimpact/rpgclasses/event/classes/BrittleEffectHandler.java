package net.Frostimpact.rpgclasses.event.classes;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.registry.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID)
public class BrittleEffectHandler {

    @SubscribeEvent
    public static void onLivingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity target) {
            // Check if the target has the BRITTLE effect
            if (target.hasEffect(ModEffects.BRITTLE)) {
                // Increase damage by 20%
                float originalDamage = event.getAmount();
                float increasedDamage = originalDamage * 1.2f;
                event.setAmount(increasedDamage);
            }
        }
    }
}
