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
public class ManaBarOverlay implements LayeredDraw.Layer {

    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 7;
    private static final int BORDER_WIDTH = 1;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.FOOD_LEVEL,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "mana_bar"),
                new ManaBarOverlay()
        );
        System.out.println("RPG Classes: Mana Bar Overlay Registered!");
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        PlayerRPGData rpg = mc.player.getData(ModAttachments.PLAYER_RPG);
        
        int mana = rpg.getMana();
        int maxMana = rpg.getMaxMana();
        float manaPercent = Math.max(0, Math.min(1, (float) mana / maxMana));

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Bottom-right area
        int barX = screenWidth / 2 + 5;
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
        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, 0xFF0f0f1a);
        
        // Inner shadow (top)
        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + 2, 0x80000000);
        
        // Inner shadow (left)
        graphics.fill(innerX, innerY, innerX + 2, innerY + innerHeight, 0x60000000);

        // === MANA BAR FILL (with gradient) ===
        int fillWidth = (int) (innerWidth * manaPercent);
        
        if (fillWidth > 0) {
            // Blue/cyan gradient for mana
            int barColor1 = 0xFF00bfff; // Bright cyan
            int barColor2 = 0xFF0066cc; // Deep blue

            // Draw gradient fill
            drawGradientBar(graphics, innerX, innerY, fillWidth, innerHeight, barColor1, barColor2);

            // Glossy highlight on top
            int highlightColor = 0x60FFFFFF;
            graphics.fill(innerX, innerY, innerX + fillWidth, innerY + 4, highlightColor);

            // Bright edge on left
            graphics.fill(innerX, innerY, innerX + 2, innerY + innerHeight, 0x40FFFFFF);

            // Magic shimmer effect
            long time = System.currentTimeMillis() / 100;
            int shimmerOffset = (int) (time % fillWidth);
            if (shimmerOffset < fillWidth - 4) {
                graphics.fill(innerX + shimmerOffset, innerY, innerX + shimmerOffset + 4, innerY + innerHeight, 0x30FFFFFF);
            }
        }

        // === NOTCHES (visual segments) ===
        int notchCount = 8;
        for (int i = 1; i < notchCount; i++) {
            int notchX = innerX + (innerWidth * i / notchCount);
            graphics.fill(notchX, innerY, notchX + 1, innerY + innerHeight, 0x40000000);
        }

        // === MANA TEXT ===
        String manaText = String.format("%d / %d", mana, maxMana);
        int textWidth = mc.font.width(manaText);
        int textX = barX + (BAR_WIDTH - textWidth) / 2;
        int textY = barY + (BAR_HEIGHT - mc.font.lineHeight) / 2;

        // Text shadow (for depth)
        graphics.drawString(mc.font, manaText, textX + 1, textY + 1, 0xFF000000, false);
        
        // Main text (white with slight cyan tint)
        graphics.drawString(mc.font, manaText, textX, textY, 0xFFccffff, false);

        // === LABEL ===
        //String label = "MANA";
        //int labelWidth = mc.font.width(label);
        //int labelX = barX + BAR_WIDTH - labelWidth - 4;
        //int labelY = barY - 12;
        //graphics.drawString(mc.font, label, labelX, labelY, 0xFF3399ff, true);

        // === LOW MANA PULSE ===
        if (manaPercent < 0.15f) {
            long time = System.currentTimeMillis() / 250;
            if (time % 2 == 0) {
                // Pulsing blue glow
                graphics.fill(barX, barY, barX + BAR_WIDTH, barY + 2, 0xFF0099ff);
                graphics.fill(barX, barY + BAR_HEIGHT - 2, barX + BAR_WIDTH - 1, barY + BAR_HEIGHT, 0xFF0099ff);
                graphics.fill(barX, barY, barX + 2, barY + BAR_HEIGHT - 1, 0xFF0099ff);
                graphics.fill(barX + BAR_WIDTH - 2, barY, barX + BAR_WIDTH - 1, barY + BAR_HEIGHT, 0xFF0099ff);
            }
        }

        RenderSystem.disableBlend();
    }

    private void drawOrnateFrame(GuiGraphics graphics, int x, int y, int width, int height) {
        // Outer border (bright decorative edge)
        int outerColor = 0xFF5577aa; // Blue-silver
        graphics.fill(x - 2, y - 2, x + width + 2, y - 1, outerColor);
        graphics.fill(x - 2, y + height + 1, x + width + 2, y + height + 2, outerColor);
        graphics.fill(x - 2, y - 1, x - 1, y + height + 1, outerColor);
        graphics.fill(x + width + 1, y - 1, x + width + 2, y + height + 1, outerColor);

        // Main frame
        int frameColor1 = 0xFF2d3d5c; // Dark blue-grey
        int frameColor2 = 0xFF1a2433; // Darker blue-grey
        
        // Top and bottom
        graphics.fill(x, y, x + width, y + BORDER_WIDTH, frameColor1);
        graphics.fill(x, y + height - BORDER_WIDTH, x + width, y + height, frameColor2);
        
        // Left and right
        graphics.fill(x, y, x + BORDER_WIDTH, y + height, frameColor1);
        graphics.fill(x + width - BORDER_WIDTH, y, x + width, y + height, frameColor2);

        // Inner highlight (beveled edge)
        int highlightColor = 0x60aaccff;
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + width - BORDER_WIDTH, y + BORDER_WIDTH + 1, highlightColor);
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + BORDER_WIDTH + 1, y + height - BORDER_WIDTH, highlightColor);

        // Corner decorations
        int cornerColor = 0xFF66ccff; // Bright cyan
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