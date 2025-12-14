package net.Frostimpact.rpgclasses.client.renderer;

import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AfterimageRenderer extends MobRenderer<AfterimageEntity, HumanoidModel<AfterimageEntity>> {
    
    private static final ResourceLocation TEXTURE = 
            ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");
    
    public AfterimageRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
    }
    
    @Override
    public ResourceLocation getTextureLocation(AfterimageEntity entity) {
        return TEXTURE;
    }
    
    // Make the afterimage model invisible - particles will be rendered instead
    @Override
    protected float getAlpha(AfterimageEntity entity) {
        return 0.0f; // Completely invisible
    }
}
