package net.Frostimpact.rpgclasses.rpg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
// CRITICAL: This must be the NeoForge specific import
import net.neoforged.neoforge.common.util.INBTSerializable;

// CRITICAL: You must add "implements INBTSerializable<CompoundTag>"
public class PlayerRPGData implements INBTSerializable<CompoundTag> {

    // --- Variables ---
    private String currentClass = "NONE";
    private int mana = 100;
    private int maxMana = 100;
    private int dashCooldown = 0;

    public PlayerRPGData() {
        // Default constructor
    }

    // --- Getters & Setters ---
    public String getCurrentClass() {
        return currentClass;
    }

    public void setCurrentClass(String currentClass) {
        this.currentClass = currentClass;
    }

    public int getMana() {
        return mana;
    }

    public void useMana(int amount) {
        this.mana = Math.max(0, this.mana - amount);
    }

    public int getDashCooldown() {
        return dashCooldown;
    }

    public void setDashCooldown(int dashCooldown) {
        this.dashCooldown = dashCooldown;
    }

    // --- Saving (Required by INBTSerializable) ---
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("rpg_class", currentClass);
        nbt.putInt("mana", mana);
        nbt.putInt("dash_cooldown", dashCooldown);
        return nbt;
    }

    // --- Loading (Required by INBTSerializable) ---
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("rpg_class")) {
            this.currentClass = nbt.getString("rpg_class");
        }
        if (nbt.contains("mana")) {
            this.mana = nbt.getInt("mana");
        }
        if (nbt.contains("dash_cooldown")) {
            this.dashCooldown = nbt.getInt("dash_cooldown");
        }
    }
}