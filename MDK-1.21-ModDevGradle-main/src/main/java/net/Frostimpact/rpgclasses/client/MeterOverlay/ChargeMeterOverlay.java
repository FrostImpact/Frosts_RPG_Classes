package net.Frostimpact.rpgclasses.client.MeterOverlay;

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

    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 8;

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

        // Only show for Juggernaut
        if (!rpg.getCurrentClass().equals("JUGGERNAUT")) return;

        int charge = rpg.getJuggernautCharge();
        int maxCharge = rpg.getJuggernautMaxCharge();
        boolean isShieldMode = rpg.isJuggernautShieldMode();
        boolean isDecaying = rpg.isChargeDecaying();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Left of hotbar
        int barX = (screenWidth / 2) - 110 - BAR_WIDTH;
        int barY = screenHeight - 32;

        // === MODE INDICATOR ===
        String modeText;
        int modeColor;
        if (isShieldMode) {
            modeText = "SHIELD";
            modeColor = 0xFF00AAFF; // Bright blue
        } else {
            modeText = "SHATTER";
            modeColor = 0xFFFF4444; // Bright red
        }

        // Draw mode text above bar
        graphics.drawString(mc.font, modeText, barX, barY - 12, modeColor, true);

        // === CHARGE BAR ===
        // Outer border (black)
        graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xFF000000);

        // Background (dark gray)
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF1a1a1a);

        // Calculate fill width
        float fillPercent = (float) charge / (float) maxCharge;
        int fillWidth = (int) (BAR_WIDTH * fillPercent);

        // Determine bar color
        int barColor;
        if (isShieldMode) {
            barColor = 0xFF00DDFF; // Cyan
        } else {
            if (charge > 50) {
                barColor = 0xFFFF0000; // Red
            } else if (charge > 0) {
                barColor = 0xFFFF8800; // Orange
            } else {
                barColor = 0xFF444444; // Gray
            }
        }

        // Draw fill
        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, barColor);

            // Top highlight
            int highlightColor = (barColor & 0x00FFFFFF) | 0x60FFFFFF;
            graphics.fill(barX, barY, barX + fillWidth, barY + 2, highlightColor);
        }

        // === DECAY WARNING ===
        if (isDecaying) {
            long pulseTime = System.currentTimeMillis() / 300;
            if (pulseTime % 2 == 0) {
                graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY, 0xFFFF0000);
                graphics.fill(barX - 1, barY + BAR_HEIGHT, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xFFFF0000);
                graphics.fill(barX - 1, barY, barX, barY + BAR_HEIGHT, 0xFFFF0000);
                graphics.fill(barX + BAR_WIDTH, barY, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT, 0xFFFF0000);
            }

            String decayText = "DECAY";
            graphics.drawString(mc.font, decayText, barX + BAR_WIDTH + 5, barY, 0xFFFF0000, true);
        }

        // === CHARGE TEXT ===
        String chargeText = charge + "/" + maxCharge;
        int textWidth = mc.font.width(chargeText);
        int textX = barX + (BAR_WIDTH - textWidth) / 2;
        int textY = barY;

        graphics.drawString(mc.font, chargeText, textX, textY, 0xFFFFFFFF, true);

        // === THRESHOLD MARKER ===
        int threshold50X = barX + (BAR_WIDTH / 2);
        graphics.fill(threshold50X, barY, threshold50X + 1, barY + BAR_HEIGHT, 0x80FFFFFF);
    }
}