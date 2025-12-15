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
public class AlchemistOverlay implements LayeredDraw.Layer {

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "alchemist_overlay"),
                new AlchemistOverlay()
        );
        System.out.println("RPG Classes: Alchemist Overlay Registered!");
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || mc.options.hideGui || mc.level == null) return;

        PlayerRPGData rpg;
        try {
            rpg = mc.player.getData(ModAttachments.PLAYER_RPG);
            if (rpg == null) {
                System.err.println("[ALCHEMIST OVERLAY] RPG data is null!");
                return;
            }
        } catch (Exception e) {
            System.err.println("[ALCHEMIST OVERLAY] Error getting RPG data: " + e.getMessage());
            return;
        }

        String currentClass = rpg.getCurrentClass();
        if (currentClass == null || !currentClass.equals("ALCHEMIST")) return;

        boolean isConcoctionActive = rpg.isAlchemistConcoction();
        boolean isInjectionActive = rpg.isAlchemistInjectionActive();

        // DEBUG: Print state every second
        if (mc.level.getGameTime() % 20 == 0) {
            System.out.println("[ALCHEMIST OVERLAY] Concoction: " + isConcoctionActive +
                    ", Injection: " + isInjectionActive +
                    ", Ticks: " + rpg.getAlchemistConcoctionTicks());
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (isConcoctionActive) {
            renderConcoctionOverlay(graphics, mc, rpg, screenWidth, screenHeight);
        } else if (isInjectionActive) {
            renderInjectionOverlay(graphics, mc, rpg, screenWidth, screenHeight);
        }

        // Always render enemy debuffs if any
        renderEnemyDebuffs(graphics, mc, rpg, screenWidth, screenHeight);

        RenderSystem.disableBlend();
    }

    private void renderConcoctionOverlay(GuiGraphics graphics, Minecraft mc, PlayerRPGData rpg, int screenWidth, int screenHeight) {
        String clickPattern = rpg.getAlchemistClickPattern();
        if (clickPattern == null) clickPattern = "";

        boolean isBuffMode = rpg.isAlchemistBuffMode();
        int maxClicks = isBuffMode ? 2 : 3;

        // Position: Bottom center, above hotbar
        int startX = screenWidth / 3;
        int startY = screenHeight - 100;

        // Draw title with glow effect for visibility
        String title = isBuffMode ? "§a§lCONCOCTION (BUFF)" : "§c§lCONCOCTION (DEBUFF)";
        int titleColor = isBuffMode ? 0xFFaaff00 : 0xFFff5555;

        // Draw background for title
        graphics.fill(startX - 2, startY - 2, startX + 120, startY + 10, 0xAA000000);
        graphics.drawString(mc.font, title, startX, startY, titleColor, true);

        // Draw click pattern boxes
        int boxY = startY + 12;
        int boxSize = 20;
        int spacing = 4;

        for (int i = 0; i < maxClicks; i++) {
            int boxX = startX + i * (boxSize + spacing);

            // Box background - brighter when active
            int bgColor = i < clickPattern.length() ? 0xDD222222 : 0xAA000000;
            graphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, bgColor);

            // Box border - thicker and brighter
            int borderColor = i < clickPattern.length() ? 0xFFFFFFFF : 0xFF888888;
            graphics.fill(boxX, boxY, boxX + boxSize, boxY + 1, borderColor);
            graphics.fill(boxX, boxY + boxSize - 1, boxX + boxSize, boxY + boxSize, borderColor);
            graphics.fill(boxX, boxY, boxX + 1, boxY + boxSize, borderColor);
            graphics.fill(boxX + boxSize - 1, boxY, boxX + boxSize, boxY + boxSize, borderColor);

            // Draw letter if clicked
            if (i < clickPattern.length()) {
                char click = clickPattern.charAt(i);
                String letter = String.valueOf(click);
                int letterColor = click == 'L' ? 0xFF00ff00 : 0xFFff0000; // Green for L, Red for R

                int letterWidth = mc.font.width(letter);
                int letterX = boxX + (boxSize - letterWidth) / 2;
                int letterY = boxY + (boxSize - mc.font.lineHeight) / 2;

                // Draw with shadow for visibility
                graphics.drawString(mc.font, letter, letterX + 1, letterY + 1, 0xFF000000, false);
                graphics.drawString(mc.font, letter, letterX, letterY, letterColor, false);
            }
        }

        // Draw effect preview
        int effectY = boxY + boxSize + 6;
        if (clickPattern.length() == maxClicks) {
            String effectName = getEffectName(clickPattern);
            graphics.fill(startX - 2, effectY - 2, startX + 150, effectY + 10, 0xAA000000);
            graphics.drawString(mc.font, "§e§lEffect: §f" + effectName, startX, effectY, 0xFFffff00, true);
        } else {
            graphics.fill(startX - 2, effectY - 2, startX + 100, effectY + 10, 0xAA000000);
            graphics.drawString(mc.font, "§7Effect: ???", startX, effectY, 0xFF888888, true);
        }

        // Draw instructions
        int instructY = effectY + 12;
        graphics.fill(startX - 2, instructY - 2, startX + 150, instructY + 10, 0xAA000000);
        graphics.drawString(mc.font, "§7Left/Right Click to build pattern", startX, instructY, 0xFFAAAAAA, false);
    }

    private void renderInjectionOverlay(GuiGraphics graphics, Minecraft mc, PlayerRPGData rpg, int screenWidth, int screenHeight) {
        String reagent = rpg.getAlchemistSelectedReagent();
        if (reagent == null) reagent = "CRYOSTAT";

        // Position: Bottom center, above hotbar
        int startX = screenWidth / 3;
        int startY = screenHeight - 100;

        // Draw title with background
        graphics.fill(startX - 2, startY - 2, startX + 100, startY + 10, 0xAA000000);
        graphics.drawString(mc.font, "§b§lINJECTION", startX, startY, 0xFF00ddff, true);

        // Draw reagent name with color
        int reagentY = startY + 12;
        String reagentText = "REAGENT: " + reagent;
        int reagentColor = getReagentColor(reagent);
        graphics.fill(startX - 2, reagentY - 2, startX + 120, reagentY + 10, 0xAA000000);
        graphics.drawString(mc.font, reagentText, startX, reagentY, reagentColor, true);

        // Draw instruction
        int instructionY = reagentY + 12;
        graphics.fill(startX - 2, instructionY - 2, startX + 120, instructionY + 10, 0xAA000000);
        graphics.drawString(mc.font, "§7(Shift to cycle)", startX, instructionY, 0xFF888888, false);
    }

    private void renderEnemyDebuffs(GuiGraphics graphics, Minecraft mc, PlayerRPGData rpg, int screenWidth, int screenHeight) {
        java.util.List<String> debuffs = rpg.getAlchemistEnemyDebuffs();

        if (debuffs == null || debuffs.isEmpty()) return;

        // Position: Right side of screen, middle
        int startX = screenWidth - 150;
        int startY = screenHeight / 2;

        // Draw title with background
        graphics.fill(startX - 2, startY - 2, startX + 120, startY + 10, 0xAA000000);
        graphics.drawString(mc.font, "§c§lEnemy Debuffs:", startX, startY, 0xFFff5555, true);

        // Draw each debuff
        int yOffset = startY + 12;
        for (String debuff : debuffs) {
            graphics.fill(startX - 2, yOffset - 2, startX + 120, yOffset + 10, 0xAA000000);
            graphics.drawString(mc.font, "§6• §f" + debuff, startX, yOffset, 0xFFffaa00, false);
            yOffset += 12;
        }
    }

    private String getEffectName(String pattern) {
        switch (pattern) {
            // Debuff patterns (3 clicks)
            case "LLL": return "SLOWNESS I";
            case "LRR": return "POISON I";
            case "LLR": return "CORROSION I";
            case "LRL": return "WITHER I";
            case "RRL": return "FREEZE";
            case "RLR": return "GLOWING";
            case "RRR": return "WEAKNESS I";

            // Buff patterns (2 clicks)
            case "LL": return "SPEED I";
            case "LR": return "RESISTANCE I";
            case "RL": return "REGENERATION I";
            case "RR": return "STRENGTH I";

            default: return "UNKNOWN";
        }
    }

    private int getReagentColor(String reagent) {
        switch (reagent) {
            case "CRYOSTAT": return 0xFF00ffff; // Cyan
            case "CATALYST": return 0xFFff0000; // Red
            case "FRACTURE": return 0xFF888888; // Gray
            case "SANCTIFIED": return 0xFFffaa00; // Gold
            default: return 0xFFffffff; // White
        }
    }
}