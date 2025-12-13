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

    private static final int CHARGE_SIZE = 14;
    private static final int CHARGE_SPACING = 16;

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
        if (mc.player == null || mc.options.hideGui) return;

        PlayerRPGData rpg = mc.player.getData(ModAttachments.PLAYER_RPG);

        // Only show for Marksman
        if (!rpg.getCurrentClass().equals("MARKSMAN")) return;

        int charges = rpg.getMarksmanSeekerCharges();
        int maxCharges = 5;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Left side of hotbar
        int startX = (screenWidth / 2) - 110;
        int startY = screenHeight - 38;

        // Draw label
        String label = "§aSEEKER";
        graphics.drawString(mc.font, label, startX - 42, startY + 3, 0xFFFFFFFF, true);

        // Draw charges
        for (int i = 0; i < maxCharges; i++) {
            int chargeX = startX + (i * CHARGE_SPACING);
            int chargeY = startY;

            boolean filled = i < charges;
            int color = filled ? 0xFF00FF00 : 0xFF003300; // Bright lime or dark green
            
            drawCharge(graphics, chargeX, chargeY, CHARGE_SIZE, color, filled);
        }
    }

    private void drawCharge(GuiGraphics graphics, int x, int y, int size, int color, boolean filled) {
        RenderSystem.enableBlend();

        if (filled) {
            // Glow effect for filled charges
            long time = System.currentTimeMillis() / 200;
            if (time % 2 == 0) {
                graphics.fill(x - 2, y - 2, x + size + 2, y + size + 2, 
                    (color & 0x00FFFFFF) | 0x40000000);
            }
        }

        // Border
        int borderColor = filled ? 0xFF00AA00 : 0xFF002200;
        graphics.fill(x - 1, y - 1, x + size + 1, y + size + 1, borderColor);

        // Main diamond shape (rotated square)
        // Draw as a diamond using 4 triangles
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        int halfSize = size / 2;

        // Draw diamond using lines to create filled effect
        for (int dy = -halfSize; dy <= halfSize; dy++) {
            int width = halfSize - Math.abs(dy);
            int lineY = centerY + dy;
            graphics.fill(centerX - width, lineY, centerX + width + 1, lineY + 1, color);
        }

        // Highlight on top
        if (filled) {
            int highlightColor = 0xFF80FF80; // Brighter lime
            for (int dy = -halfSize; dy <= -halfSize / 2; dy++) {
                int width = halfSize - Math.abs(dy);
                int lineY = centerY + dy;
                graphics.fill(centerX - width, lineY, centerX + width + 1, lineY + 1, highlightColor);
            }
        }

        RenderSystem.disableBlend();
    }
}