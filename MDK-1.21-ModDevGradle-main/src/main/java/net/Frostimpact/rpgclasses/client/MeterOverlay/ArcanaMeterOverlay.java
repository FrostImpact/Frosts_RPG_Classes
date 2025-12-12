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
public class ArcanaMeterOverlay implements LayeredDraw.Layer {

    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 8;

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
        if (mc.player == null || mc.options.hideGui) return;

        PlayerRPGData rpg = mc.player.getData(ModAttachments.PLAYER_RPG);

        // Only show for Manaforge
        if (!rpg.getCurrentClass().equals("MANAFORGE")) return;

        int arcana = rpg.getManaforgeArcana();
        int maxArcana = 100;
        boolean isCoalescence = rpg.isCoalescenceActive();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Left of hotbar
        int barX = (screenWidth / 2) - 110 - BAR_WIDTH;
        int barY = screenHeight - 32;

        // === LABEL ===
        String labelText = isCoalescence ? "COALESCENCE" : "ARCANA";
        int labelColor = isCoalescence ? 0xFF9900FF : 0xFF00DDFF; // Purple for coalescence, cyan for normal

        graphics.drawString(mc.font, labelText, barX, barY - 12, labelColor, true);

        // === BAR ===
        // Outer border (black)
        graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xFF000000);

        // Background (dark gray)
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF1a1a1a);

        // Calculate fill width
        float fillPercent = (float) arcana / (float) maxArcana;
        int fillWidth = (int) (BAR_WIDTH * fillPercent);

        // Determine bar color based on ARCANA level
        int barColor;
        if (arcana == 100) {
            barColor = 0xFFFF00FF; // Magenta at max
        } else if (arcana >= 75) {
            barColor = 0xFF9900FF; // Purple at high
        } else if (arcana >= 50) {
            barColor = 0xFF0099FF; // Cyan at medium
        } else if (arcana >= 25) {
            barColor = 0xFF0066AA; // Blue at low-medium
        } else {
            barColor = 0xFF003366; // Dark blue at low
        }

        // Pulsing effect at max ARCANA
        if (arcana == 100) {
            long pulseTime = System.currentTimeMillis() / 200;
            if (pulseTime % 2 == 0) {
                barColor = 0xFFFFFFFF; // Flash white
            }
        }

        // Draw fill
        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, barColor);

            // Top highlight
            int highlightColor = (barColor & 0x00FFFFFF) | 0x60FFFFFF;
            graphics.fill(barX, barY, barX + fillWidth, barY + 2, highlightColor);
        }

        // === THRESHOLD MARKERS ===
        // Mark 25, 50, 75 ARCANA thresholds
        for (int threshold : new int[]{25, 50, 75}) {
            int markerX = barX + (BAR_WIDTH * threshold / 100);
            graphics.fill(markerX, barY, markerX + 1, barY + BAR_HEIGHT, 0x80FFFFFF);
        }

        // === ARCANA TEXT ===
        String arcanaText = arcana + "/" + maxArcana;
        int textWidth = mc.font.width(arcanaText);
        int textX = barX + (BAR_WIDTH - textWidth) / 2;
        int textY = barY;

        graphics.drawString(mc.font, arcanaText, textX, textY, 0xFFFFFFFF, true);

        // === BONUS INDICATOR ===
        if (arcana >= 25) {
            int bonusProjectiles = arcana / 25;
            String bonusText = "+" + bonusProjectiles + " ⚡";
            graphics.drawString(mc.font, bonusText, barX + BAR_WIDTH + 5, barY, 0xFF00FF00, true);
        }

        // === COALESCENCE STORED DAMAGE ===
        if (isCoalescence) {
            float storedDamage = rpg.getCoalescenceStoredDamage();
            if (storedDamage > 0) {
                String damageText = "§5✦ " + String.format("%.0f", storedDamage) + " DMG";
                graphics.drawString(mc.font, damageText, barX, barY + BAR_HEIGHT + 3, 0xFFFFFFFF, true);
            }
        }
    }
}