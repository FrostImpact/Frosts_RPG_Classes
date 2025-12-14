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
    private static final int MAX_TICKS = 120; // 6 seconds
    private static final int WARNING_THRESHOLD_TICKS = 60; // 3 seconds

    // Keep a static instance to ensure we aren't creating new ones constantly
    private static final MirageShadowstepOverlay INSTANCE = new MirageShadowstepOverlay();

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "shadowstep_meter"),
                INSTANCE
        );
        // Only print this once during startup, not every frame
        System.out.println("RPG Classes: Shadowstep Meter Overlay Registered!");
    }

// ... imports ...

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || mc.level == null) return;

        PlayerRPGData rpg;
        try {
            rpg = mc.player.getData(ModAttachments.PLAYER_RPG);
        } catch (Exception e) { return; }

        if (rpg == null || !rpg.getCurrentClass().equals("MIRAGE")) return;
        if (!rpg.isMirageShadowstepActive()) return;

        int remainingTicks = rpg.getMirageShadowstepTicks();

        // --- SAFEGUARD: Prevent crash/freeze if data is bad ---
        if (remainingTicks < 0) remainingTicks = 0;
        if (remainingTicks > MAX_TICKS) remainingTicks = MAX_TICKS;

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int barX = (screenWidth - BAR_WIDTH) / 2;
        int barY = screenHeight - 68;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 1. Frame
        drawOrnateFrame(graphics, barX, barY, BAR_WIDTH, BAR_HEIGHT);

        // 2. Background
        int innerX = barX + BORDER_WIDTH;
        int innerY = barY + BORDER_WIDTH;
        int innerWidth = BAR_WIDTH - (BORDER_WIDTH * 2);
        int innerHeight = BAR_HEIGHT - (BORDER_WIDTH * 2);

        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, 0xFF0a0a1a);

        // 3. Warning Glow (Fixed Math)
        if (remainingTicks <= WARNING_THRESHOLD_TICKS) {
            // Slower, safer pulse math
            long time = mc.level.getGameTime(); // Use game time instead of system millis for stability
            float pulse = (float) (Math.sin(time * 0.2) * 0.5 + 0.5);
            int glowAlpha = (int) (pulse * 100);

            // Ensure alpha is valid (0-255)
            glowAlpha = Math.max(0, Math.min(255, glowAlpha));

            int warningGlow = (glowAlpha << 24) | 0xFF4400;

            // Draw glow over the background
            graphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, warningGlow);
        }

        // 4. Bar Fill
        float progress = (float) remainingTicks / (float) MAX_TICKS;
        int fillWidth = (int) (innerWidth * progress);

        if (fillWidth > 0) {
            drawGradientBar(graphics, innerX, innerY, fillWidth, innerHeight, 0xFF00d4ff, 0xFF9400ff);

            // Shimmer
            long time = mc.level.getGameTime();
            int shimmerOffset = (int) ((time * 2) % Math.max(1, innerWidth)); // Loop over full width

            // Only draw shimmer if it's inside the filled area
            if (shimmerOffset < fillWidth - 6) {
                graphics.fill(innerX + shimmerOffset, innerY, innerX + shimmerOffset + 6, innerY + innerHeight, 0x4000d4ff);
            }
        }

        // 5. Text & Label (Same as before)
        String timeText = String.format("%.1fs", remainingTicks / 20.0f);
        int textX = barX + (BAR_WIDTH - mc.font.width(timeText)) / 2;
        int textY = barY + (BAR_HEIGHT - mc.font.lineHeight) / 2 + 1;
        graphics.drawString(mc.font, timeText, textX + 1, textY + 1, 0xFF000000, false);
        graphics.drawString(mc.font, timeText, textX, textY, 0xFFccbbff, false);

        graphics.drawString(mc.font, "SHADOWSTEP", barX + 4, barY - 10, 0xFF00d4ff, true);

        RenderSystem.disableBlend();
    }
// ... keep helper methods ...

    private void drawOrnateFrame(GuiGraphics graphics, int x, int y, int width, int height) {
        int outerColor = 0xFF5500aa;
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

        int accentColor = 0xFF00d4ff;
        int highlightColor = (0x60 << 24) | (accentColor & 0x00FFFFFF);
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + width - BORDER_WIDTH, y + BORDER_WIDTH + 1, highlightColor);
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + BORDER_WIDTH + 1, y + height - BORDER_WIDTH, highlightColor);

        int cornerColor = 0xFF9400ff;
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