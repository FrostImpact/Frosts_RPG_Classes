package net.Frostimpact.rpgclasses.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.ability.AbilityRegistry;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class AbilityCooldownOverlay implements LayeredDraw.Layer {

    private static final Map<String, String[]> CLASS_ABILITIES = new HashMap<>();

    static {
        CLASS_ABILITIES.put("BLADEDANCER", new String[]{
                "dash", "blade_dance", "parry", "blade_waltz"
        });
        CLASS_ABILITIES.put("JUGGERNAUT", new String[]{
                "swap", "crush", "fortify", "leap"
        });
        CLASS_ABILITIES.put("MANAFORGE", new String[]{
                "magic_missile", "surge", "open_rift", "coalescence"
        });
        CLASS_ABILITIES.put("MARKSMAN", new String[]{
                "seekers", "vault", "updraft", "arrow_rain"
        });
        CLASS_ABILITIES.put("MERCENARY", new String[]{
                "cloak", "stun_bolt", "cycle_quiver", "hired_gun"
        });
        // ADD THIS:
        CLASS_ABILITIES.put("RULER", new String[]{
                "call_to_arms", "invigorate", "regroup", "rally"
        });
        CLASS_ABILITIES.put("ARTIFICER", new String[]{
                "turret", "tower", "reposition", "self_destruct"
        });
        CLASS_ABILITIES.put("MIRAGE", new String[]{
                "reflections", "shadowstep", "recall", "fracture_line"
        });
        CLASS_ABILITIES.put("ALCHEMIST", new String[]{
                "flask", "volatile_mix", "distill", "injection"
        });
    }

    private static final int ICON_SIZE = 16;
    private static final int SLOT_SIZE = 22;
    private static final int SLOT_SPACING = 24;
    private static final int FRAME_WIDTH = 2;

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "ability_cooldowns"),
                new AbilityCooldownOverlay()
        );
        System.out.println("RPG Classes: Ability Cooldown Overlay Registered!");
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        PlayerRPGData rpg = mc.player.getData(ModAttachments.PLAYER_RPG);
        String currentClass = rpg.getCurrentClass();

        String[] trackedAbilities = CLASS_ABILITIES.get(currentClass);
        if (trackedAbilities == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Position: Above hotbar, right side
        int startX = (screenWidth / 2) + 100;
        int startY = screenHeight - 30;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (int i = 0; i < trackedAbilities.length; i++) {
            String abilityId = trackedAbilities[i];
            Ability ability = AbilityRegistry.getAbility(abilityId);
            if (ability == null) continue;

            int slotX = startX + (i * SLOT_SPACING);
            int slotY = startY;

            int cooldownTicks = rpg.getAbilityCooldown(abilityId);
            int maxCooldown = ability.getCooldownTicks();
            int manaCost = ability.getManaCost();
            int currentMana = rpg.getMana();

            boolean onCooldown = cooldownTicks > 0;
            boolean notEnoughMana = currentMana < manaCost;
            boolean canUse = !onCooldown && !notEnoughMana;

            // === ORNATE FRAME ===
            drawOrnateSlotFrame(graphics, slotX, slotY, SLOT_SIZE, canUse, onCooldown, notEnoughMana);

            // === SLOT BACKGROUND ===
            int innerX = slotX + FRAME_WIDTH;
            int innerY = slotY + FRAME_WIDTH;
            int innerSize = SLOT_SIZE - (FRAME_WIDTH * 2);

            // Consistent grey background for all slots
            int bgColor = 0xFF1a1a1a;
            graphics.fill(innerX, innerY, innerX + innerSize, innerY + innerSize, bgColor);

            // Inner shadow for depth
            graphics.fill(innerX, innerY, innerX + innerSize, innerY + 1, 0x80000000);
            graphics.fill(innerX, innerY, innerX + 1, innerY + innerSize, 0x60000000);

            // === RENDER ICON ===
            int iconX = slotX + (SLOT_SIZE - ICON_SIZE) / 2;
            int iconY = slotY + (SLOT_SIZE - ICON_SIZE) / 2;

            ItemStack icon = getAbilityIcon(currentClass, abilityId);
            graphics.renderItem(icon, iconX, iconY);

            // === MANA CHECK OVERLAY ===
            if (notEnoughMana) {
                RenderSystem.disableDepthTest();
                graphics.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, 0x800000AA);
                RenderSystem.enableDepthTest();
            }

            // === COOLDOWN OVERLAY ===
            if (onCooldown && maxCooldown > 0) {
                RenderSystem.disableDepthTest();

                float progress = (float) cooldownTicks / (float) maxCooldown;
                progress = Math.min(1.0f, Math.max(0.0f, progress));

                int boxHeight = (int) (progress * ICON_SIZE);
                boxHeight = Math.min(ICON_SIZE, Math.max(0, boxHeight));

                int boxTop = iconY + (ICON_SIZE - boxHeight);
                int boxBottom = iconY + ICON_SIZE;

                // Dark overlay showing remaining cooldown
                graphics.fill(iconX, boxTop, iconX + ICON_SIZE, boxBottom, 0xC0000000);

                RenderSystem.enableDepthTest();

                // Cooldown number
                int secondsLeft = (cooldownTicks + 19) / 20;
                String cdText = String.valueOf(secondsLeft);
                int cdTextWidth = mc.font.width(cdText);
                int cdX = slotX + (SLOT_SIZE - cdTextWidth) / 2;
                int cdY = slotY + (SLOT_SIZE - mc.font.lineHeight) / 2;

                graphics.drawString(mc.font, cdText, cdX + 1, cdY + 1, 0xFF000000, false);
                graphics.drawString(mc.font, cdText, cdX, cdY, 0xFFFFFFFF, false);
            }

            // === READY GLOW === (removed - keeping consistent appearance)
        }

        RenderSystem.disableBlend();
    }

    private void drawOrnateSlotFrame(GuiGraphics graphics, int x, int y, int size, boolean ready, boolean onCooldown, boolean noMana) {
        // Consistent ornate grey/bronze frame for all states
        int outerColor = 0xFF8b7355; // Bronze
        int frameColor1 = 0xFF5c4033; // Dark brown
        int frameColor2 = 0xFF3d2817; // Darker brown
        int cornerColor = 0xFFd4af37; // Gold

        // Outer border
        graphics.fill(x - 1, y - 1, x + size + 1, y, outerColor);
        graphics.fill(x - 1, y + size, x + size + 1, y + size + 1, outerColor);
        graphics.fill(x - 1, y, x, y + size, outerColor);
        graphics.fill(x + size, y, x + size + 1, y + size, outerColor);

        // Main frame
        graphics.fill(x, y, x + size, y + FRAME_WIDTH, frameColor1);
        graphics.fill(x, y + size - FRAME_WIDTH, x + size, y + size, frameColor2);
        graphics.fill(x, y, x + FRAME_WIDTH, y + size, frameColor1);
        graphics.fill(x + size - FRAME_WIDTH, y, x + size, y + size, frameColor2);

        // Inner highlight (subtle for all slots)
        int highlightColor = 0x40FFFFFF;
        graphics.fill(x + FRAME_WIDTH, y + FRAME_WIDTH, x + size - FRAME_WIDTH, y + FRAME_WIDTH + 1, highlightColor);
        graphics.fill(x + FRAME_WIDTH, y + FRAME_WIDTH, x + FRAME_WIDTH + 1, y + size - FRAME_WIDTH, highlightColor);

        // Corner decorations (gold accents)
        graphics.fill(x - 1, y - 1, x, y, cornerColor);
        graphics.fill(x + size, y - 1, x + size + 1, y, cornerColor);
        graphics.fill(x - 1, y + size, x, y + size + 1, cornerColor);
        graphics.fill(x + size, y + size, x + size + 1, y + size + 1, cornerColor);
    }

    private ItemStack getAbilityIcon(String className, String abilityId) {
        if (className.equals("BLADEDANCER")) {
            return switch (abilityId) {
                case "dash" -> new ItemStack(Items.FEATHER);
                case "blade_dance" -> new ItemStack(Items.IRON_SWORD);
                case "parry" -> new ItemStack(Items.SHIELD);
                case "blade_waltz" -> new ItemStack(Items.DIAMOND_SWORD);
                default -> new ItemStack(Items.BARRIER);
            };
        }

        if (className.equals("JUGGERNAUT")) {
            return switch (abilityId) {
                case "swap" -> new ItemStack(Items.SHIELD);
                case "crush" -> new ItemStack(Items.IRON_AXE);
                case "fortify" -> new ItemStack(Items.IRON_CHESTPLATE);
                case "leap" -> new ItemStack(Items.LEATHER_BOOTS);
                default -> new ItemStack(Items.BARRIER);
            };
        }

        if (className.equals("MANAFORGE")) {
            return switch (abilityId) {
                case "magic_missile" -> new ItemStack(Items.AMETHYST_SHARD);
                case "surge" -> new ItemStack(Items.DRAGON_BREATH);
                case "open_rift" -> new ItemStack(Items.ENDER_PEARL);
                case "coalescence" -> new ItemStack(Items.TOTEM_OF_UNDYING);
                default -> new ItemStack(Items.BARRIER);
            };
        }

        if (className.equals("MARKSMAN")) {
            return switch (abilityId) {
                case "seekers" -> new ItemStack(Items.SPECTRAL_ARROW);
                case "vault" -> new ItemStack(Items.LEATHER_BOOTS);
                case "updraft" -> new ItemStack(Items.FEATHER);
                case "arrow_rain" -> new ItemStack(Items.BOW);
                default -> new ItemStack(Items.BARRIER);
            };
        }

        if (className.equals("MERCENARY")) {
            return switch (abilityId) {
                case "cloak" -> new ItemStack(Items.GLASS);
                case "stun_bolt" -> new ItemStack(Items.LIGHTNING_ROD);
                case "cycle_quiver" -> new ItemStack(Items.ARROW);
                case "hired_gun" -> new ItemStack(Items.CROSSBOW);
                default -> new ItemStack(Items.BARRIER);
            };
        }

        if (className.equals("RULER")) {
            return switch (abilityId) {
                case "call_to_arms" -> new ItemStack(Items.IRON_SWORD);
                case "invigorate" -> new ItemStack(Items.GOLDEN_APPLE);
                case "regroup" -> new ItemStack(Items.BELL);
                case "rally" -> new ItemStack(Items.YELLOW_BANNER);
                default -> new ItemStack(Items.BARRIER);
            };
        }

        if (className.equals("ARTIFICER")) {
            return switch (abilityId) {
                case "turret" -> new ItemStack(Items.DISPENSER);
                case "tower" -> new ItemStack(Items.LIGHTNING_ROD);
                case "reposition" -> new ItemStack(Items.ENDER_PEARL);
                case "self_destruct" -> new ItemStack(Items.TNT);
                default -> new ItemStack(Items.BARRIER);
            };
        }

        if (className.equals("ARTIFICER")) {
            return switch (abilityId) {
                case "turret" -> new ItemStack(Items.DISPENSER);
                case "tower" -> new ItemStack(Items.LIGHTNING_ROD);
                case "reposition" -> new ItemStack(Items. ENDER_PEARL);
                case "self_destruct" -> new ItemStack(Items.TNT);
                default -> new ItemStack(Items.BARRIER);
            };
        }

        if (className.equals("MIRAGE")) {
            return switch (abilityId) {
                case "reflections" -> new ItemStack(Items.GLASS_PANE);
                case "shadowstep" -> new ItemStack(Items.ENDER_PEARL);
                case "recall" -> new ItemStack(Items.RECOVERY_COMPASS);
                case "fracture_line" -> new ItemStack(Items.PRISMARINE_SHARD);
                default -> new ItemStack(Items.BARRIER);
            };
        }

        if (className.equals("ALCHEMIST")) {
            return switch (abilityId) {
                case "flask" -> new ItemStack(Items.POTION);
                case "volatile_mix" -> new ItemStack(Items.DRAGON_BREATH);
                case "distill" -> new ItemStack(Items.GLASS_BOTTLE);
                case "injection" -> new ItemStack(Items.ARROW);
                default -> new ItemStack(Items.BARRIER);
            };
        }

        return new ItemStack(Items.BARRIER);
    }
}