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
    private static int lastArcana = -1;
    private static long lastLogTime = 0;

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

        // Safety checks
        if (mc.player == null) return;
        if (mc.options.hideGui) return;
        if (mc.level == null) return;

        PlayerRPGData rpg;
        try {
            rpg = mc.player.getData(ModAttachments.PLAYER_RPG);
            if (rpg == null) {
                System.err.println("RPG Classes: RPG data is null!");
                return;
            }
        } catch (Exception e) {
            System.err.println("RPG Classes: Failed to get player RPG data for Arcana overlay: " + e.getMessage());
            return;
        }

        // Only show for Manaforge
        String currentClass = rpg.getCurrentClass();
        if (currentClass == null || !currentClass.equals("MANAFORGE")) return;

        int arcana = rpg.getManaforgeArcana();

        // Debug logging - only log when arcana changes or every 5 seconds
        long currentTime = System.currentTimeMillis();
        if (arcana != lastArcana || (currentTime - lastLogTime) > 5000) {
            System.out.println("RPG Classes [CLIENT]: Arcana value = " + arcana);
            lastArcana = arcana;
            lastLogTime = currentTime;
        }

        int maxArcana = 100;
        boolean isCoalescence = rpg.isCoalescenceActive();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Left of hotbar, slightly higher to avoid overlap
        int barX = (screenWidth / 2) - 110 - BAR_WIDTH;
        int barY = screenHeight - 38;

        // === LABEL ===
        String labelText = isCoalescence ? "COALESCENCE" : "ARCANA";
        int labelColor = isCoalescence ? 0xFF9900FF : 0xFF00DDFF;

        graphics.drawString(mc.font, labelText, barX, barY - 10, labelColor, true);

        // === BAR FRAME ===
        // Outer border (bright edge)
        graphics.fill(barX - 2, barY - 2, barX + BAR_WIDTH + 2, barY + BAR_HEIGHT + 2, 0xFF444444);

        // Inner border (black)
        graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xFF000000);

        // Background (dark gray)
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF1a1a1a);

        // === BAR FILL ===
        float fillPercent = Math.max(0, Math.min(1, (float) arcana / (float) maxArcana));
        int fillWidth = (int) (BAR_WIDTH * fillPercent);

        // Determine bar color based on ARCANA level
        int barColor;
        if (isCoalescence) {
            barColor = 0xFF9900FF;
        } else if (arcana == 100) {
            long pulseTime = System.currentTimeMillis() / 200;
            barColor = (pulseTime % 2 == 0) ? 0xFFFFFFFF : 0xFFFF00FF;
        } else if (arcana >= 75) {
            barColor = 0xFF9900FF;
        } else if (arcana >= 50) {
            barColor = 0xFF0099FF;
        } else if (arcana >= 25) {
            barColor = 0xFF0066AA;
        } else {
            barColor = 0xFF003366;
        }

        // Draw fill
        if (fillWidth > 0) {
            graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, barColor);

            // Top highlight for depth
            int highlightColor = (barColor & 0x00FFFFFF) | 0x60FFFFFF;
            graphics.fill(barX, barY, barX + fillWidth, barY + 2, highlightColor);
        }

        // === THRESHOLD MARKERS ===
        for (int threshold : new int[]{25, 50, 75}) {
            int markerX = barX + (BAR_WIDTH * threshold / 100);
            graphics.fill(markerX, barY, markerX + 1, barY + BAR_HEIGHT, 0x80FFFFFF);
        }

        // === ARCANA TEXT (centered in bar) ===
        String arcanaText = arcana + "/" + maxArcana;
        int textWidth = mc.font.width(arcanaText);
        int textX = barX + (BAR_WIDTH - textWidth) / 2;
        int textY = barY;

        graphics.drawString(mc.font, arcanaText, textX, textY, 0xFFFFFFFF, true);

        // === BONUS INDICATOR ===
        if (arcana >= 25) {
            int bonusProjectiles = arcana / 25;
            String bonusText = "+" + bonusProjectiles + " ⚡";
            int bonusColor = arcana == 100 ? 0xFFFFFF00 : 0xFF00FF00;
            graphics.drawString(mc.font, bonusText, barX + BAR_WIDTH + 5, barY, bonusColor, true);
        }

        // === COALESCENCE STORED DAMAGE ===
        if (isCoalescence) {
            float storedDamage = rpg.getCoalescenceStoredDamage();
            if (storedDamage > 0) {
                String damageText = "§5✦ " + String.format("%.0f", storedDamage) + " DMG";
                graphics.drawString(mc.font, damageText, barX, barY + BAR_HEIGHT + 3, 0xFFFFFFFF, true);
            }
        }

        // === MAX ARCANA GLOW ===
        if (arcana == 100 && !isCoalescence) {
            long glowTime = System.currentTimeMillis() / 300;
            if (glowTime % 2 == 0) {
                int glowColor = 0x40FF00FF;
                graphics.fill(barX - 2, barY - 2, barX + BAR_WIDTH + 2, barY - 1, glowColor);
                graphics.fill(barX - 2, barY + BAR_HEIGHT + 1, barX + BAR_WIDTH + 2, barY + BAR_HEIGHT + 2, glowColor);
                graphics.fill(barX - 2, barY, barX - 1, barY + BAR_HEIGHT, glowColor);
                graphics.fill(barX + BAR_WIDTH + 1, barY, barX + BAR_WIDTH + 2, barY + BAR_HEIGHT, glowColor);
            }
        }
    }
}