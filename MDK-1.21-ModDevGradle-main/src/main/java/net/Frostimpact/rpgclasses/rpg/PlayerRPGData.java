package net.Frostimpact.rpgclasses.rpg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public class PlayerRPGData implements INBTSerializable<CompoundTag> {

    // Variables
    private String currentClass = "NONE";
    private int mana = 100;
    private int maxMana = 100;
    private Map<String, Integer> abilityCooldowns = new HashMap<>();

    //DASH
    private boolean dashActive = false;
    private Vec3 dashStartPos = Vec3.ZERO;
    private Vec3 dashDirection = Vec3.ZERO;
    private double dashTargetDistance = 0;

    //BLADE DANCE
    private boolean bladeDanceActive = false;
    private int bladeDanceTicks = 0;
    private int bladeDanceBlades = 0;
    private int bladeDanceDamageCooldown = 0;

    private int tempoStacks = 0;
    private boolean tempoActive = false;

    //DASH
    public boolean isDashActive() { return dashActive; }
    public void setDashActive(boolean active) { this.dashActive = active; }
    public Vec3 getDashStartPos() { return dashStartPos; }
    public void setDashStartPos(Vec3 pos) { this.dashStartPos = pos; }
    public Vec3 getDashDirection() { return dashDirection; }
    public void setDashDirection(Vec3 dir) { this.dashDirection = dir; }
    public double getDashTargetDistance() { return dashTargetDistance; }
    public void setDashTargetDistance(double dist) { this.dashTargetDistance = dist; }

    //BLADE DANCE
    public boolean isBladeDanceActive() { return bladeDanceActive; }
    public void setBladeDanceActive(boolean active) { this.bladeDanceActive = active; }

    public int getBladeDanceTicks() { return bladeDanceTicks; }
    public void setBladeDanceTicks(int ticks) { this.bladeDanceTicks = ticks; }

    public int getBladeDanceBlades() { return bladeDanceBlades; }
    public void setBladeDanceBlades(int blades) { this.bladeDanceBlades = blades; }

    public int getBladeDanceDamageCooldown() { return bladeDanceDamageCooldown; }
    public void setBladeDanceDamageCooldown(int ticks) { this.bladeDanceDamageCooldown = ticks; }

    private java.util.List<Integer> bladeDanceSwordIds = new java.util.ArrayList<>();

    public java.util.List<Integer> getBladeDanceSwordIds() { return bladeDanceSwordIds; }
    public void setBladeDanceSwordIds(java.util.List<Integer> ids) { this.bladeDanceSwordIds = ids; }
    public void addBladeDanceSword(int entityId) { this.bladeDanceSwordIds.add(entityId); }
    public void clearBladeDanceSwords() { this.bladeDanceSwordIds.clear(); }

    public void removeBlade() {
        if (bladeDanceBlades > 0) {
            bladeDanceBlades--;
        }
    }

    //TEMPO
    public int getTempoStacks() { return tempoStacks; }
    public void setTempoStacks(int stacks) { this.tempoStacks = stacks; }

    public boolean isTempoActive() { return tempoActive; }
    public void setTempoActive(boolean active) { this.tempoActive = active; }

    public void addTempoStack() {
        this.tempoStacks++;
    }

    public void resetTempo() {
        this.tempoStacks = 0;
        this.tempoActive = false;
    }

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

        nbt.putBoolean("blade_dance_active", bladeDanceActive);
        nbt.putInt("blade_dance_ticks", bladeDanceTicks);
        nbt.putInt("blade_dance_blades", bladeDanceBlades);

        nbt.putInt("tempo_stacks", tempoStacks);
        nbt.putBoolean("tempo_active", tempoActive);

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

        if (nbt.contains("blade_dance_active")) {
            this.bladeDanceActive = nbt.getBoolean("blade_dance_active");
        }
        if (nbt.contains("blade_dance_ticks")) {
            this.bladeDanceTicks = nbt.getInt("blade_dance_ticks");
        }
        if (nbt.contains("blade_dance_blades")) {
            this.bladeDanceBlades = nbt.getInt("blade_dance_blades");
        }

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

        if (nbt.contains("tempo_stacks")) {
            this.tempoStacks = nbt.getInt("tempo_stacks");
        }
        if (nbt.contains("tempo_active")) {
            this.tempoActive = nbt.getBoolean("tempo_active");
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