package net.Frostimpact.rpgclasses.rpg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.util.INBTSerializable;

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

    public void setMana(int mana) {
        this.mana = Math.max(0, Math.min(mana, maxMana));
    }

    public void useMana(int amount) {
        this.mana = Math.max(0, Math.min(this.mana - amount, maxMana));
    }

    public int getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
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
        nbt.putInt("max_mana", maxMana);
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
        if (nbt.contains("max_mana")) {
            this.maxMana = nbt.getInt("max_mana");
        }
        if (nbt.contains("dash_cooldown")) {
            this.dashCooldown = nbt.getInt("dash_cooldown");
        }
    }
}