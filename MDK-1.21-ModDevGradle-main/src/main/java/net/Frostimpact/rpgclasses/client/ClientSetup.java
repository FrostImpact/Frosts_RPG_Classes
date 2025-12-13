package net.Frostimpact.rpgclasses.client;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.client.renderer.MagicMissileRenderer;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.Frostimpact.rpgclasses.client.renderer.SeekerArrowRenderer;
import net.Frostimpact.rpgclasses.client.renderer.VaultProjectileRenderer;
import net.Frostimpact.rpgclasses.client.renderer.StunBoltRenderer;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MAGIC_MISSILE.get(), MagicMissileRenderer::new);
        event.registerEntityRenderer(ModEntities.SEEKER_ARROW.get(), SeekerArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.VAULT_PROJECTILE.get(), VaultProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.STUN_BOLT.get(), StunBoltRenderer::new); // ADD THIS LINE
        System.out.println("RPG Classes: Entity Renderers Registered!");
    }





}