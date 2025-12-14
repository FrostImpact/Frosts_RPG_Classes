package net.Frostimpact.rpgclasses.registry;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.effect.BrittleEffect;
import net.Frostimpact.rpgclasses.effect.CorrosionEffect;
import net.Frostimpact.rpgclasses.effect.FreezeEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, RpgClassesMod.MOD_ID);

    public static final DeferredHolder<MobEffect, CorrosionEffect> CORROSION =
            MOB_EFFECTS.register("corrosion", CorrosionEffect::new);

    public static final DeferredHolder<MobEffect, BrittleEffect> BRITTLE =
            MOB_EFFECTS.register("brittle", BrittleEffect::new);

    public static final DeferredHolder<MobEffect, FreezeEffect> FREEZE =
            MOB_EFFECTS.register("freeze", FreezeEffect::new);

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
