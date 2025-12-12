package net.Frostimpact.rpgclasses.client;

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

        // Position: Left of hotbar, below TEMPO counter if it exists
        int barX = (screenWidth / 2) - 110 - BAR_WIDTH;
        int barY = screenHeight - 32; // Above hotbar

        // Draw mode indicator
        String modeText = isShieldMode ? "§b🛡 SHIELD" : "§c⚔ SHATTER";
        graphics.drawString(mc.font, modeText, barX, barY - 12, 0xFFFFFFFF, true);

        // Draw charge bar background
        graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xFF000000);
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF333333);

        // Calculate fill width
        float fillPercent = (float) charge / (float) maxCharge;
        int fillWidth = (int) (BAR_WIDTH * fillPercent);

        // Determine bar color based on mode and charge level
        int barColor;
        if (isShieldMode) {
            // SHIELD mode: Blue gradient
            barColor = 0xFF0099FF;
        } else {
            // SHATTER mode: Color changes based on charge
            if (charge > 50) {
                barColor = 0xFFFF0000; // Red for Strength II
            } else if (charge > 0) {
                barColor = 0xFFFFAA00; // Orange for Strength I
            } else {
                barColor = 0xFF666666; // Gray for empty
            }
        }

        // Draw fill
        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, barColor);

            // Add glow effect
            int glowColor = (barColor & 0x00FFFFFF) | 0x40000000;
            graphics.fill(barX, barY - 2, barX + fillWidth, barY, glowColor);
            graphics.fill(barX, barY + BAR_HEIGHT, barX + fillWidth, barY + BAR_HEIGHT + 2, glowColor);
        }

        // Draw decay warning
        if (isDecaying) {
            String decayText = "§c⚠ DECAYING";
            int decayX = barX + BAR_WIDTH + 5;
            graphics.drawString(mc.font, decayText, decayX, barY, 0xFFFF0000, true);

            // Pulsing effect
            if ((System.currentTimeMillis() / 500) % 2 == 0) {
                graphics.fill(barX, barY - 1, barX + BAR_WIDTH, barY, 0xFFFF0000);
                graphics.fill(barX, barY + BAR_HEIGHT, barX + BAR_WIDTH, barY + BAR_HEIGHT + 1, 0xFFFF0000);
            }
        }

        // Draw charge text (centered on bar)
        String chargeText = charge + "/" + maxCharge;
        int textWidth = mc.font.width(chargeText);
        int textX = barX + (BAR_WIDTH - textWidth) / 2;
        int textY = barY + 1;

        // Draw text with shadow for readability
        graphics.drawString(mc.font, chargeText, textX, textY, 0xFFFFFFFF, true);

        // Draw threshold markers at 50 charge
        int threshold50X = barX + (BAR_WIDTH / 2);
        graphics.fill(threshold50X, barY - 2, threshold50X + 1, barY + BAR_HEIGHT + 2, 0xFFFFFFFF);
    }
}