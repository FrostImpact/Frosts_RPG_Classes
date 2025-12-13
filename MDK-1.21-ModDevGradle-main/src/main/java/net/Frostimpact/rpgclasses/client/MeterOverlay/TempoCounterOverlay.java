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
public class TempoCounterOverlay implements LayeredDraw.Layer {

    private static final int STACK_SIZE = 10;
    private static final int STACK_SPACING = 18;
    private static final int FRAME_WIDTH = 1;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "tempo_counter"),
                new TempoCounterOverlay()
        );
        System.out.println("RPG Classes: TEMPO Counter Overlay Registered!");
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        PlayerRPGData rpg = mc.player.getData(ModAttachments.PLAYER_RPG);

        if (!rpg.getCurrentClass().equals("BLADEDANCER")) return;

        int tempoStacks = rpg.getTempoStacks();
        boolean isActive = rpg.isTempoActive();
        boolean isFinalWaltz = rpg.isFinalWaltzActive();

        if (tempoStacks == 0 && !isActive) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int displayStacks = isFinalWaltz ? tempoStacks : Math.min(tempoStacks, 4);
        int totalWidth = (displayStacks * STACK_SPACING) - (STACK_SPACING - STACK_SIZE);

        int baseX = (screenWidth / 2) - (totalWidth / 2);
        int baseY = screenHeight - 70;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // === LABEL ===
        String labelColor = isFinalWaltz ? "§5" : "§6";
        String label = labelColor + "TEMPO";
        int labelWidth = mc.font.width("TEMPO");
        int labelX = baseX + (totalWidth / 2) - (labelWidth / 2);
        int labelY = baseY - 12;
        graphics.drawString(mc.font, label, labelX, labelY, 0xFFFFFFFF, true);

        // === DRAW STACKS ===
        for (int i = 0; i < displayStacks; i++) {
            int stackX = baseX + (i * STACK_SPACING);
            int stackY = baseY;

            int color;
            if (isFinalWaltz) {
                if (i < 3) {
                    color = 0xFFFFAA00; // Orange
                } else if (i < 6) {
                    color = 0xFFAA00FF; // Purple
                } else {
                    color = 0xFFFF00FF; // Magenta
                }
            } else {
                if (i < 3) {
                    color = 0xFFFFAA00; // Orange
                } else {
                    color = 0xFFFF0000; // Red
                }
            }

            boolean shouldGlow = isActive && i < 3;
            drawOrnateStack(graphics, stackX, stackY, STACK_SIZE, color, shouldGlow);
        }

        // === STRENGTH LEVEL INDICATOR ===
        if (isActive) {
            String strengthText;
            int strengthColor;

            if (isFinalWaltz && tempoStacks >= 6) {
                strengthText = "II"; // Strength II
                strengthColor = 0xFFFF00FF;
            } else {
                strengthText = "I"; // Strength I
                strengthColor = 0xFFFFAA00;
            }

            int textX = baseX + totalWidth + 6;
            int textY = baseY + (STACK_SIZE / 2) - (mc.font.lineHeight / 2);

            // Ornate strength display
            graphics.fill(textX - 2, textY - 2, textX + mc.font.width(strengthText) + 2, textY + mc.font.lineHeight + 2, 0xFF2d2817);
            graphics.fill(textX - 3, textY - 3, textX + mc.font.width(strengthText) + 3, textY - 2, 0xFF8b7355);
            graphics.fill(textX - 3, textY + mc.font.lineHeight + 2, textX + mc.font.width(strengthText) + 3, textY + mc.font.lineHeight + 3, 0xFF8b7355);
            graphics.fill(textX - 3, textY - 2, textX - 2, textY + mc.font.lineHeight + 2, 0xFF8b7355);
            graphics.fill(textX + mc.font.width(strengthText) + 2, textY - 2, textX + mc.font.width(strengthText) + 3, textY + mc.font.lineHeight + 2, 0xFF8b7355);

            graphics.drawString(mc.font, strengthText, textX, textY, strengthColor, false);
        }

        // === OVERFLOW COUNTER (Final Waltz only) ===
        if (isFinalWaltz && tempoStacks > 6) {
            int overflow = rpg.getFinalWaltzOverflow();
            String overflowText = "+" + overflow;
            int overflowX = baseX + totalWidth + (isActive ? 24 : 6);
            int overflowY = baseY + (STACK_SIZE / 2) - (mc.font.lineHeight / 2);

            graphics.fill(overflowX - 2, overflowY - 2, overflowX + mc.font.width(overflowText) + 2, overflowY + mc.font.lineHeight + 2, 0xFF2d175c);
            graphics.drawString(mc.font, overflowText, overflowX, overflowY, 0xFFFF00FF, true);
        }

        RenderSystem.disableBlend();
    }

    private void drawOrnateStack(GuiGraphics graphics, int x, int y, int size, int color, boolean glow) {
        // Glow effect
        if (glow) {
            long time = System.currentTimeMillis() / 200;
            if (time % 2 == 0) {
                int glowColor = (color & 0x00FFFFFF) | 0x60000000;
                graphics.fill(x - 3, y - 3, x + size + 3, y + size + 3, glowColor);
            }
        }

        // Outer decorative frame
        int outerColor = darkenColor(color, 0.4f);
        graphics.fill(x - 2, y - 2, x + size + 2, y - 1, outerColor);
        graphics.fill(x - 2, y + size + 1, x + size + 2, y + size + 2, outerColor);
        graphics.fill(x - 2, y - 1, x - 1, y + size + 1, outerColor);
        graphics.fill(x + size + 1, y - 1, x + size + 2, y + size + 1, outerColor);

        // Main frame
        int frameColor = darkenColor(color, 0.6f);
        graphics.fill(x - 1, y - 1, x + size + 1, y, frameColor);
        graphics.fill(x - 1, y + size, x + size + 1, y + size + 1, frameColor);
        graphics.fill(x - 1, y, x, y + size, frameColor);
        graphics.fill(x + size, y, x + size + 1, y + size, frameColor);

        // Diamond shape (filled square rotated)
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int halfSize = size / 2 - 1;

        for (int dy = -halfSize; dy <= halfSize; dy++) {
            int width = halfSize - Math.abs(dy);
            int lineY = centerY + dy;

            // Gradient from top to bottom
            float gradientRatio = (dy + halfSize) / (float)(halfSize * 2);
            int gradientColor = interpolateColor(color, darkenColor(color, 0.4f), gradientRatio);

            graphics.fill(centerX - width, lineY, centerX + width + 1, lineY + 1, gradientColor);
        }

        // Highlight on top half
        for (int dy = -halfSize; dy <= 0; dy++) {
            int width = halfSize - Math.abs(dy);
            int lineY = centerY + dy;
            int highlightColor = 0x40FFFFFF;
            graphics.fill(centerX - width, lineY, centerX + width + 1, lineY + 1, highlightColor);
        }

        // Corner decorations
        int cornerColor = lightenColor(color, 1.2f);
        graphics.fill(x - 1, y - 1, x, y, cornerColor);
        graphics.fill(x + size, y - 1, x + size + 1, y, cornerColor);
        graphics.fill(x - 1, y + size, x, y + size + 1, cornerColor);
        graphics.fill(x + size, y + size, x + size + 1, y + size + 1, cornerColor);
    }

    private int darkenColor(int color, float factor) {
        int r = (int)(((color >> 16) & 0xFF) * factor);
        int g = (int)(((color >> 8) & 0xFF) * factor);
        int b = (int)((color & 0xFF) * factor);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int lightenColor(int color, float factor) {
        int r = Math.min(255, (int)(((color >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int)(((color >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int)((color & 0xFF) * factor));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int interpolateColor(int color1, int color2, float ratio) {
        int r = (int)(((color1 >> 16) & 0xFF) * (1 - ratio) + ((color2 >> 16) & 0xFF) * ratio);
        int g = (int)(((color1 >> 8) & 0xFF) * (1 - ratio) + ((color2 >> 8) & 0xFF) * ratio);
        int b = (int)((color1 & 0xFF) * (1 - ratio) + (color2 & 0xFF) * ratio);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}