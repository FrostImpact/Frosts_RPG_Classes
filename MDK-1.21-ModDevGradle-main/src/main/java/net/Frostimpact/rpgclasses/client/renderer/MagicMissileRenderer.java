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
import net.minecraft.client.renderer.texture.OverlayTexture;
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

        // Draw a simple quad (4 vertices forming 2 triangles)
        // Note: We're using the full vertex format now with all required components

        // Vertex order: Bottom-left, Bottom-right, Top-right, Top-left
        // This creates a quad facing the camera

        // Bottom-left
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, -size, -size, 0, 0, 1);
        // Bottom-right
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, size, -size, 0, 1, 1);
        // Top-right
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, size, size, 0, 1, 0);
        // Top-left
        addVertex(vertexConsumer, matrix4f, matrix3f, packedLight, -size, size, 0, 0, 0);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix4f, Matrix3f matrix3f,
                           int light, float x, float y, float z, float u, float v) {

        // 1. Define the normal vector for the quad in local space (e.g., facing Z-axis)
        // For a billboard, this normal is somewhat arbitrary, but let's use the standard
        // direction that will face the camera after the PoseStack rotations: (0, 0, 1) in local space
        // Since the quad is drawn in the XY plane, its normal is technically Z.
        // However, for this simple quad, (0, 0, 1) is a safe bet for a local normal.

        // We can use the JOML Matrix3f to transform a vector.
        org.joml.Vector3f normal = new org.joml.Vector3f(0.0f, 0.0f, 1.0f); // Local Normal Z-axis
        normal.mul(matrix3f); // Transform the normal by the world rotation matrix

        consumer.addVertex(matrix4f, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                // 2. Pass the resulting transformed components to the simple setNormal
                .setNormal(normal.x(), normal.y(), normal.z());

        // NOTE: If you are using an older version or a specific library's VertexConsumer,
        // the original line might be correct, but based on your error, this transformation
        // and subsequent use of the 3-float setNormal is the standard fix for modern MC.
    }

    @Override
    public ResourceLocation getTextureLocation(MagicMissileEntity entity) {
        return TEXTURE;
    }
}