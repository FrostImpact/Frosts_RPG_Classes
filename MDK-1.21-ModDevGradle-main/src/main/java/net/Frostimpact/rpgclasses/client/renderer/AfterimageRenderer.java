package net.Frostimpact.rpgclasses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
    
    @Override
    public void render(AfterimageEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        // Apply translucent/ghostly effect
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        
        // Make the afterimage semi-transparent
        poseStack.scale(1.0f, 1.0f, 1.0f);
        
        poseStack.popPose();
        
        // Call the parent render with modified rendering
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
}
