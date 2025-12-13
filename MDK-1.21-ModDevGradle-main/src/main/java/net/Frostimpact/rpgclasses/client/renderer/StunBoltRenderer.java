package net.Frostimpact.rpgclasses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.Frostimpact.rpgclasses.entity.projectile.StunBoltEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import org.joml.Vector3f;

public class StunBoltRenderer extends EntityRenderer<StunBoltEntity> {

    private static final ResourceLocation TEXTURE = 
            ResourceLocation.withDefaultNamespace("textures/item/glowstone_dust.png");
    
    public StunBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StunBoltEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Billboard effect
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

        float size = 0.3f;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

        // Draw glowing yellow/electric bolt
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, -size, -size, 0, 0, 1);
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, size, -size, 0, 1, 1);
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, size, size, 0, 1, 0);
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, -size, size, 0, 0, 0);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f,
                           int light, float x, float y, float z, float u, float v) {

        Vector3f normal = new Vector3f(0.0f, 0.0f, 1.0f);
        normal.mul(matrix3f);

        consumer.addVertex(matrix4f, x, y, z)
                .setColor(255, 255, 100, 255) // Bright yellow
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(normal.x(), normal.y(), normal.z());
    }

    @Override
    public ResourceLocation getTextureLocation(StunBoltEntity entity) {
        return TEXTURE;
    }
}