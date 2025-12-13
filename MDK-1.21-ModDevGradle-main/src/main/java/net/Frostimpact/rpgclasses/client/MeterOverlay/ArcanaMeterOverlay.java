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
public class ArcanaMeterOverlay implements LayeredDraw.Layer {

    private static final int BAR_WIDTH = 140;
    private static final int BAR_HEIGHT = 8;
    private static final int BORDER_WIDTH = 1;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "arcana_meter"),
                new ArcanaMeterOverlay()
        );
        System.out.println("RPG Classes: Arcana Meter Overlay Registered!");
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
        if (currentClass == null || !currentClass.equals("MANAFORGE")) return;

        int arcana = rpg.getManaforgeArcana();
        int maxArcana = 100;
        boolean isCoalescence = rpg.isCoalescenceActive();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Above hotbar, centered
        int barX = (screenWidth / 2) - (BAR_WIDTH / 2);
        int barY = screenHeight - 70;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // === ORNATE FRAME ===
        drawOrnateFrame(graphics, barX, barY, BAR_WIDTH, BAR_HEIGHT, isCoalescence);

        // === BAR BACKGROUND ===
        int innerX = barX + BORDER_WIDTH;
        int innerY = barY + BORDER_WIDTH;
        int innerWidth = BAR_WIDTH - (BORDER_WIDTH * 2);
        int innerHeight = BAR_HEIGHT - (BORDER_WIDTH * 2);

        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, 0xFF0f0a1a);
        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + 2, 0x80000000);
        graphics.fill(innerX, innerY, innerX + 2, innerY + innerHeight, 0x60000000);

        // === ARCANA BAR FILL ===
        float fillPercent = Math.max(0, Math.min(1, (float) arcana / (float) maxArcana));
        int fillWidth = (int) (innerWidth * fillPercent);

        if (fillWidth > 0) {
            int barColor1, barColor2;

            if (isCoalescence) {
                barColor1 = 0xFFaa00ff; // Bright purple
                barColor2 = 0xFF6600aa; // Deep purple
            } else if (arcana == 100) {
                long pulseTime = System.currentTimeMillis() / 200;
                if (pulseTime % 2 == 0) {
                    barColor1 = 0xFFff00ff; // Magenta
                    barColor2 = 0xFFaa00aa;
                } else {
                    barColor1 = 0xFFaa00ff; // Purple
                    barColor2 = 0xFF6600aa;
                }
            } else if (arcana >= 75) {
                barColor1 = 0xFFaa00ff; // Purple
                barColor2 = 0xFF6600aa;
            } else if (arcana >= 50) {
                barColor1 = 0xFF0099ff; // Cyan
                barColor2 = 0xFF0066cc;
            } else if (arcana >= 25) {
                barColor1 = 0xFF0088dd; // Blue
                barColor2 = 0xFF004488;
            } else {
                barColor1 = 0xFF004488; // Dark blue
                barColor2 = 0xFF002244;
            }

            drawGradientBar(graphics, innerX, innerY, fillWidth, innerHeight, barColor1, barColor2);

            // Glossy highlight
            graphics.fill(innerX, innerY, innerX + fillWidth, innerY + 3, 0x60FFFFFF);
            graphics.fill(innerX, innerY, innerX + 2, innerY + innerHeight, 0x40FFFFFF);

            // Arcane energy shimmer
            long time = System.currentTimeMillis() / 150;
            int shimmerOffset = (int) (time % fillWidth);
            if (shimmerOffset < fillWidth - 6) {
                graphics.fill(innerX + shimmerOffset, innerY, innerX + shimmerOffset + 6, innerY + innerHeight, 0x40FF00FF);
            }
        }

        // === THRESHOLD MARKERS ===
        for (int threshold : new int[]{25, 50, 75}) {
            int markerX = innerX + (innerWidth * threshold / 100);
            graphics.fill(markerX, innerY, markerX + 1, innerY + innerHeight, 0x80FFFFFF);
        }

        // === TEXT ===
        String arcanaText = arcana + "/" + maxArcana;
        int textWidth = mc.font.width(arcanaText);
        int textX = barX + (BAR_WIDTH - textWidth) / 2;
        int textY = barY + (BAR_HEIGHT - mc.font.lineHeight) / 2 + 1;

        graphics.drawString(mc.font, arcanaText, textX + 1, textY + 1, 0xFF000000, false);
        graphics.drawString(mc.font, arcanaText, textX, textY, 0xFFccbbff, false);

        // === LABEL ===
        String labelText = isCoalescence ? "COALESCENCE" : "ARCANA";
        int labelColor = isCoalescence ? 0xFFaa00ff : 0xFF00ddff;
        int labelX = barX + 4;
        int labelY = barY - 11;
        graphics.drawString(mc.font, labelText, labelX, labelY, labelColor, true);

        // === BONUS INDICATOR ===
        if (arcana >= 25 && !isCoalescence) {
            int bonusProjectiles = arcana / 25;
            String bonusText = "+" + bonusProjectiles + " ⚡";
            int bonusColor = arcana == 100 ? 0xFFffff00 : 0xFF00ff00;
            graphics.drawString(mc.font, bonusText, barX + BAR_WIDTH + 4, barY + 4, bonusColor, true);
        }

        // === COALESCENCE STORED DAMAGE ===
        if (isCoalescence) {
            float storedDamage = rpg.getCoalescenceStoredDamage();
            if (storedDamage > 0) {
                String damageText = "§5✦ " + String.format("%.0f", storedDamage) + " DMG";
                graphics.drawString(mc.font, damageText, barX, barY + BAR_HEIGHT + 3, 0xFFaa00ff, true);
            }
        }

        // === MAX ARCANA GLOW ===
        if (arcana == 100 && !isCoalescence) {
            long glowTime = System.currentTimeMillis() / 300;
            if (glowTime % 2 == 0) {
                int glowColor = 0x60FF00FF;
                graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY, glowColor);
                graphics.fill(barX - 1, barY + BAR_HEIGHT, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, glowColor);
                graphics.fill(barX - 1, barY, barX, barY + BAR_HEIGHT, glowColor);
                graphics.fill(barX + BAR_WIDTH, barY, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT, glowColor);
            }
        }

        RenderSystem.disableBlend();
    }

    private void drawOrnateFrame(GuiGraphics graphics, int x, int y, int width, int height, boolean isSpecial) {
        int outerColor = isSpecial ? 0xFFaa55ff : 0xFF5555aa;
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

        int highlightColor = 0x60aaccff;
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + width - BORDER_WIDTH, y + BORDER_WIDTH + 1, highlightColor);
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + BORDER_WIDTH + 1, y + height - BORDER_WIDTH, highlightColor);

        int cornerColor = isSpecial ? 0xFFcc66ff : 0xFF6666cc;
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