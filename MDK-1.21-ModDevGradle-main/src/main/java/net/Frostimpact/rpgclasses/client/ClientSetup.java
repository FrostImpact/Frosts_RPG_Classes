package net.Frostimpact.rpgclasses.client;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.client.renderer.MagicMissileRenderer;
import net.Frostimpact.rpgclasses.client.renderer.SeekerArrowRenderer;
import net.Frostimpact.rpgclasses.client.renderer.VaultProjectileRenderer;
import net.Frostimpact.rpgclasses.client.renderer.StunBoltRenderer;
import net.Frostimpact.rpgclasses.entity.summon.ArcherSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.KnightSummonEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.minecraft.client.model.HumanoidModel; // CHANGED: Import HumanoidModel
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientSetup {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MAGIC_MISSILE.get(), MagicMissileRenderer::new);
        event.registerEntityRenderer(ModEntities.SEEKER_ARROW.get(), SeekerArrowRenderer::new);
        event.registerEntityRenderer(ModEntities.VAULT_PROJECTILE.get(), VaultProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.STUN_BOLT.get(), StunBoltRenderer::new);

        // RULER summon entities
        event.registerEntityRenderer(ModEntities.KNIGHT_SUMMON.get(), KnightRenderer::new);
        event.registerEntityRenderer(ModEntities.ARCHER_SUMMON.get(), ArcherRenderer::new);

        System.out.println("RPG Classes: Entity Renderers Registered!");
    }

    // Knight summon renderer
    // CHANGED: ZombieModel -> HumanoidModel
    private static class KnightRenderer extends MobRenderer<KnightSummonEntity, HumanoidModel<KnightSummonEntity>> {
        public KnightRenderer(EntityRendererProvider.Context context) {
            // We can still use ModelLayers.ZOMBIE because the bone structure is the same
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
        }

        @Override
        public ResourceLocation getTextureLocation(KnightSummonEntity entity) {
            return ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");
        }
    }

    // Archer summon renderer
    // CHANGED: ZombieModel -> HumanoidModel
    private static class ArcherRenderer extends MobRenderer<ArcherSummonEntity, HumanoidModel<ArcherSummonEntity>> {
        public ArcherRenderer(EntityRendererProvider.Context context) {
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5f);
        }

        @Override
        public ResourceLocation getTextureLocation(ArcherSummonEntity entity) {
            return ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");
        }
    }
}