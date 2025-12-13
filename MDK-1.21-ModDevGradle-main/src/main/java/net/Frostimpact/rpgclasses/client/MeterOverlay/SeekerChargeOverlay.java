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
public class SeekerChargeOverlay implements LayeredDraw.Layer {

    private static final int CHARGE_SIZE = 10;
    private static final int CHARGE_SPACING = 20;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "seeker_charge"),
                new SeekerChargeOverlay()
        );
        System.out.println("RPG Classes: SEEKER Charge Overlay Registered!");
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();

        // Add safety checks
        if (mc.player == null || mc.options.hideGui || mc.level == null) return;

        PlayerRPGData rpg;
        try {
            rpg = mc.player.getData(ModAttachments.PLAYER_RPG);
            if (rpg == null) return;
        } catch (Exception e) {
            return;
        }

        // Check class
        String currentClass = rpg.getCurrentClass();
        if (currentClass == null || !currentClass.equals("MARKSMAN")) return;

        int charges = rpg.getMarksmanSeekerCharges();
        int maxCharges = 5;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int totalWidth = (maxCharges * CHARGE_SPACING) - (CHARGE_SPACING - CHARGE_SIZE);
        int baseX = (screenWidth / 2) - (totalWidth / 2);
        int baseY = screenHeight - 70;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // === LABEL ===
        String label = "§aSEEKER";
        int labelWidth = mc.font.width("SEEKER");
        int labelX = baseX + (totalWidth / 2) - (labelWidth / 2);
        int labelY = baseY - 12;
        graphics.drawString(mc.font, label, labelX, labelY, 0xFF00ff00, true);

        // === DRAW CHARGES ===
        for (int i = 0; i < maxCharges; i++) {
            int chargeX = baseX + (i * CHARGE_SPACING);
            int chargeY = baseY;

            boolean filled = i < charges;
            drawOrnateCharge(graphics, chargeX, chargeY, CHARGE_SIZE, filled);
        }

        RenderSystem.disableBlend();
    }

    private void drawOrnateCharge(GuiGraphics graphics, int x, int y, int size, boolean filled) {
        if (filled) {
            long time = System.currentTimeMillis() / 200;
            if (time % 2 == 0) {
                graphics.fill(x - 3, y - 3, x + size + 3, y + size + 3, 0x4000FF00);
            }
        }

        int outerColor = filled ? 0xFF00AA00 : 0xFF003300;
        graphics.fill(x - 2, y - 2, x + size + 2, y - 1, outerColor);
        graphics.fill(x - 2, y + size + 1, x + size + 2, y + size + 2, outerColor);
        graphics.fill(x - 2, y - 1, x - 1, y + size + 1, outerColor);
        graphics.fill(x + size + 1, y - 1, x + size + 2, y + size + 1, outerColor);

        int frameColor = filled ? 0xFF005500 : 0xFF001100;
        graphics.fill(x - 1, y - 1, x + size + 1, y, frameColor);
        graphics.fill(x - 1, y + size, x + size + 1, y + size + 1, frameColor);
        graphics.fill(x - 1, y, x, y + size, frameColor);
        graphics.fill(x + size, y, x + size + 1, y + size, frameColor);

        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int halfSize = size / 2 - 1;

        int color1, color2;
        if (filled) {
            color1 = 0xFF00FF00;
            color2 = 0xFF00AA00;
        } else {
            color1 = 0xFF003300;
            color2 = 0xFF001100;
        }

        for (int dy = -halfSize; dy <= halfSize; dy++) {
            int width = halfSize - Math.abs(dy);
            int lineY = centerY + dy;

            float gradientRatio = (dy + halfSize) / (float)(halfSize * 2);
            int gradientColor = interpolateColor(color1, color2, gradientRatio);

            graphics.fill(centerX - width, lineY, centerX + width + 1, lineY + 1, gradientColor);
        }

        if (filled) {
            for (int dy = -halfSize; dy <= -halfSize / 2; dy++) {
                int width = halfSize - Math.abs(dy);
                int lineY = centerY + dy;
                graphics.fill(centerX - width, lineY, centerX + width + 1, lineY + 1, 0x60FFFFFF);
            }

            graphics.fill(centerX, centerY - halfSize, centerX + 1, centerY + halfSize + 1, 0x80FFFFFF);
        }

        int cornerColor = filled ? 0xFF66FF66 : 0xFF224422;
        graphics.fill(x - 1, y - 1, x, y, cornerColor);
        graphics.fill(x + size, y - 1, x + size + 1, y, cornerColor);
        graphics.fill(x - 1, y + size, x, y + size + 1, cornerColor);
        graphics.fill(x + size, y + size, x + size + 1, y + size + 1, cornerColor);

        if (filled) {
            int fletchY = centerY + halfSize - 2;
            graphics.fill(centerX - 3, fletchY, centerX - 1, fletchY + 3, 0xFF00CC00);
            graphics.fill(centerX + 2, fletchY, centerX + 4, fletchY + 3, 0xFF00CC00);
        }
    }

    private int interpolateColor(int color1, int color2, float ratio) {
        int r = (int)(((color1 >> 16) & 0xFF) * (1 - ratio) + ((color2 >> 16) & 0xFF) * ratio);
        int g = (int)(((color1 >> 8) & 0xFF) * (1 - ratio) + ((color2 >> 8) & 0xFF) * ratio);
        int b = (int)((color1 & 0xFF) * (1 - ratio) + (color2 & 0xFF) * ratio);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}