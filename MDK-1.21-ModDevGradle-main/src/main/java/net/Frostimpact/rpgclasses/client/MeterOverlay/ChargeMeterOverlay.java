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
public class ChargeMeterOverlay implements LayeredDraw.Layer {

    private static final int BAR_WIDTH = 140;
    private static final int BAR_HEIGHT = 8;
    private static final int BORDER_WIDTH = 1;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "charge_meter"),
                new ChargeMeterOverlay()
        );
        System.out.println("RPG Classes: Charge Meter Overlay Registered!");
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        PlayerRPGData rpg = mc.player.getData(ModAttachments.PLAYER_RPG);

        if (!rpg.getCurrentClass().equals("JUGGERNAUT")) return;

        int charge = rpg.getJuggernautCharge();
        int maxCharge = rpg.getJuggernautMaxCharge();
        boolean isShieldMode = rpg.isJuggernautShieldMode();
        boolean isDecaying = rpg.isChargeDecaying();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Above hotbar, centered
        int barX = (screenWidth / 2) - (BAR_WIDTH / 2);
        int barY = screenHeight - 70;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // === ORNATE FRAME ===
        drawOrnateFrame(graphics, barX, barY, BAR_WIDTH, BAR_HEIGHT, isShieldMode, isDecaying);

        // === BAR BACKGROUND ===
        int innerX = barX + BORDER_WIDTH;
        int innerY = barY + BORDER_WIDTH;
        int innerWidth = BAR_WIDTH - (BORDER_WIDTH * 2);
        int innerHeight = BAR_HEIGHT - (BORDER_WIDTH * 2);

        int bgColor = isShieldMode ? 0xFF0a0f1a : 0xFF1a0a0a;
        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + innerHeight, bgColor);
        graphics.fill(innerX, innerY, innerX + innerWidth, innerY + 2, 0x80000000);
        graphics.fill(innerX, innerY, innerX + 2, innerY + innerHeight, 0x60000000);

        // === CHARGE BAR FILL ===
        float fillPercent = (float) charge / (float) maxCharge;
        int fillWidth = (int) (innerWidth * fillPercent);

        if (fillWidth > 0) {
            int barColor1, barColor2;

            if (isShieldMode) {
                barColor1 = 0xFF00ddff; // Bright cyan
                barColor2 = 0xFF0088cc; // Deep cyan
            } else {
                if (charge > 50) {
                    barColor1 = 0xFFff3333; // Bright red
                    barColor2 = 0xFFaa0000; // Dark red
                } else if (charge > 0) {
                    barColor1 = 0xFFff8800; // Bright orange
                    barColor2 = 0xFFcc4400; // Dark orange
                } else {
                    barColor1 = 0xFF444444; // Grey
                    barColor2 = 0xFF222222;
                }
            }

            drawGradientBar(graphics, innerX, innerY, fillWidth, innerHeight, barColor1, barColor2);

            // Glossy highlight
            graphics.fill(innerX, innerY, innerX + fillWidth, innerY + 3, 0x60FFFFFF);
            graphics.fill(innerX, innerY, innerX + 2, innerY + innerHeight, 0x40FFFFFF);

            // Energy pulse effect
            if (!isShieldMode && charge > 0) {
                long time = System.currentTimeMillis() / 100;
                int pulseOffset = (int) (time % fillWidth);
                if (pulseOffset < fillWidth - 4) {
                    graphics.fill(innerX + pulseOffset, innerY, innerX + pulseOffset + 4, innerY + innerHeight, 0x40FF0000);
                }
            }
        }

        // === THRESHOLD MARKER (50%) ===
        int threshold50X = innerX + (innerWidth / 2);
        graphics.fill(threshold50X, innerY, threshold50X + 1, innerY + innerHeight, 0x80FFFFFF);

        // === TEXT ===
        String chargeText = charge + "/" + maxCharge;
        int textWidth = mc.font.width(chargeText);
        int textX = barX + (BAR_WIDTH - textWidth) / 2;
        int textY = barY + (BAR_HEIGHT - mc.font.lineHeight) / 2 + 1;

        graphics.drawString(mc.font, chargeText, textX + 1, textY + 1, 0xFF000000, false);
        int textColor = isShieldMode ? 0xFFccffff : 0xFFffcccc;
        graphics.drawString(mc.font, chargeText, textX, textY, textColor, false);

        // === MODE INDICATOR ===
        String modeText = isShieldMode ? "SHIELD" : "SHATTER";
        int modeColor = isShieldMode ? 0xFF00AAFF : 0xFFFF4444;
        int labelX = barX + 4;
        int labelY = barY - 11;
        graphics.drawString(mc.font, modeText, labelX, labelY, modeColor, true);

        // === DECAY WARNING ===
        if (isDecaying) {
            long pulseTime = System.currentTimeMillis() / 300;
            if (pulseTime % 2 == 0) {
                int glowColor = 0x80FF0000;
                graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY, glowColor);
                graphics.fill(barX - 1, barY + BAR_HEIGHT, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, glowColor);
                graphics.fill(barX - 1, barY, barX, barY + BAR_HEIGHT, glowColor);
                graphics.fill(barX + BAR_WIDTH, barY, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT, glowColor);
            }

            String decayText = "DECAY";
            graphics.drawString(mc.font, decayText, barX + BAR_WIDTH - mc.font.width(decayText) - 4, barY + 4, 0xFFFF0000, true);
        }

        RenderSystem.disableBlend();
    }

    private void drawOrnateFrame(GuiGraphics graphics, int x, int y, int width, int height, boolean isShield, boolean isDecaying) {
        int outerColor = isShield ? 0xFF5599bb : 0xFFbb5555;
        if (isDecaying && System.currentTimeMillis() / 300 % 2 == 0) {
            outerColor = 0xFFff0000;
        }

        graphics.fill(x - 2, y - 2, x + width + 2, y - 1, outerColor);
        graphics.fill(x - 2, y + height + 1, x + width + 2, y + height + 2, outerColor);
        graphics.fill(x - 2, y - 1, x - 1, y + height + 1, outerColor);
        graphics.fill(x + width + 1, y - 1, x + width + 2, y + height + 1, outerColor);

        int frameColor1 = isShield ? 0xFF2d3d5c : 0xFF5c2d2d;
        int frameColor2 = isShield ? 0xFF1a2433 : 0xFF331a1a;

        graphics.fill(x, y, x + width, y + BORDER_WIDTH, frameColor1);
        graphics.fill(x, y + height - BORDER_WIDTH, x + width, y + height, frameColor2);
        graphics.fill(x, y, x + BORDER_WIDTH, y + height, frameColor1);
        graphics.fill(x + width - BORDER_WIDTH, y, x + width, y + height, frameColor2);

        int highlightColor = isShield ? 0x60aaccff : 0x60ffaa88;
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + width - BORDER_WIDTH, y + BORDER_WIDTH + 1, highlightColor);
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH, x + BORDER_WIDTH + 1, y + height - BORDER_WIDTH, highlightColor);

        int cornerColor = isShield ? 0xFF66ccff : 0xFFff6666;
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