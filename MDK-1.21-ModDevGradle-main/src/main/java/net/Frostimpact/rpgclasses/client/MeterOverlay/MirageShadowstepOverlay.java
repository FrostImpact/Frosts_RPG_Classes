package net.Frostimpact.rpgclasses.client.MeterOverlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class MirageShadowstepOverlay implements LayeredDraw.Layer {

    private static final int BAR_WIDTH = 140;
    private static final int BAR_HEIGHT = 8;
    private static final int BORDER_WIDTH = 1;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "shadowstep_meter"),
                new MirageShadowstepOverlay()
        );
        System.out.println("RPG Classes: Shadowstep Meter Overlay Registered!");
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui || mc.level == null) return;

        PlayerRPGData rpg;
        try {
            rpg = mc.player.getData(ModAttachments.PLAYER_RPG);
            if (rpg == null) return;
        } catch (Exception e) {
            return;
        }

        String currentClass = rpg.getCurrentClass();
        if (currentClass == null || !currentClass.equals("MIRAGE")) return;

        // Only display when shadowstep reactivation window is active
        if (!rpg.isMirageShadowstepActive()) return;

        int remainingTicks = rpg.getMirageShadowstepTicks();
        int maxTicks = 120; // 6 seconds
        
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Above hotbar, centered
        int barX = (screenWidth / 2) - (BAR_WIDTH / 2);
        int barY = screenHeight - 70;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // === ORNATE FRAME ===
        drawOrnateFrame(graphics, barX, barY, BAR_WIDTH, BAR_HEIGHT);

        // === BAR BACKGROUND ===
        int innerX = barX + BORDER_WIDTH;
        int innerY = barY + BORDER_WIDTH;
        int innerWidth = BAR_WIDTH - (BORDER_WIDTH * 2);
        int innerHeight = BAR_HEIGHT - (BORDER_WIDTH * 2);

        int bgColor = 0xFF0a0a1a; // Dark purple/blue background
        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, bgColor);
        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + 2, 0x80000000);
        graphics.fill(innerX, innerY, innerX + 2, innerY + innerHeight, 0x60000000);

        // === WARNING GLOW (Last 3 seconds) ===
        if (remainingTicks <= 60) { // Last 3 seconds
            long time = System.currentTimeMillis();
            float pulse = (float) (Math.sin(time / 100.0) * 0.5 + 0.5);
            int glowAlpha = (int) (pulse * 100);
            int warningGlow = (glowAlpha << 24) | 0xFF4400;
            graphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, warningGlow);
        }

        // === SHADOWSTEP BAR FILL ===
        float progress = (float) remainingTicks / (float) maxTicks;
        int fillWidth = (int) (innerWidth * progress);

        if (fillWidth > 0) {
            int topColor = 0xFF00d4ff; // Bright cyan
            int bottomColor = 0xFF9400ff; // Purple

            drawGradientBar(graphics, innerX, innerY, fillWidth, innerHeight, topColor, bottomColor);

            // Glossy highlight
            graphics.fill(innerX, innerY, innerX + fillWidth, innerY + 3, 0x60FFFFFF);
            graphics.fill(innerX, innerY, innerX + 2, innerY + innerHeight, 0x40FFFFFF);

            // Shadow energy shimmer
            long time = System.currentTimeMillis() / 150;
            int shimmerOffset = (int) (time % fillWidth);
            if (shimmerOffset < fillWidth - 6) {
                graphics.fill(innerX + shimmerOffset, innerY, innerX + shimmerOffset + 6, innerY + innerHeight, 0x4000d4ff);
            }
        }

        // === TIME TEXT ===
        float secondsLeft = remainingTicks / 20.0f;
        String timeText = String.format("%.1fs", secondsLeft);
        int textWidth = mc.font.width(timeText);
        int textX = barX + (BAR_WIDTH - textWidth) / 2;
        int textY = barY + (BAR_HEIGHT - mc.font.lineHeight) / 2 + 1;

        graphics.drawString(mc.font, timeText, textX + 1, textY + 1, 0xFF000000, false);
        graphics.drawString(mc.font, timeText, textX, textY, 0xFFccbbff, false);

        // === LABEL ===
        String labelText = "SHADOWSTEP";
        int labelColor = 0xFF00d4ff; // Cyan
        int labelX = barX + 4;
        int labelY = barY - 11;
        graphics.drawString(mc.font, labelText, labelX, labelY, labelColor, true);

        // === WARNING GLOW FRAME (Last 3 seconds) ===
        if (remainingTicks <= 60) {
            long pulseTime = System.currentTimeMillis() / 300;
            if (pulseTime % 2 == 0) {
                int glowColor = 0x80FF4400;
                graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY, glowColor);
                graphics.fill(barX - 1, barY + BAR_HEIGHT, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, glowColor);
                graphics.fill(barX - 1, barY, barX, barY + BAR_HEIGHT, glowColor);
                graphics.fill(barX + BAR_WIDTH, barY, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT, glowColor);
            }
        }

        RenderSystem.disableBlend();
    }

    private void drawOrnateFrame(GuiGraphics graphics, int x, int y, int width, int height) {
        int outerColor = 0xFF5500aa; // Deep purple border
        graphics.fill(x - 2, y - 2, x + width + 2, y - 1, outerColor);
        graphics.fill(x - 2, y + height + 1, x + width + 2, y + height + 2, outerColor);
        graphics.fill(x - 2, y - 1, x - 1, y + height + 1, outerColor);
        graphics.fill(x + width + 1, y - 1, x + width + 2, y + height + 1, outerColor);

        int frameColor1 = 0xFF2d2d5c;
        int frameColor2 = 0xFF1a1a33;

        graphics.fill(x, y, x + width, y + BORDER_WIDTH, frameColor1);
        graphics.fill(x, y + height - BORDER_WIDTH, x + width, y + height, frameColor2);
        graphics.fill(x, y, x + BORDER_WIDTH, y + height, frameColor1);
        graphics.fill(x + width - BORDER_WIDTH, y, x + width, y + height, frameColor2);

        int accentColor = 0xFF00d4ff; // Cyan accents
        int highlightColor = (0x60 << 24) | (accentColor & 0x00FFFFFF);
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + width - BORDER_WIDTH, y + BORDER_WIDTH + 1, highlightColor);
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + BORDER_WIDTH + 1, y + height - BORDER_WIDTH, highlightColor);

        int cornerColor = 0xFF9400ff; // Purple corners
        graphics.fill(x - 1, y - 1, x + 1, y, cornerColor);
        graphics.fill(x - 1, y, x, y + 1, cornerColor);
        graphics.fill(x + width - 1, y - 1, x + width + 1, y, cornerColor);
        graphics.fill(x + width, y, x + width + 1, y + 1, cornerColor);
        graphics.fill(x - 1, y + height, x + 1, y + height + 1, cornerColor);
        graphics.fill(x - 1, y + height - 1, x, y + height, cornerColor);
        graphics.fill(x + width - 1, y + height, x + width + 1, y + height + 1, cornerColor);
        graphics.fill(x + width, y + height - 1, x + width + 1, y + height, cornerColor);
    }

    private void drawGradientBar(GuiGraphics graphics, int x, int y, int width, int height, int color1, int color2) {
        for (int dy = 0; dy < height; dy++) {
            float ratio = (float) dy / height;
            int r = (int) (((color1 >> 16) & 0xFF) * (1 - ratio) + ((color2 >> 16) & 0xFF) * ratio);
            int g = (int) (((color1 >> 8) & 0xFF) * (1 - ratio) + ((color2 >> 8) & 0xFF) * ratio);
            int b = (int) ((color1 & 0xFF) * (1 - ratio) + (color2 & 0xFF) * ratio);
            int color = 0xFF000000 | (r << 16) | (g << 8) | b;

            graphics.fill(x, y + dy, x + width, y + dy + 1, color);
        }
    }
}
