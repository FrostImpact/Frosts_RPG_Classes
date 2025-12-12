package net.Frostimpact.rpgclasses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.entity.projectile.MagicMissileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix3f;

public class MagicMissileRenderer extends EntityRenderer<MagicMissileEntity> {

    private static final ResourceLocation TEXTURE = 
            ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "textures/entity/magic_missile.png");
    
    public MagicMissileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MagicMissileEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Billboard effect - always face camera
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

        // Size of the quad
        float size = 0.3f;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));

        // Draw a simple quad (2 triangles)
        // Bottom-left
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, -size, -size, 0, 0, 1);
        // Bottom-right
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, size, -size, 0, 1, 1);
        // Top-right
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, size, size, 0, 1, 0);
        // Top-left
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, -size, size, 0, 0, 0);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f,
                                int light, float x, float y, float z, float u, float v) {
        consumer.addVertex(matrix4f, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)

                .setLight(light)
                .setNormal(0.0f, 1.0f, 0.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(MagicMissileEntity entity) {
        return TEXTURE;
    }
}