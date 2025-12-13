package net.Frostimpact.rpgclasses.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.Frostimpact.rpgclasses.entity.projectile.SeekerArrowEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import org.joml.Vector3f; // <--- ADDED JOML Vector3f import

public class SeekerArrowRenderer extends EntityRenderer<SeekerArrowEntity> {

    // Default arrow texture from vanilla Minecraft
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");

    public SeekerArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SeekerArrowEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Rotate to face movement direction
        // Arrows point along the Z-axis, so we rotate based on pitch (XRot) and yaw (YRot)
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYRot() - 90.0f)); // Yaw
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));         // Pitch

        float size = 0.4f;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        // Using entityCutout for a standard texture that shouldn't flicker
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));

        // Draw a simple quad (representing the arrow texture)
        // Note: The default vanilla arrow is often a 3D model, but this quad rendering
        // is common for custom projectiles. It's drawn in the XY plane.

        // RGB for the green tint (0, 255, 0)
        int r = 0;
        int g = 255;
        int b = 0;

        // Vertex order: Bottom-left, Bottom-right, Top-right, Top-left
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, -size/2, -size/2, 0, 0, 1, r, g, b);
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, size/2, -size/2, 0, 1, 1, r, g, b);
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, size/2, size/2, 0, 1, 0, r, g, b);
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, -size/2, size/2, 0, 0, 0, r, g, b);

        // Also draw the back side for culling reasons, if needed, though entityCutout should handle it
        // addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, -size/2, size/2, 0, 0, 0, r, g, b);
        // addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, size/2, size/2, 0, 1, 0, r, g, b);
        // addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, size/2, -size/2, 0, 1, 1, r, g, b);
        // addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, -size/2, -size/2, 0, 0, 1, r, g, b);


        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f,
                           int light, float x, float y, float z, float u, float v,
                           int r, int g, int b) {

        // Define the local normal vector (pointing along Z-axis out of the quad plane)
        Vector3f normal = new Vector3f(0.0f, 0.0f, 1.0f);

        // Transform the normal by the rotation matrix
        normal.mul(matrix3f);

        consumer.addVertex(matrix4f, x, y, z)
                .setColor(r, g, b, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                // Pass the transformed normal components to the correct setNormal method
                .setNormal(normal.x(), normal.y(), normal.z());
    }

    @Override
    public ResourceLocation getTextureLocation(SeekerArrowEntity entity) {
        return TEXTURE;
    }
}