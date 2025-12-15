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
            if (rpg == null) return;
        } catch (Exception e) {
            return;
        }

        String currentClass = rpg.getCurrentClass();
        if (currentClass == null || !currentClass.equals("ALCHEMIST")) return;

        boolean isConcoctionActive = rpg.isAlchemistConcoction();
        boolean isInjectionActive = rpg.isAlchemistInjectionActive();

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
        boolean isBuffMode = rpg.isAlchemistBuffMode();
        int maxClicks = isBuffMode ? 2 : 3;

        // Position: Bottom center, above hotbar
        int startX = screenWidth / 2 - 60;
        int startY = screenHeight - 80;

        // Draw title
        String title = isBuffMode ? "CONCOCTION (BUFF)" : "CONCOCTION (DEBUFF)";
        int titleColor = isBuffMode ? 0xFFaaff00 : 0xFFff5555;
        graphics.drawString(mc.font, title, startX, startY, titleColor, true);

        // Draw click pattern boxes
        int boxY = startY + 12;
        int boxSize = 20;
        int spacing = 4;

        for (int i = 0; i < maxClicks; i++) {
            int boxX = startX + i * (boxSize + spacing);
            
            // Box background
            graphics.fill(boxX, boxY, boxX + boxSize, boxY + boxSize, 0xAA000000);
            
            // Box border
            int borderColor = 0xFF888888;
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
                
                graphics.drawString(mc.font, letter, letterX, letterY, letterColor, false);
            }
        }

        // Draw effect preview
        if (clickPattern.length() == maxClicks) {
            String effectName = getEffectName(clickPattern);
            int effectY = boxY + boxSize + 6;
            graphics.drawString(mc.font, "Effect: " + effectName, startX, effectY, 0xFFffff00, true);
        } else {
            int effectY = boxY + boxSize + 6;
            graphics.drawString(mc.font, "Effect: ???", startX, effectY, 0xFF888888, true);
        }
    }

    private void renderInjectionOverlay(GuiGraphics graphics, Minecraft mc, PlayerRPGData rpg, int screenWidth, int screenHeight) {
        String reagent = rpg.getAlchemistSelectedReagent();

        // Position: Bottom center, above hotbar
        int startX = screenWidth / 2 - 60;
        int startY = screenHeight - 80;

        // Draw title
        graphics.drawString(mc.font, "INJECTION", startX, startY, 0xFF00ddff, true);

        // Draw reagent name with color
        int reagentY = startY + 12;
        String reagentText = "REAGENT: " + reagent;
        int reagentColor = getReagentColor(reagent);
        graphics.drawString(mc.font, reagentText, startX, reagentY, reagentColor, true);

        // Draw instruction
        int instructionY = reagentY + 12;
        graphics.drawString(mc.font, "(Shift to cycle)", startX, instructionY, 0xFF888888, false);
    }

    private void renderEnemyDebuffs(GuiGraphics graphics, Minecraft mc, PlayerRPGData rpg, int screenWidth, int screenHeight) {
        java.util.List<String> debuffs = rpg.getAlchemistEnemyDebuffs();
        
        if (debuffs == null || debuffs.isEmpty()) return;

        // Position: Right side of screen, middle
        int startX = screenWidth - 150;
        int startY = screenHeight / 2;

        // Draw title
        graphics.drawString(mc.font, "Enemy Debuffs:", startX, startY, 0xFFff5555, true);

        // Draw each debuff
        int yOffset = startY + 12;
        for (String debuff : debuffs) {
            graphics.drawString(mc.font, "• " + debuff, startX, yOffset, 0xFFffaa00, false);
            yOffset += 10;
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
            case "CATALYST": return 0xFFff0000; // Dark red
            case "FRACTURE": return 0xFF888888; // Gray
            case "SANCTIFIED": return 0xFFffaa00; // Gold
            default: return 0xFFffffff; // White
        }
    }
}
