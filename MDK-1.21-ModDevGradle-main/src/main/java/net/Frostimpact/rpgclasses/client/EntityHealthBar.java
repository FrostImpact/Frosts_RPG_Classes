package net.Frostimpact.rpgclasses.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis; // Needed for the rotation fix
import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import org.joml.Matrix4f;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, value = Dist.CLIENT)
public class EntityHealthBar {

    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player || (mc.player != null && mc.player.distanceTo(entity) > 32.0)) {
            return;
        }

        renderHealthBar(event.getPoseStack(), event.getMultiBufferSource(), entity, event.getPackedLight());
    }

    private static void renderHealthBar(PoseStack poseStack, MultiBufferSource buffer, LivingEntity entity, int light) {
        poseStack.pushPose();

        // 1. Move UP
        poseStack.translate(0.0f, 2.3f, 0.0f);

        // 2. ROTATE 180 DEGREES (The Fix)
        // This flips the bar around so it faces the front (the player) instead of the back.
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));

        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float healthPercent = Math.max(0.0f, Math.min(1.0f, health / maxHealth));

        // Dimensions (Small size)
        float barWidth = 2f;
        float barHeight = 0.125f;
        float startX = -barWidth / 2.0f;
        float startY = 0.0f;

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer vc = buffer.getBuffer(RenderType.gui());

        // 3. Draw Background (Black)
        // Z = 0.0f
        drawQuad(vc, matrix, (float) (startX - 0.03), (float) (startY - 0.03), (float) (barWidth + 0.06), (float) (barHeight + 0.06), 0.0f, 0, 0, 0, 200, light);

        // 4. Determine Color
        int r = 255, g = 50, b = 50;
        if (healthPercent > 0.66f) { r = 50; g = 255; b = 50; }
        else if (healthPercent > 0.33f) { r = 255; g = 255; b = 0; }

        // 5. Draw Foreground (Health)
        // Z = 0.01f (Prevents clipping)
        drawQuad(vc, matrix, startX, startY, barWidth * healthPercent, barHeight, -0.01f, r, g, b, 255, light);

        poseStack.popPose();
    }

    private static void drawQuad(VertexConsumer vc, Matrix4f matrix, float x, float y, float w, float h, float z, int r, int g, int b, int a, int light) {
        // Bottom Left
        vc.addVertex(matrix, x, y + h, z).setColor(r, g, b, a).setLight(light);
        // Bottom Right
        vc.addVertex(matrix, x + w, y + h, z).setColor(r, g, b, a).setLight(light);
        // Top Right
        vc.addVertex(matrix, x + w, y, z).setColor(r, g, b, a).setLight(light);
        // Top Left
        vc.addVertex(matrix, x, y, z).setColor(r, g, b, a).setLight(light);
    }
}