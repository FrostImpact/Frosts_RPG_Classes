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
    }

    private static final int ICON_SIZE = 16;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_SPACING = 22;

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

        int startX = (screenWidth / 2) + 95;
        int startY = screenHeight - 22;

        for (int i = 0; i < trackedAbilities.length; i++) {
            String abilityId = trackedAbilities[i];
            Ability ability = AbilityRegistry.getAbility(abilityId);
            if (ability == null) continue;

            int slotX = startX + (i * SLOT_SPACING);
            int slotY = startY;

            int iconX = slotX + 1;
            int iconY = slotY + 1;

            int cooldownTicks = rpg.getAbilityCooldown(abilityId);
            int maxCooldown = ability.getCooldownTicks();
            int manaCost = ability.getManaCost();
            int currentMana = rpg.getMana();

            // Draw Slot Background
            graphics.fill(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, 0x90000000);
            graphics.renderOutline(slotX, slotY, SLOT_SIZE, SLOT_SIZE, 0xFF333333);

            // Render Icon
            ItemStack icon = getAbilityIcon(currentClass, abilityId);
            graphics.renderItem(icon, iconX, iconY);

            // Render Mana Check
            if (currentMana < manaCost) {
                RenderSystem.disableDepthTest();
                graphics.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, 0x600000AA);
                RenderSystem.enableDepthTest();
            }

            // Render Cooldown
            if (cooldownTicks > 0 && maxCooldown > 0) {
                RenderSystem.disableDepthTest();

                float progress = (float) cooldownTicks / (float) maxCooldown;
                progress = Math.min(1.0f, Math.max(0.0f, progress));

                int boxHeight = (int) (progress * ICON_SIZE);
                boxHeight = Math.min(ICON_SIZE, Math.max(0, boxHeight));

                int boxTop = iconY + (ICON_SIZE - boxHeight);
                int boxBottom = iconY + ICON_SIZE;

                graphics.fill(iconX, boxTop, iconX + ICON_SIZE, boxBottom, 0x80FFFFFF);

                RenderSystem.enableDepthTest();
            }
        }
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

        return new ItemStack(Items.BARRIER);
    }



}