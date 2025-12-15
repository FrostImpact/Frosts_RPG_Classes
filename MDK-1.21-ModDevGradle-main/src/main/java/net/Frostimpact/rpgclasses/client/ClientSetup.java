package net.Frostimpact.rpgclasses.client;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.client.renderer.MagicMissileRenderer;
import net.Frostimpact.rpgclasses.client.renderer.SeekerArrowRenderer;
import net.Frostimpact.rpgclasses.client.renderer.VaultProjectileRenderer;
import net.Frostimpact.rpgclasses.client.renderer.StunBoltRenderer;
import net.Frostimpact.rpgclasses.client.renderer.AfterimageRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.Frostimpact.rpgclasses.entity.summon.ArcherSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.KnightSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.TurretSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.ShockTowerEntity;
import net.Frostimpact.rpgclasses.entity.summon.WindTowerEntity;
import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
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

        // ALCHEMIST projectile entities
        event.registerEntityRenderer(ModEntities.ALCHEMIST_POTION.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.INJECTION_BOLT.get(), ThrownItemRenderer::new);

        // RULER summon entities
        event.registerEntityRenderer(ModEntities.KNIGHT_SUMMON.get(), KnightRenderer::new);
        event.registerEntityRenderer(ModEntities.ARCHER_SUMMON.get(), ArcherRenderer::new);

        // ARTIFICER summon entities
        event.registerEntityRenderer(ModEntities.TURRET_SUMMON.get(), TurretRenderer::new);
        event.registerEntityRenderer(ModEntities.SHOCK_TOWER.get(), ShockTowerRenderer::new);
        event.registerEntityRenderer(ModEntities.WIND_TOWER.get(), WindTowerRenderer::new);

        // MIRAGE summon entities
        event.registerEntityRenderer(ModEntities.AFTERIMAGE.get(), AfterimageRenderer::new);

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

    // Turret summon renderer - use a smaller model since turrets are stationary
    private static class TurretRenderer extends MobRenderer<TurretSummonEntity, HumanoidModel<TurretSummonEntity>> {
        public TurretRenderer(EntityRendererProvider.Context context) {
            // Smaller shadow radius for compact appearance
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.4f);
        }

        @Override
        public ResourceLocation getTextureLocation(TurretSummonEntity entity) {
            // Keep iron golem texture for metallic turret appearance
            return ResourceLocation.withDefaultNamespace("textures/entity/iron_golem/iron_golem.png");
        }
    }

    // Shock Tower renderer - scaled differently with blue-themed texture
    private static class ShockTowerRenderer extends MobRenderer<ShockTowerEntity, HumanoidModel<ShockTowerEntity>> {
        public ShockTowerRenderer(EntityRendererProvider.Context context) {
            // Use ARMOR_STAND model with different scale for distinct appearance
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.5f);
        }

        @Override
        public ResourceLocation getTextureLocation(ShockTowerEntity entity) {
            // Use wither skeleton texture for dark/electric themed appearance
            return ResourceLocation.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");
        }
    }

    // Wind Tower renderer - different scale and texture for green/wind theme
    private static class WindTowerRenderer extends MobRenderer<WindTowerEntity, HumanoidModel<WindTowerEntity>> {
        public WindTowerRenderer(EntityRendererProvider.Context context) {
            // Slightly larger shadow for wind tower
            super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.6f);
        }

        @Override
        public ResourceLocation getTextureLocation(WindTowerEntity entity) {
            // Use zombie texture for green/wind themed appearance
            return ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");
        }
    }
}