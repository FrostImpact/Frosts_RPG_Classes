package net.Frostimpact.rpgclasses.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketSelectClass;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Added for cleaner error handling if data is missing

public class ClassSelectionScreen extends Screen {

    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 400;
    private static final int BUTTON_HEIGHT = 30;
    private static final int SPACING = 5;

    private String currentClass = "NONE";
    private ClassType selectedType = null;
    private String hoveredSpec = null;

    public ClassSelectionScreen() {
        super(Component.literal("Class Selection"));
    }

    @Override
    protected void init() {
        super.init();

        // **FIXED: Use Optional to safely get player data and store current class.**
        if (Minecraft.getInstance().player != null) {
            Optional<PlayerRPGData> rpg = Minecraft.getInstance().player.getExistingData(ModAttachments.PLAYER_RPG);
            currentClass = rpg.map(PlayerRPGData::getCurrentClass).orElse("NONE");
        }

        // **FIXED: Explicitly disable post-processing/blur effect ONCE upon screen open.**
        // This is done to ensure the world behind is not blurred.
        //if (this.minecraft != null && this.minecraft.levelRenderer != null) {
            //this.minecraft.levelRenderer.postEffectUnbind();
        //}
    }

    @Override
    public void onClose() {
        super.onClose();
        // **FIXED: DO NOT re-enable the blur here.**
        // Vanilla handles re-enabling the blur/post-effect when the *next* screen/menu requires it (like the pause menu).
        // Calling postEffectUnbind() here is incorrect.
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // **FIXED: Override to prevent default background blur/dim effect - render nothing, as we handle the background in render()**
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // **FIXED: Consolidated and Corrected Render Logic**

        // 1. Render a solid dark translucent background (0xC0000000 is 75% opaque black)
        graphics.fill(0, 0, this.width, this.height, 0xC0000000);
        //

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Title
        graphics.drawCenteredString(this.font, "§6§lCHOOSE YOUR CLASS",
                centerX, 20, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7Current: §f" + currentClass,
                centerX, 35, 0xAAAAAA);

        if (selectedType == null) {
            // Show class type selection
            renderClassTypes(graphics, centerX, centerY, mouseX, mouseY);
        } else {
            // Show specializations for selected type
            renderSpecializations(graphics, centerX, centerY, mouseX, mouseY);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderClassTypes(GuiGraphics graphics, int centerX, int centerY, int mouseX, int mouseY) {
        int startY = centerY - 100;
        int index = 0;

        for (ClassType type : ClassType.values()) {
            int y = startY + (index * (BUTTON_HEIGHT + SPACING));
            int x = centerX - PANEL_WIDTH / 2;

            boolean isHovered = mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                    mouseY >= y && mouseY <= y + BUTTON_HEIGHT;

            // Draw button background
            int bgColor = isHovered ? 0x80404040 : 0x80202020;
            graphics.fill(x, y, x + PANEL_WIDTH, y + BUTTON_HEIGHT, bgColor);

            // Draw border
            int borderColor = type.color | 0xFF000000;
            drawInnerBorder(graphics, x, y, PANEL_WIDTH, BUTTON_HEIGHT, borderColor);

            // Draw icon and text
            // Note: The y positions here (y+10 and y+20) work for a 30px high button.
            graphics.drawString(this.font, type.icon + " §l" + type.displayName,
                    x + 10, y + 6, type.color, false); // Adjusted y slightly for better centering

            graphics.drawString(this.font, "§7" + type.description,
                    x + 10, y + 18, 0x888888, false); // Adjusted y slightly

            index++;
        }

        // Instructions
        graphics.drawCenteredString(this.font, "§7Click a class type to view specializations",
                centerX, this.height - 30, 0x888888);
    }

    private void renderSpecializations(GuiGraphics graphics, int centerX, int centerY, int mouseX, int mouseY) {
        // Back button area calculation (Improved: Defines explicit area)
        int backButtonY = centerY - 150;
        int backButtonWidth = 80;
        int backButtonHeight = 20;
        int backButtonX = centerX - backButtonWidth / 2;

        boolean backButtonHovered = mouseX >= backButtonX && mouseX <= backButtonX + backButtonWidth &&
                mouseY >= backButtonY && mouseY <= backButtonY + backButtonHeight;

        // Draw Back Button Background
        int backBgColor = backButtonHovered ? 0x80303030 : 0x80101010;
        graphics.fill(backButtonX, backButtonY, backButtonX + backButtonWidth, backButtonY + backButtonHeight, backBgColor);

        // Draw Back Button Border
        drawInnerBorder(graphics, backButtonX, backButtonY, backButtonWidth, backButtonHeight, backButtonHovered ? 0xFF999999 : 0xFF444444);

        // Draw Back Button Text
        graphics.drawCenteredString(this.font, backButtonHovered ? "§e← Back" : "§7← Back",
                centerX, backButtonY + 6, backButtonHovered ? 0xFFFF55 : 0x888888);


        // Title for selected type
        graphics.drawCenteredString(this.font,
                selectedType.icon + " §l" + selectedType.displayName.toUpperCase(),
                centerX, centerY - 120, selectedType.color);

        // Specializations side by side
        ClassSpec[] specs = selectedType.specs;
        // Adjusted total width to match your original calculation: (PANEL_WIDTH * specs.length) + ((specs.length - 1) * 20)
        int panelSpacing = 20;
        int totalWidth = (PANEL_WIDTH * specs.length) + ((specs.length - 1) * panelSpacing);
        int startX = centerX - totalWidth / 2;
        int panelY = centerY - 90;

        hoveredSpec = null;

        for (int i = 0; i < specs.length; i++) {
            ClassSpec spec = specs[i];
            int x = startX + (i * (PANEL_WIDTH + panelSpacing));

            boolean isHovered = mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                    mouseY >= panelY && mouseY <= panelY + PANEL_HEIGHT;

            if (isHovered) {
                hoveredSpec = spec.className;
            }

            boolean isCurrent = spec.className.equals(currentClass);

            renderSpecPanel(graphics, spec, x, panelY, isHovered, isCurrent);
        }
    }

    private void renderSpecPanel(GuiGraphics graphics, ClassSpec spec, int x, int y,
                                 boolean isHovered, boolean isCurrent) {
        // Background
        int bgColor = isHovered ? 0xD0202020 : 0xC0101010;
        graphics.fill(x, y, x + PANEL_WIDTH, y + PANEL_HEIGHT, bgColor);
        //

        // Border
        int borderColor;
        if (isCurrent) {
            borderColor = 0xFFFFD700; // Gold for current class
        } else if (isHovered) {
            borderColor = selectedType.color | 0xFF000000;
        } else {
            borderColor = 0xFF404040;
        }
        drawInnerBorder(graphics, x, y, PANEL_WIDTH, PANEL_HEIGHT, borderColor);

        int contentX = x + 10;
        int contentY = y + 10;

        // Class name
        String displayName = spec.displayName + (isCurrent ? " §7(Current)" : "");
        graphics.drawString(this.font, "§l" + displayName,
                contentX, contentY, selectedType.color, false);
        contentY += 15;

        // Weapon type
        graphics.drawString(this.font, "§7Weapon: §f" + spec.weaponType,
                contentX, contentY, 0xAAAAAA, false);
        contentY += 12;

        // Overview
        String overview = spec.overview;
        List<String> wrappedOverview = wrapText(overview, PANEL_WIDTH - 20);
        for (String line : wrappedOverview) {
            graphics.drawString(this.font, "§7" + line,
                    contentX, contentY, 0x888888, false);
            contentY += 10;
        }
        contentY += 5;

        // Passive
        graphics.drawString(this.font, "§6Passive: §f" + spec.passiveName,
                contentX, contentY, 0xFFAA00, false);
        contentY += 12;

        List<String> wrappedPassive = wrapText(spec.passiveDesc, PANEL_WIDTH - 20);
        for (String line : wrappedPassive) {
            graphics.drawString(this.font, "§7" + line,
                    contentX, contentY, 0x777777, false);
            contentY += 9;
        }
        contentY += 8;

        // Abilities
        graphics.drawString(this.font, "§bAbilities:",
                contentX, contentY, 0x55AAFF, false);
        contentY += 12;

        for (int i = 0; i < spec.abilities.length; i++) {
            Ability ability = spec.abilities[i];
            if (ability == null) continue;

            graphics.drawString(this.font, "§e" + (i + 1) + ". " + ability.name,
                    contentX, contentY, 0xFFFF55, false);
            contentY += 10;

            if (ability.description != null && !ability.description.isEmpty()) {
                List<String> wrappedDesc = wrapText(ability.description, PANEL_WIDTH - 30);
                for (String line : wrappedDesc) {
                    graphics.drawString(this.font, "§7  " + line,
                            contentX, contentY, 0x666666, false);
                    contentY += 9;
                }
            }
            contentY += 3;
        }

        // Select button at bottom (only renders on hover)
        if (isHovered) {
            int buttonY = y + PANEL_HEIGHT - 35;
            int buttonWidth = 100;
            int buttonHeight = 25;
            int buttonX = x + PANEL_WIDTH / 2 - buttonWidth / 2;

            // Highlight color changes for current vs selectable
            int selectColor = isCurrent ? 0xFF888888 : 0xFF008800; // Use a darker green for selected state
            String selectText = isCurrent ? "§8§lCURRENT" : "§f§lSELECT";

            graphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, selectColor);

            // Draw a border around the select button
            drawInnerBorder(graphics, buttonX, buttonY, buttonWidth, buttonHeight, isCurrent ? 0xFF444444 : 0xFF00FF00);

            graphics.drawCenteredString(this.font, selectText,
                    x + PANEL_WIDTH / 2, buttonY + 8, 0xFFFFFF);
        }
    }

    // **FIXED: drawBorder method name changed and implementation corrected for a 1px inner border**
    private void drawInnerBorder(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        // Top line
        graphics.fill(x, y, x + width, y + 1, color);
        // Bottom line
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        // Left line
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);
        // Right line
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // Check width of currentLine + " " + word
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (this.font.width(testLine) <= maxWidth) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                // If it's too long, finalize the current line
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                // Start a new line with the current word
                currentLine = new StringBuilder(word);
            }
        }
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        return lines;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (selectedType == null) {
            // Check class type clicks
            int startY = centerY - 100;
            int index = 0;

            for (ClassType type : ClassType.values()) {
                int y = startY + (index * (BUTTON_HEIGHT + SPACING));
                int x = centerX - PANEL_WIDTH / 2;

                if (mouseX >= x && mouseX <= x + PANEL_WIDTH &&
                        mouseY >= y && mouseY <= y + BUTTON_HEIGHT) {
                    // Left click only (button == 0)
                    if (button == 0) {
                        selectedType = type;
                        return true;
                    }
                }
                index++;
            }
        } else {
            // Check back button
            int backButtonY = centerY - 150;
            int backButtonWidth = 80;
            int backButtonHeight = 20;
            int backButtonX = centerX - backButtonWidth / 2;

            if (mouseX >= backButtonX && mouseX <= backButtonX + backButtonWidth &&
                    mouseY >= backButtonY && mouseY <= backButtonY + backButtonHeight) {
                // Left click only
                if (button == 0) {
                    selectedType = null;
                    return true;
                }
            }

            // Check specialization selection
            if (hoveredSpec != null && !hoveredSpec.equals(currentClass)) {

                // You were checking if a spec was hovered, but not if the SELECT button area was clicked.
                // To keep it simple based on your existing logic (click anywhere on the hovered panel),
                // we'll assume a click on the hovered panel means selection.
                if (button == 0) {
                    ModMessages.sendToServer(new PacketSelectClass(hoveredSpec));
                    this.onClose();
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // **NOTE: Data structures remain untouched as they were correct.**
    // === CLASS DATA STRUCTURES ===

    private enum ClassType {
        // ... (ClassType definitions remain the same)
        DAMAGE("Damage", "⚔", 0xFF4444, "High risk, high reward melee fighters",
                new ClassSpec[]{
                        new ClassSpec("BERSERKER", "Berserker", "Great Swords, Axes",
                                "Stereotypical RPG class. Lower HP = more damage with lifesteal element.",
                                "RAGE", "Gain Strength II when below 8 HP (4 hearts)",
                                new Ability[]{
                                        new Ability("Coming Soon", "Abilities not yet implemented"),
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", "")
                                }),
                        new ClassSpec("BLADEDANCER", "Bladedancer", "Daggers, Swords",
                                "Relies on combos to deal high damage while having evasion + dashes.",
                                "TEMPO", "Every 3 hits, gain Strength I lasting until the 4th hit.",
                                new Ability[]{
                                        new Ability("DASH", "Dash forward 5 blocks. 5s CD / 15 MP"),
                                        new Ability("BLADE DANCE", "Summon 4 blades rotating around you, dealing damage. Lasts 4s. Each hit removes 1 blade, healing 2 HP. 12s CD / 35 MP"),
                                        new Ability("PARRY", "Brief I-frames for 0.4s. Successfully parrying refreshes DASH and grants 3 TEMPO. 6s CD / 15 MP"),
                                        new Ability("FINAL WALTZ", "TEMPO overflows, applying Strength II at 6 stacks. Hits don't reset. 30s CD / 50 MP")
                                })
                }),
        TANK("Tank", "🛡", 0x55AAFF, "Absorb damage and protect allies",
                new ClassSpec[]{
                        new ClassSpec("BASTION", "Bastion", "Great Swords, Axes, Shields",
                                "Typical tank with high defense, high HP, and low damage. Abilities to aggro and stun.",
                                "Coming Soon", "Passive not yet implemented",
                                new Ability[]{
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", "")
                                }),
                        new ClassSpec("JUGGERNAUT", "Juggernaut", "Great Swords, Axes, Shields",
                                "Switch stance between defense and offense. Generate charge when tanking for burst damage.",
                                "INERTIA", "Accumulate CHARGE taking damage in SHIELD. Release as AOE in SHATTER. CHARGE decays in 12s. Strength bonuses at 50+ and 0+ CHARGE.",
                                new Ability[]{
                                        new Ability("SWAP", "Swap SHIELD/SHATTER mode. SHIELD at 50+ CHARGE grants 6 absorption hearts. 3s CD / 10 MP"),
                                        new Ability("CRUSH", "Smash ground for AOE damage. SHATTER: consume 10 CHARGE for bonus damage + slow. 5s CD / 25 MP"),
                                        new Ability("FORTIFY", "Gain Resistance I for 5s. SHIELD: +10 Charge per hit but Slowness I. 10s CD / 25 MP"),
                                        new Ability("LEAP", "Jump 7 blocks forward. SHIELD: grant allies 6 Absorption. SHATTER: 3.5s CD, consume 15 CHARGE. 12s CD / 35 MP")
                                })
                }),
        RANGED("Ranged", "🏹", 0x55FF55, "Consistent damage from afar with mobility",
                new ClassSpec[]{
                        new ClassSpec("MARKSMAN", "Marksman", "Bows, Shortbows",
                                "Single-target DPS with escape/movement options and aerial combat.",
                                "GLIDE + AERIAL AFFINITY", "Slow Falling I in air. Gain SEEKER charges (max 5) while midair. Abilities grant 1 charge.",
                                new Ability[]{
                                        new Ability("SEEKERS", "Release projectiles based on charges (1 per charge). 5s CD / (5 × charges) MP"),
                                        new Ability("VAULT", "Launch forward and lob projectile. Hit resets UPDRAFT CD. 8s CD / 15 MP"),
                                        new Ability("UPDRAFT", "Launch yourself upwards. 12s CD / 15 MP"),
                                        new Ability("ARROW RAIN", "Summon arrow storm in AOE dealing constant damage. 15s CD / 30 MP")
                                }),
                        new ClassSpec("MERCENARY", "Mercenary", "Crossbows",
                                "Precisely timed strikes with single-target CC and stealth positioning.",
                                "CLOAK + PREDATOR EYE", "Shift to turn invisible (7s CD / 10 MP). Attacking ends CLOAK. PARALYSIS enemies are highlighted.",
                                new Ability[]{
                                        new Ability("STUN BOLT", "Fire skillshot inflicting PARALYSIS. 6s CD / 15 MP"),
                                        new Ability("CYCLE QUIVER", "Switch between QUILL, PYRE, SPORE arrows. Special arrows: 15 MP/shot. 2s CD / 0 MP"),
                                        new Ability("HIRED GUN", "Target enemy, kill in 20s to reset all CDs + Speed III. 40s CD / 40 MP"),
                                        new Ability("PRECISION SHOT", "Charge slow piercing projectile. Damage increases with range. 12s CD / 30 MP")
                                }),
                        new ClassSpec("MIRAGE", "Mirage", "Swords, Bows",
                                "Clone-based repositioning and safe engagement using AFTERIMAGES.",
                                "ESSENCE OF CREATION", "All AFTERIMAGES mimic your direction and attacks. Deals 30% damage.",
                                new Ability[]{
                                        new Ability("REFLECTIONS", "Spawn AFTERIMAGE that glides forward (max 10 blocks). Max 3. 4s CD / 15 MP"),
                                        new Ability("SHADOWSTEP", "Teleport to closest AFTERIMAGE in line of sight. 4s reactivation window. 12s CD / 10 MP"),
                                        new Ability("RECALL", "All AFTERIMAGES glide to your cast position. 8s CD / 20 MP"),
                                        new Ability("FRACTURE LINE", "Charge and dash forward. AFTERIMAGES in path explode after 1.5s. 20s CD / 40 MP")
                                })
                }),
        BURST("Burst/Control", "✨", 0xAA00FF, "High burst damage and crowd control",
                new ClassSpec[]{
                        new ClassSpec("MANAFORGE", "Manaforge", "Staffs",
                                "Maximize burst potential with low mobility. High risk, high reward with decent AOE control.",
                                "ACCUMULATION", "Gain ARCANA while dealing damage. ARCANA decays at 100 or if no attack for 10s.",
                                new Ability[]{
                                        new Ability("MAGIC MISSILE", "Fire projectiles (1 + ARCANA/25, max 5). Consumes ARCANA. 2s CD / 10 MP"),
                                        new Ability("SURGE", "Charge energy beam for massive damage. Consume ARCANA to prolong charge. 20s CD / 40 MP"),
                                        new Ability("OPEN RIFT", "Create rift pulling in enemies. 15s CD / 35 MP"),
                                        new Ability("COALESCENCE", "All damage for 6s converts to ARCANA. 25s CD / 30 MP")
                                }),
                        new ClassSpec("REFRACTION", "Refraction", "Staffs",
                                "Low damage potential, high mobility and utility focused caster.",
                                "Coming Soon", "Passive not yet implemented",
                                new Ability[]{
                                        new Ability("Coming Soon", ""),
                                        new Ability("PORTAL", "Place mirror at location. Reactivate for 2nd mirror. Go through to teleport. 12s CD / 30 MP"),
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", "")
                                })
                }),
        SUPPORT("Support", "❤", 0xFFAA00, "Offensive support with utility",
                new ClassSpec[]{
                        new ClassSpec("BATTLE_PRIEST", "Battle Priest", "Staffs, Maces",
                                "Not your typical healer - offensive support with combat abilities.",
                                "Coming Soon", "Passive not yet implemented",
                                new Ability[]{
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", "")
                                }),
                        new ClassSpec("ALCHEMIST", "Alchemist", "Potions, Throwables",
                                "Concocts powerful brews and explosives for offensive support.",
                                "Coming Soon", "Passive not yet implemented",
                                new Ability[]{
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", ""),
                                        new Ability("Coming Soon", "")
                                })
                }),

        SUMMON("Summon", "👑", 0xFFD700, "Command armies and control the battlefield",
                new ClassSpec[]{
                        new ClassSpec("RULER", "Ruler", "Any",
                                "Generic summoner. Summons troops while staying in combat to utilize kit.",
                                "KING'S BANNER + DEMORALIZE",
                                "Banner spawns on user creating golden circle. Summons outside circle become DEMORALIZED and die after 10s. GLORY prevents DEMORALIZE.",
                                new Ability[]{
                                        new Ability("CALL TO ARMS", "Summon 2 knights (max 4). Shift: summon 2 archers (max 4). 12s CD / 20 MP"),
                                        new Ability("INVIGORATE", "Grant GLORY to summons in banner radius for 10s. GLORY: +20% Speed, +4 hearts, prevents DEMORALIZE. 20s CD / 40 MP"),
                                        new Ability("REGROUP", "All summons move to banner, pausing death timers. Heal 50% HP on arrival. 15s CD / 30 MP"),
                                        new Ability("RALLY", "Place banner for 12s. Reactivate to recall. 25s CD / 10 MP")
                                }),
                        new ClassSpec("ARTIFICER", "Artificer", "Any",
                                "Summons stationary turrets and towers for area control and utility.",
                                "RESTORATION + VISION", "Hitting turrets/towers with reset attack heals them. All summons glow.",
                                new Ability[]{
                                        new Ability("TURRET", "Construct turret for 3s. Shoots pellets at enemies. Max 3. Decay after 30s. 7s CD / 20 MP"),
                                        new Ability("TOWER", "Construct SHOCK tower for 5s (slows). Shift: WIND tower (knockback). Max 2 total. 14s CD / 20 MP"),
                                        new Ability("REPOSITION", "Teleport to closest summon in line of sight. 4s CD / 10 MP"),
                                        new Ability("SELF-DESTRUCT", "Kill all summons, causing AOE explosions. 20s CD / 40 MP")
                                })

                });


        final String displayName;
        final String icon;
        final int color;
        final String description;
        final ClassSpec[] specs;

        ClassType(String displayName, String icon, int color, String description, ClassSpec[] specs) {
            this.displayName = displayName;
            this.icon = icon;
            this.color = color;
            this.description = description;
            this.specs = specs;
        }
    }

    private static class ClassSpec {
        final String className;
        final String displayName;
        final String weaponType;
        final String overview;
        final String passiveName;
        final String passiveDesc;
        final Ability[] abilities;

        ClassSpec(String className, String displayName, String weaponType, String overview,
                  String passiveName, String passiveDesc, Ability[] abilities) {
            this.className = className;
            this.displayName = displayName;
            this.weaponType = weaponType;
            this.overview = overview;
            this.passiveName = passiveName;
            this.passiveDesc = passiveDesc;
            this.abilities = abilities;
        }
    }

    private static class Ability {
        final String name;
        final String description;

        Ability(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}