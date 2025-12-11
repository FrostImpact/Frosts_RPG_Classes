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

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class AbilityCooldownOverlay implements LayeredDraw.Layer {

    private static final String[] TRACKED_ABILITIES = {
            "dash",
            "blade_dance",
            "parry",
            "blade_waltz"
    };

    // UI Constants
    private static final int ICON_SIZE = 16;
    private static final int SLOT_SIZE = 18; // 1px border around 16px icon
    private static final int SLOT_SPACING = 22; // Space between slots

    @SubscribeEvent
    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.HOTBAR,
                ResourceLocation.fromNamespaceAndPath(RpgClassesMod.MOD_ID, "ability_cooldowns"),
                new AbilityCooldownOverlay()
        );
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        PlayerRPGData rpg = mc.player.getData(ModAttachments.PLAYER_RPG);

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // Start X: Center + Offset. Start Y: Bottom aligned with hotbar
        int startX = (screenWidth / 2) + 95;
        int startY = screenHeight - 22;

        for (int i = 0; i < TRACKED_ABILITIES.length; i++) {
            String abilityId = TRACKED_ABILITIES[i];
            Ability ability = AbilityRegistry.getAbility(abilityId);
            if (ability == null) continue;

            // Calculate position for this slot
            int slotX = startX + (i * SLOT_SPACING);
            int slotY = startY;

            // Center the 16x16 icon inside the 18x18 slot
            int iconX = slotX + 1;
            int iconY = slotY + 1;

            int cooldownTicks = rpg.getAbilityCooldown(abilityId);
            int maxCooldown = ability.getCooldownTicks();
            int manaCost = ability.getManaCost();
            int currentMana = rpg.getMana();

            // 1. Draw Slot Background
            // Draw a dark gray background
            graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x90000000);
            // Draw a border
            graphics.renderOutline(slotX, slotY, SLOT_SIZE, SLOT_SIZE, 0xFF333333);

            // 2. Render Icon
            ItemStack icon = getPlaceholderIcon(abilityId);
            graphics.renderItem(icon, iconX, iconY);

            // 3. Render Mana Check
            if (currentMana < manaCost) {
                RenderSystem.disableDepthTest();
                // use PoseStack translation to ensure z-index correctness if needed,
                // but usually disabling depth test is enough for 2D GUI overlays.
                graphics.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, 0x600000AA);
                RenderSystem.enableDepthTest();
            }

            // Render Cooldown "Tick Down"
            if (cooldownTicks > 0 && maxCooldown > 0) {
                RenderSystem.disableDepthTest();

                // IT SHOULD NEVER EXCEEDS 1.0, JUST MAKING SURE
                float progress = (float) cooldownTicks / (float) maxCooldown;
                progress = Math.min(1.0f, Math.max(0.0f, progress));

                // Calculate height of the cooldown box
                int boxHeight = (int) (progress * ICON_SIZE);

                boxHeight = Math.min(ICON_SIZE, Math.max(0, boxHeight));

                // Calculate the top edge of the overlay
                // If boxHeight is 16, top is iconY (Full Cover)
                // If boxHeight is 0, top is iconY + 16 (No Cover)
                int boxTop = iconY + (ICON_SIZE - boxHeight);
                int boxBottom = iconY + ICON_SIZE;

                // Draw the "Cooldown Shutter"
                graphics.fill(iconX, boxTop, iconX + ICON_SIZE, boxBottom, 0x80FFFFFF);

                RenderSystem.enableDepthTest();
            }
        }
    }

    private ItemStack getPlaceholderIcon(String abilityId) {
        return switch (abilityId) {
            case "dash" -> new ItemStack(Items.FEATHER);
            case "blade_dance" -> new ItemStack(Items.IRON_SWORD);
            case "parry" -> new ItemStack(Items.SHIELD);
            case "blade_waltz" -> new ItemStack(Items.DIAMOND_SWORD);
            default -> new ItemStack(Items.BARRIER);
        };
    }
}