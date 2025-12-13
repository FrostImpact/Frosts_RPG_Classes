package net.Frostimpact.rpgclasses.rpg;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Numerical stat system for players.
 * Replaces potion effects with direct stat modifications.
 */
public class PlayerStats implements INBTSerializable<CompoundTag> {

    // Base Stats (modified by class/equipment)
    private double maxHealth = 20.0;
    private double currentHealth = 20.0;
    private double baseDamage = 1.0; // Multiplier for all damage dealt
    private double baseDefense = 0.0; // Flat damage reduction
    private double baseSpeed = 1.0; // Movement speed multiplier
    private double baseAttackSpeed = 1.0; // Attack speed multiplier
    private double critChance = 0.0; // 0.0 to 1.0 (0% to 100%)
    private double critDamage = 1.5; // Multiplier for critical hits

    // Temporary Buffs/Debuffs (applied by abilities, expire over time)
    private Map<String, StatModifier> activeModifiers = new HashMap<>();

    // === STAT MODIFIERS ===
    public static class StatModifier {
        public final String id;
        public final StatType statType;
        public final double value;
        public final ModifierType modifierType;
        public int duration; // Ticks remaining

        public StatModifier(String id, StatType statType, double value, ModifierType modifierType, int duration) {
            this.id = id;
            this.statType = statType;
            this.value = value;
            this.modifierType = modifierType;
            this.duration = duration;
        }

        public void tick() {
            if (duration > 0) {
                duration--;
            }
        }

        public boolean isExpired() {
            return duration <= 0;
        }
    }

    public enum StatType {
        MAX_HEALTH,
        DAMAGE,
        DEFENSE,
        SPEED,
        ATTACK_SPEED,
        CRIT_CHANCE,
        CRIT_DAMAGE
    }

    public enum ModifierType {
        FLAT,       // Adds/subtracts a flat amount
        MULTIPLY    // Multiplies by a percentage (1.5 = +50%)
    }

    // === ADD/REMOVE MODIFIERS ===
    public void addModifier(StatModifier modifier) {
        activeModifiers.put(modifier.id, modifier);
    }

    public void removeModifier(String id) {
        activeModifiers.remove(id);
    }

    public void clearAllModifiers() {
        activeModifiers.clear();
    }

    public void tickModifiers() {
        activeModifiers.values().forEach(StatModifier::tick);
        activeModifiers.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // === CALCULATED STATS (Base + Modifiers) ===
    public double getMaxHealth() {
        return calculateStat(StatType.MAX_HEALTH, maxHealth);
    }

    public double getCurrentHealth() {
        return Math.min(currentHealth, getMaxHealth());
    }

    public void setCurrentHealth(double health) {
        this.currentHealth = Math.max(0, Math.min(health, getMaxHealth()));
    }

    public void heal(double amount) {
        setCurrentHealth(currentHealth + amount);
    }

    public void damage(double amount) {
        setCurrentHealth(currentHealth - amount);
    }

    public double getDamageMultiplier() {
        return calculateStat(StatType.DAMAGE, baseDamage);
    }

    public double getDefense() {
        return calculateStat(StatType.DEFENSE, baseDefense);
    }

    public double getSpeedMultiplier() {
        return calculateStat(StatType.SPEED, baseSpeed);
    }

    public double getAttackSpeedMultiplier() {
        return calculateStat(StatType.ATTACK_SPEED, baseAttackSpeed);
    }

    public double getCritChance() {
        return Math.min(1.0, calculateStat(StatType.CRIT_CHANCE, critChance));
    }

    public double getCritDamage() {
        return calculateStat(StatType.CRIT_DAMAGE, critDamage);
    }

    // === HELPER: Calculate Final Stat Value ===
    private double calculateStat(StatType statType, double baseValue) {
        double flatBonus = 0.0;
        double multiplyBonus = 1.0;

        for (StatModifier mod : activeModifiers.values()) {
            if (mod.statType == statType) {
                if (mod.modifierType == ModifierType.FLAT) {
                    flatBonus += mod.value;
                } else if (mod.modifierType == ModifierType.MULTIPLY) {
                    multiplyBonus *= mod.value;
                }
            }
        }

        return (baseValue + flatBonus) * multiplyBonus;
    }

    // === BASE STAT SETTERS ===
    public void setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    public void setBaseDamage(double damage) {
        this.baseDamage = damage;
    }

    public void setBaseDefense(double defense) {
        this.baseDefense = defense;
    }

    public void setBaseSpeed(double speed) {
        this.baseSpeed = speed;
    }

    public void setBaseAttackSpeed(double attackSpeed) {
        this.baseAttackSpeed = attackSpeed;
    }

    public void setCritChance(double chance) {
        this.critChance = Math.max(0.0, Math.min(1.0, chance));
    }

    public void setCritDamage(double damage) {
        this.critDamage = damage;
    }

    // === NBT SERIALIZATION ===
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        
        nbt.putDouble("max_health", maxHealth);
        nbt.putDouble("current_health", currentHealth);
        nbt.putDouble("base_damage", baseDamage);
        nbt.putDouble("base_defense", baseDefense);
        nbt.putDouble("base_speed", baseSpeed);
        nbt.putDouble("base_attack_speed", baseAttackSpeed);
        nbt.putDouble("crit_chance", critChance);
        nbt.putDouble("crit_damage", critDamage);

        // Save active modifiers
        CompoundTag modsTag = new CompoundTag();
        for (Map.Entry<String, StatModifier> entry : activeModifiers.entrySet()) {
            CompoundTag modTag = new CompoundTag();
            StatModifier mod = entry.getValue();
            modTag.putString("stat_type", mod.statType.name());
            modTag.putDouble("value", mod.value);
            modTag.putString("modifier_type", mod.modifierType.name());
            modTag.putInt("duration", mod.duration);
            modsTag.put(entry.getKey(), modTag);
        }
        nbt.put("modifiers", modsTag);

        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if (nbt.contains("max_health")) maxHealth = nbt.getDouble("max_health");
        if (nbt.contains("current_health")) currentHealth = nbt.getDouble("current_health");
        if (nbt.contains("base_damage")) baseDamage = nbt.getDouble("base_damage");
        if (nbt.contains("base_defense")) baseDefense = nbt.getDouble("base_defense");
        if (nbt.contains("base_speed")) baseSpeed = nbt.getDouble("base_speed");
        if (nbt.contains("base_attack_speed")) baseAttackSpeed = nbt.getDouble("base_attack_speed");
        if (nbt.contains("crit_chance")) critChance = nbt.getDouble("crit_chance");
        if (nbt.contains("crit_damage")) critDamage = nbt.getDouble("crit_damage");

        // Load modifiers
        if (nbt.contains("modifiers")) {
            CompoundTag modsTag = nbt.getCompound("modifiers");
            activeModifiers.clear();
            for (String key : modsTag.getAllKeys()) {
                CompoundTag modTag = modsTag.getCompound(key);
                StatType statType = StatType.valueOf(modTag.getString("stat_type"));
                double value = modTag.getDouble("value");
                ModifierType modType = ModifierType.valueOf(modTag.getString("modifier_type"));
                int duration = modTag.getInt("duration");
                
                activeModifiers.put(key, new StatModifier(key, statType, value, modType, duration));
            }
        }
    }
}