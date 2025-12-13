package net.Frostimpact.rpgclasses.client.MeterOverlay;

import com.mojang.blaze3d.systems.RenderSystem;
import net.Frostimpact.rpgclasses.RpgClassesMod;
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
public class HealthBarOverlay implements LayeredDraw.Layer {

    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 7;
    private static final int BORDER_WIDTH = 1;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.FOOD_LEVEL,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "health_bar"),
                new HealthBarOverlay()
        );
        System.out.println("RPG Classes: Health Bar Overlay Registered!");
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        float health = mc.player.getHealth();
        float maxHealth = mc.player.getMaxHealth();
        float healthPercent = Math.max(0, Math.min(1, health / maxHealth));

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Bottom-left area
        int barX = screenWidth / 2 - BAR_WIDTH - 5;
        int barY = screenHeight - 50;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // === ORNATE FRAME ===
        drawOrnateFrame(graphics, barX, barY, BAR_WIDTH, BAR_HEIGHT);

        // === BAR BACKGROUND (Dark recessed area) ===
        int innerX = barX + BORDER_WIDTH;
        int innerY = barY + BORDER_WIDTH;
        int innerWidth = BAR_WIDTH - (BORDER_WIDTH * 2);
        int innerHeight = BAR_HEIGHT - (BORDER_WIDTH * 2);

        // Dark background with slight gradient
        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, 0xFF1a0f0f);
        
        // Inner shadow (top)
        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + 2, 0x80000000);
        
        // Inner shadow (left)
        graphics.fill(innerX, innerY, innerX + 2, innerY + innerHeight, 0x60000000);

        // === HEALTH BAR FILL (with gradient) ===
        int fillWidth = (int) (innerWidth * healthPercent);
        
        if (fillWidth > 0) {
            // Determine bar color based on health percentage
            int barColor1, barColor2;
            if (healthPercent > 0.6f) {
                // High health - Green
                barColor1 = 0xFF2eb82e; // Bright green
                barColor2 = 0xFF1a8a1a; // Dark green
            } else if (healthPercent > 0.3f) {
                // Medium health - Yellow/Orange
                barColor1 = 0xFFe6b800; // Bright yellow
                barColor2 = 0xFFb38f00; // Dark yellow
            } else {
                // Low health - Red
                barColor1 = 0xFFcc0000; // Bright red
                barColor2 = 0xFF800000; // Dark red
            }

            // Draw gradient fill
            drawGradientBar(graphics, innerX, innerY, fillWidth, innerHeight, barColor1, barColor2);

            // Glossy highlight on top
            int highlightColor = (barColor1 & 0x00FFFFFF) | 0x60FFFFFF;
            graphics.fill(innerX, innerY, innerX + fillWidth, innerY + 4, highlightColor);

            // Bright edge on left
            graphics.fill(innerX, innerY, innerX + 2, innerY + innerHeight, 0x40FFFFFF);
        }

        // === NOTCHES (visual segments) ===
        int notchCount = 8;
        for (int i = 1; i < notchCount; i++) {
            int notchX = innerX + (innerWidth * i / notchCount);
            graphics.fill(notchX, innerY, notchX + 1, innerY + innerHeight, 0x40000000);
        }

        // === HEALTH TEXT ===
        String healthText = String.format("%.0f / %.0f", health, maxHealth);
        int textWidth = mc.font.width(healthText);
        int textX = barX + (BAR_WIDTH - textWidth) / 2;
        int textY = barY + (BAR_HEIGHT - mc.font.lineHeight) / 2;

        // Text shadow (for depth)
        graphics.drawString(mc.font, healthText, textX + 1, textY + 1, 0xFF000000, false);
        
        // Main text (white with slight gold tint)
        graphics.drawString(mc.font, healthText, textX, textY, 0xFFFFFFFF, false);

        // === LABEL ===
        //String label = "HEALTH";
        //int labelX = barX + 4;
        //int labelY = barY - 12;
        //graphics.drawString(mc.font, label, labelX, labelY, 0xFFcc3333, true);

        // === LOW HEALTH PULSE ===
        if (healthPercent < 0.25f) {
            long time = System.currentTimeMillis() / 200;
            if (time % 2 == 0) {
                // Pulsing red glow
                graphics.fill(barX, barY, barX + BAR_WIDTH, barY + 2, 0xFFff0000);
                graphics.fill(barX, barY + BAR_HEIGHT - 2, barX + BAR_WIDTH - 1, barY + BAR_HEIGHT, 0xFFff0000);
                graphics.fill(barX, barY, barX + 2, barY + BAR_HEIGHT - 1, 0xFFff0000);
                graphics.fill(barX + BAR_WIDTH - 2, barY, barX + BAR_WIDTH - 1, barY + BAR_HEIGHT, 0xFFff0000);
            }
        }

        RenderSystem.disableBlend();
    }

    private void drawOrnateFrame(GuiGraphics graphics, int x, int y, int width, int height) {
        // Outer border (bright decorative edge)
        int outerColor = 0xFF8b7355; // Bronze/brown
        graphics.fill(x - 2, y - 2, x + width + 2, y - 1, outerColor);
        graphics.fill(x - 2, y + height + 1, x + width + 2, y + height + 2, outerColor);
        graphics.fill(x - 2, y - 1, x - 1, y + height + 1, outerColor);
        graphics.fill(x + width + 1, y - 1, x + width + 2, y + height + 1, outerColor);

        // Main frame
        int frameColor1 = 0xFF5c4033; // Dark brown
        int frameColor2 = 0xFF3d2817; // Darker brown
        
        // Top and bottom
        graphics.fill(x, y, x + width, y + BORDER_WIDTH, frameColor1);
        graphics.fill(x, y + height - BORDER_WIDTH, x + width, y + height, frameColor2);
        
        // Left and right
        graphics.fill(x, y, x + BORDER_WIDTH, y + height, frameColor1);
        graphics.fill(x + width - BORDER_WIDTH, y, x + width, y + height, frameColor2);

        // Inner highlight (beveled edge)
        int highlightColor = 0x60FFFFFF;
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + width - BORDER_WIDTH, y + BORDER_WIDTH + 1, highlightColor);
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + BORDER_WIDTH + 1, y + height - BORDER_WIDTH, highlightColor);

        // Corner decorations
        int cornerColor = 0xFFd4af37; // Gold
        // Top-left corner
        graphics.fill(x - 1, y - 1, x + 1, y, cornerColor);
        graphics.fill(x - 1, y, x, y + 1, cornerColor);
        
        // Top-right corner
        graphics.fill(x + width - 1, y - 1, x + width + 1, y, cornerColor);
        graphics.fill(x + width, y, x + width + 1, y + 1, cornerColor);
        
        // Bottom-left corner
        graphics.fill(x - 1, y + height, x + 1, y + height + 1, cornerColor);
        graphics.fill(x - 1, y + height - 1, x, y + height, cornerColor);
        
        // Bottom-right corner
        graphics.fill(x + width - 1, y + height, x + width + 1, y + height + 1, cornerColor);
        graphics.fill(x + width, y + height - 1, x + width + 1, y + height, cornerColor);
    }

    private void drawGradientBar(GuiGraphics graphics, int x, int y, int width, int height, int color1, int color2) {
        // Simple vertical gradient by drawing horizontal lines
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