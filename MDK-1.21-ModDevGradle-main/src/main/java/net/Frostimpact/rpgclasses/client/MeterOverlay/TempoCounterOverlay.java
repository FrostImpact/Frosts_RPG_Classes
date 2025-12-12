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

    private static final int STACK_SIZE = 12;
    private static final int STACK_SPACING = 14;

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

        // Only show for Bladedancer
        if (!rpg.getCurrentClass().equals("BLADEDANCER")) return;

        int tempoStacks = rpg.getTempoStacks();
        boolean isActive = rpg.isTempoActive();
        boolean isFinalWaltz = rpg.isFinalWaltzActive();

        // Don't render if no stacks
        if (tempoStacks == 0 && !isActive) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Left side of the hotbar
        int startX = (screenWidth / 2) - 110;
        int startY = screenHeight - 20;

        // Calculate total width
        int displayStacks = isFinalWaltz ? tempoStacks : Math.min(tempoStacks, 4);
        int totalWidth = (displayStacks * STACK_SPACING);

        int baseX = startX - totalWidth;

        // Draw label
        String labelColor = isFinalWaltz ? "§5" : "§6";
        String label = labelColor + "TEMPO";
        graphics.drawString(mc.font, label, baseX - 38, startY + 2, 0xFFFFFFFF, true);

        // Draw stack indicators
        for (int i = 0; i < displayStacks; i++) {
            int stackX = baseX + (i * STACK_SPACING);
            int stackY = startY;

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

            drawStack(graphics, stackX, stackY, STACK_SIZE, color, isActive && i < 3);
        }

        // Draw Strength level
        if (isActive) {
            String strengthText;
            int strengthColor;

            if (isFinalWaltz && tempoStacks >= 6) {
                strengthText = "§lII";
                strengthColor = 0xFFFF00FF;
            } else {
                strengthText = "§lI";
                strengthColor = 0xFFFFAA00;
            }

            graphics.drawString(mc.font, strengthText,
                    baseX + totalWidth + 5, startY + 2, strengthColor, true);
        }

        // Draw overflow counter
        if (isFinalWaltz && tempoStacks > 6) {
            int overflow = rpg.getFinalWaltzOverflow();
            String overflowText = "§d+" + overflow;
            graphics.drawString(mc.font, overflowText,
                    baseX + totalWidth + 20, startY + 2, 0xFFFF00FF, true);
        }
    }

    private void drawStack(GuiGraphics graphics, int x, int y, int size, int color, boolean glow) {
        RenderSystem.enableBlend();

        if (glow) {
            graphics.fill(x - 2, y - 2, x + size + 2, y + size + 2, (color & 0x00FFFFFF) | 0x40000000);
        }

        // Border
        int borderColor = darkenColor(color);
        graphics.fill(x - 1, y - 1, x + size + 1, y + size + 1, borderColor);

        // Main square
        graphics.fill(x, y, x + size, y + size, color);

        // Highlight
        int highlightColor = lightenColor(color);
        graphics.fill(x, y, x + size - 2, y + 2, highlightColor);
        graphics.fill(x, y, x + 2, y + size - 2, highlightColor);

        RenderSystem.disableBlend();
    }

    private int darkenColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = Math.max(0, r - 60);
        g = Math.max(0, g - 60);
        b = Math.max(0, b - 60);

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private int lightenColor(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        r = Math.min(255, r + 60);
        g = Math.min(255, g + 60);
        b = Math.min(255, b + 60);

        return 0x80000000 | (r << 16) | (g << 8) | b;
    }
}