package net.Frostimpact.rpgclasses.rpg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public class PlayerRPGData implements INBTSerializable<CompoundTag> {

    // --- Variables ---
    private String currentClass = "NONE";
    private int mana = 100;
    private int maxMana = 100;
    private Map<String, Integer> abilityCooldowns = new HashMap<>();

    private boolean dashActive = false;
    private Vec3 dashStartPos = Vec3.ZERO;
    private Vec3 dashDirection = Vec3.ZERO;
    private double dashTargetDistance = 0;

    public boolean isDashActive() { return dashActive; }
    public void setDashActive(boolean active) { this.dashActive = active; }
    public Vec3 getDashStartPos() { return dashStartPos; }
    public void setDashStartPos(Vec3 pos) { this.dashStartPos = pos; }
    public Vec3 getDashDirection() { return dashDirection; }
    public void setDashDirection(Vec3 dir) { this.dashDirection = dir; }
    public double getDashTargetDistance() { return dashTargetDistance; }
    public void setDashTargetDistance(double dist) { this.dashTargetDistance = dist; }

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

    // --- Ability Cooldowns ---
    public int getAbilityCooldown(String abilityId) {
        return abilityCooldowns.getOrDefault(abilityId, 0);
    }

    public void setAbilityCooldown(String abilityId, int ticks) {
        if (ticks <= 0) {
            abilityCooldowns.remove(abilityId);
        } else {
            abilityCooldowns.put(abilityId, ticks);
        }
    }

    public void tickCooldowns() {
        abilityCooldowns.replaceAll((id, cooldown) -> cooldown - 1);
        abilityCooldowns.entrySet().removeIf(entry -> entry.getValue() <= 0);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {

        CompoundTag nbt = new CompoundTag();
        nbt.putString("rpg_class", currentClass);
        nbt.putInt("mana", mana);
        nbt.putInt("max_mana", maxMana);
        nbt.putBoolean("dash_active", dashActive);
        nbt.putDouble("dash_target", dashTargetDistance);

        // Save cooldowns
        CompoundTag cooldownsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : abilityCooldowns.entrySet()) {
            cooldownsTag.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("cooldowns", cooldownsTag);

        return nbt;


    }

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

        if (nbt.contains("dash_active")) {
            this.dashActive = nbt.getBoolean("dash_active");
        }
        if (nbt.contains("dash_target")) {
            this.dashTargetDistance = nbt.getDouble("dash_target");
        }

        // Load cooldowns
        if (nbt.contains("cooldowns")) {
            CompoundTag cooldownsTag = nbt.getCompound("cooldowns");
            abilityCooldowns.clear();
            for (String key : cooldownsTag.getAllKeys()) {
                abilityCooldowns.put(key, cooldownsTag.getInt(key));
            }
        }
    }




}