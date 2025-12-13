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

    //TEMPO
    private int tempoStacks = 0;
    private boolean tempoActive = false;

    //PARRY
    private boolean parryActive = false;
    private int parryTicks = 0;
    private boolean parrySuccessful = false;

    //FINAL WALTZ
    private boolean finalWaltzActive = false;
    private int finalWaltzTicks = 0;
    private int finalWaltzOverflow = 0;

    //JUGGERNAUT - INERTIA PASSIVE
    private boolean juggernautShieldMode = true; // Start in SHIELD mode
    private int juggernautCharge = 0;
    private int juggernautMaxCharge = 100;
    private boolean chargeDecaying = false;
    private int chargeDecayTicks = 0;
    private static final int MAX_DECAY_TICKS = 240; // 12 seconds

    //JUGGERNAUT - FORTIFY
    private boolean fortifyActive = false;
    private int fortifyTicks = 0;

    //JUGGERNAUT - LEAP
    private boolean leapActive = false;
    private double leapStartY = 0;
    private boolean leapShieldMode = false;

    //JUGGERNAUT - CRUSH (New)
    private boolean crushActive = false;
    private boolean crushPowered = false;

    // MANAFORGE - ACCUMULATION PASSIVE
    private int manaforgeArcana = 0;
    private int manaforgeLastAttackTick = 0;

    // MANAFORGE - SURGE
    private boolean surgeActive = false;
    private int surgeTicks = 0;
    private int surgeChargeTime = 0;

    // MANAFORGE - OPEN RIFT
    private boolean riftActive = false;
    private int riftTicks = 0;
    private Vec3 riftPosition = Vec3.ZERO;

    // MANAFORGE - COALESCENCE
    private boolean coalescenceActive = false;
    private int coalescenceTicks = 0;
    private float coalescenceStoredDamage = 0;

    // MARKSMAN - AERIAL AFFINITY
    private int marksmanSeekerCharges = 0;
    private int marksmanAirborneTicks = 0;

    // MARKSMAN - ARROW RAIN
    private boolean arrowRainActive = false;
    private int arrowRainTicks = 0;
    private Vec3 arrowRainPosition = Vec3.ZERO;

    //DASH
    public boolean isDashActive() {
        return dashActive;
    }

    public void setDashActive(boolean active) {
        this.dashActive = active;
    }

    public Vec3 getDashStartPos() {
        return dashStartPos;
    }

    public void setDashStartPos(Vec3 pos) {
        this.dashStartPos = pos;
    }

    public Vec3 getDashDirection() {
        return dashDirection;
    }

    public void setDashDirection(Vec3 dir) {
        this.dashDirection = dir;
    }

    public double getDashTargetDistance() {
        return dashTargetDistance;
    }

    public void setDashTargetDistance(double dist) {
        this.dashTargetDistance = dist;
    }

    //BLADE DANCE
    public boolean isBladeDanceActive() {
        return bladeDanceActive;
    }

    public void setBladeDanceActive(boolean active) {
        this.bladeDanceActive = active;
    }

    public int getBladeDanceTicks() {
        return bladeDanceTicks;
    }

    public void setBladeDanceTicks(int ticks) {
        this.bladeDanceTicks = ticks;
    }

    public int getBladeDanceBlades() {
        return bladeDanceBlades;
    }

    public void setBladeDanceBlades(int blades) {
        this.bladeDanceBlades = blades;
    }

    public int getBladeDanceDamageCooldown() {
        return bladeDanceDamageCooldown;
    }

    public void setBladeDanceDamageCooldown(int ticks) {
        this.bladeDanceDamageCooldown = ticks;
    }

    private java.util.List<Integer> bladeDanceSwordIds = new java.util.ArrayList<>();

    public java.util.List<Integer> getBladeDanceSwordIds() {
        return bladeDanceSwordIds;
    }

    public void setBladeDanceSwordIds(java.util.List<Integer> ids) {
        this.bladeDanceSwordIds = ids;
    }

    public void addBladeDanceSword(int entityId) {
        this.bladeDanceSwordIds.add(entityId);
    }

    public void clearBladeDanceSwords() {
        this.bladeDanceSwordIds.clear();
    }

    public void removeBlade() {
        if (bladeDanceBlades > 0) {
            bladeDanceBlades--;
        }
    }

    // MARKSMAN - AERIAL AFFINITY
    public int getMarksmanSeekerCharges() {
        return marksmanSeekerCharges;
    }

    public void setMarksmanSeekerCharges(int charges) {
        this.marksmanSeekerCharges = Math.max(0, Math.min(charges, 5));
    }

    public void addMarksmanSeekerCharge() {
        this.marksmanSeekerCharges = Math.min(5, this.marksmanSeekerCharges + 1);
    }

    public int getMarksmanAirborneTicks() {
        return marksmanAirborneTicks;
    }

    public void setMarksmanAirborneTicks(int ticks) {
        this.marksmanAirborneTicks = ticks;
    }

    // MARKSMAN - ARROW RAIN
    public boolean isArrowRainActive() {
        return arrowRainActive;
    }

    public void setArrowRainActive(boolean active) {
        this.arrowRainActive = active;
    }

    public int getArrowRainTicks() {
        return arrowRainTicks;
    }

    public void setArrowRainTicks(int ticks) {
        this.arrowRainTicks = ticks;
    }

    public Vec3 getArrowRainPosition() {
        return arrowRainPosition;
    }

    public void setArrowRainPosition(Vec3 pos) {
        this.arrowRainPosition = pos;
    }

    //TEMPO
    public int getTempoStacks() {
        return tempoStacks;
    }

    public void setTempoStacks(int stacks) {
        this.tempoStacks = stacks;
    }

    public boolean isTempoActive() {
        return tempoActive;
    }

    public void setTempoActive(boolean active) {
        this.tempoActive = active;
    }

    public void addTempoStack() {
        this.tempoStacks++;
    }

    public void resetTempo() {
        this.tempoStacks = 0;
        this.tempoActive = false;
    }

    //PARRY
    public boolean isParryActive() {
        return parryActive;
    }

    public void setParryActive(boolean active) {
        this.parryActive = active;
    }

    public int getParryTicks() {
        return parryTicks;
    }

    public void setParryTicks(int ticks) {
        this.parryTicks = ticks;
    }

    public boolean isParrySuccessful() {
        return parrySuccessful;
    }

    public void setParrySuccessful(boolean successful) {
        this.parrySuccessful = successful;
    }

    //FINAL WALTZ
    public boolean isFinalWaltzActive() {
        return finalWaltzActive;
    }

    public void setFinalWaltzActive(boolean active) {
        this.finalWaltzActive = active;
    }

    public int getFinalWaltzTicks() {
        return finalWaltzTicks;
    }

    public void setFinalWaltzTicks(int ticks) {
        this.finalWaltzTicks = ticks;
    }

    public int getFinalWaltzOverflow() {
        return finalWaltzOverflow;
    }

    public void setFinalWaltzOverflow(int overflow) {
        this.finalWaltzOverflow = overflow;
    }

    //JUGGERNAUT - INERTIA
    public boolean isJuggernautShieldMode() {
        return juggernautShieldMode;
    }

    public void setJuggernautShieldMode(boolean mode) {
        this.juggernautShieldMode = mode;
    }

    public int getJuggernautCharge() {
        return juggernautCharge;
    }

    public void setJuggernautCharge(int charge) {
        this.juggernautCharge = Math.max(0, Math.min(charge, juggernautMaxCharge));
    }

    public void addJuggernautCharge(int amount) {
        setJuggernautCharge(this.juggernautCharge + amount);
    }

    public void removeJuggernautCharge(int amount) {
        setJuggernautCharge(this.juggernautCharge - amount);
    }

    public int getJuggernautMaxCharge() {
        return juggernautMaxCharge;
    }

    public boolean isChargeDecaying() {
        return chargeDecaying;
    }

    public void startChargeDecay() {
        this.chargeDecaying = true;
        this.chargeDecayTicks = 0;
    }

    public void stopChargeDecay() {
        this.chargeDecaying = false;
        this.chargeDecayTicks = 0;
    }

    public int getChargeDecayTicks() {
        return chargeDecayTicks;
    }

    public void tickChargeDecay() {
        if (chargeDecaying) {
            chargeDecayTicks++;
            if (chargeDecayTicks >= MAX_DECAY_TICKS) {
                // Fully depleted - switch back to SHIELD
                this.juggernautCharge = 0;
                this.juggernautShieldMode = true;
                this.chargeDecaying = false;
                this.chargeDecayTicks = 0;
            } else {
                // Gradual decay
                float decayRate = (float) juggernautCharge / MAX_DECAY_TICKS;
                removeJuggernautCharge((int) Math.ceil(decayRate));
            }
        }
    }

    //JUGGERNAUT - FORTIFY
    public boolean isFortifyActive() {
        return fortifyActive;
    }

    public void setFortifyActive(boolean active) {
        this.fortifyActive = active;
    }

    public int getFortifyTicks() {
        return fortifyTicks;
    }

    public void setFortifyTicks(int ticks) {
        this.fortifyTicks = ticks;
    }

    //JUGGERNAUT - LEAP
    public boolean isLeapActive() {
        return leapActive;
    }

    public void setLeapActive(boolean active) {
        this.leapActive = active;
    }

    public double getLeapStartY() {
        return leapStartY;
    }

    public void setLeapStartY(double y) {
        this.leapStartY = y;
    }

    public boolean isLeapShieldMode() {
        return leapShieldMode;
    }

    public void setLeapShieldMode(boolean mode) {
        this.leapShieldMode = mode;
    }

    //JUGGERNAUT - CRUSH (New)
    public boolean isCrushActive() {
        return crushActive;
    }

    public void setCrushActive(boolean active) {
        this.crushActive = active;
    }

    public boolean isCrushPowered() {
        return crushPowered;
    }

    public void setCrushPowered(boolean powered) {
        this.crushPowered = powered;
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

    public void clearAllCooldowns() {
        abilityCooldowns.clear();
    }

    public Map<String, Integer> getAllCooldowns() {
        return new HashMap<>(abilityCooldowns);
    }

    // ACCUMULATION
    public int getManaforgeArcana() {
        return manaforgeArcana;
    }

    public void setManaforgeArcana(int arcana) {
        this.manaforgeArcana = Math.max(0, Math.min(arcana, 100));
    }

    public int getManaforgeLastAttackTick() {
        return manaforgeLastAttackTick;
    }

    public void setManaforgeLastAttackTick(int tick) {
        this.manaforgeLastAttackTick = tick;
    }

    // SURGE
    public boolean isSurgeActive() {
        return surgeActive;
    }

    public void setSurgeActive(boolean active) {
        this.surgeActive = active;
    }

    public int getSurgeTicks() {
        return surgeTicks;
    }

    public void setSurgeTicks(int ticks) {
        this.surgeTicks = ticks;
    }

    public int getSurgeChargeTime() {
        return surgeChargeTime;
    }

    public void setSurgeChargeTime(int time) {
        this.surgeChargeTime = time;
    }

    // RIFT
    public boolean isRiftActive() {
        return riftActive;
    }

    public void setRiftActive(boolean active) {
        this.riftActive = active;
    }

    public int getRiftTicks() {
        return riftTicks;
    }

    public void setRiftTicks(int ticks) {
        this.riftTicks = ticks;
    }

    public Vec3 getRiftPosition() {
        return riftPosition;
    }

    public void setRiftPosition(Vec3 pos) {
        this.riftPosition = pos;
    }

    // COALESCENCE
    public boolean isCoalescenceActive() {
        return coalescenceActive;
    }

    public void setCoalescenceActive(boolean active) {
        this.coalescenceActive = active;
    }

    public int getCoalescenceTicks() {
        return coalescenceTicks;
    }

    public void setCoalescenceTicks(int ticks) {
        this.coalescenceTicks = ticks;
    }

    public float getCoalescenceStoredDamage() {
        return coalescenceStoredDamage;
    }

    public void setCoalescenceStoredDamage(float damage) {
        this.coalescenceStoredDamage = damage;
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

        nbt.putBoolean("parry_active", parryActive);
        nbt.putInt("parry_ticks", parryTicks);
        nbt.putBoolean("parry_successful", parrySuccessful);

        nbt.putBoolean("final_waltz_active", finalWaltzActive);
        nbt.putInt("final_waltz_ticks", finalWaltzTicks);
        nbt.putInt("final_waltz_overflow", finalWaltzOverflow);

        // Juggernaut data
        nbt.putBoolean("juggernaut_shield_mode", juggernautShieldMode);
        nbt.putInt("juggernaut_charge", juggernautCharge);
        nbt.putBoolean("charge_decaying", chargeDecaying);
        nbt.putInt("charge_decay_ticks", chargeDecayTicks);

        nbt.putBoolean("fortify_active", fortifyActive);
        nbt.putInt("fortify_ticks", fortifyTicks);

        nbt.putBoolean("leap_active", leapActive);
        nbt.putDouble("leap_start_y", leapStartY);
        nbt.putBoolean("leap_shield_mode", leapShieldMode);

        // Save Crush Data
        nbt.putBoolean("crush_active", crushActive);
        nbt.putBoolean("crush_powered", crushPowered);

        nbt.putInt("manaforge_arcana", manaforgeArcana);
        nbt.putInt("manaforge_last_attack", manaforgeLastAttackTick);

        nbt.putBoolean("surge_active", surgeActive);
        nbt.putInt("surge_ticks", surgeTicks);
        nbt.putInt("surge_charge_time", surgeChargeTime);

        nbt.putBoolean("rift_active", riftActive);
        nbt.putInt("rift_ticks", riftTicks);
        nbt.putDouble("rift_x", riftPosition.x);
        nbt.putDouble("rift_y", riftPosition.y);
        nbt.putDouble("rift_z", riftPosition.z);

        nbt.putBoolean("coalescence_active", coalescenceActive);
        nbt.putInt("coalescence_ticks", coalescenceTicks);
        nbt.putFloat("coalescence_stored", coalescenceStoredDamage);

        nbt.putInt("marksman_charges", marksmanSeekerCharges);
        nbt.putInt("marksman_airborne", marksmanAirborneTicks);

        nbt.putBoolean("arrow_rain_active", arrowRainActive);
        nbt.putInt("arrow_rain_ticks", arrowRainTicks);
        nbt.putDouble("arrow_rain_x", arrowRainPosition.x);
        nbt.putDouble("arrow_rain_y", arrowRainPosition.y);
        nbt.putDouble("arrow_rain_z", arrowRainPosition.z);


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

        if (nbt.contains("parry_active")) {
            this.parryActive = nbt.getBoolean("parry_active");
        }
        if (nbt.contains("parry_ticks")) {
            this.parryTicks = nbt.getInt("parry_ticks");
        }
        if (nbt.contains("parry_successful")) {
            this.parrySuccessful = nbt.getBoolean("parry_successful");
        }

        if (nbt.contains("final_waltz_active")) {
            this.finalWaltzActive = nbt.getBoolean("final_waltz_active");
        }
        if (nbt.contains("final_waltz_ticks")) {
            this.finalWaltzTicks = nbt.getInt("final_waltz_ticks");
        }
        if (nbt.contains("final_waltz_overflow")) {
            this.finalWaltzOverflow = nbt.getInt("final_waltz_overflow");
        }

        // Juggernaut data
        if (nbt.contains("juggernaut_shield_mode")) {
            this.juggernautShieldMode = nbt.getBoolean("juggernaut_shield_mode");
        }
        if (nbt.contains("juggernaut_charge")) {
            this.juggernautCharge = nbt.getInt("juggernaut_charge");
        }
        if (nbt.contains("charge_decaying")) {
            this.chargeDecaying = nbt.getBoolean("charge_decaying");
        }
        if (nbt.contains("charge_decay_ticks")) {
            this.chargeDecayTicks = nbt.getInt("charge_decay_ticks");
        }

        if (nbt.contains("fortify_active")) {
            this.fortifyActive = nbt.getBoolean("fortify_active");
        }
        if (nbt.contains("fortify_ticks")) {
            this.fortifyTicks = nbt.getInt("fortify_ticks");
        }

        if (nbt.contains("leap_active")) {
            this.leapActive = nbt.getBoolean("leap_active");
        }
        if (nbt.contains("leap_start_y")) {
            this.leapStartY = nbt.getDouble("leap_start_y");
        }
        if (nbt.contains("leap_shield_mode")) {
            this.leapShieldMode = nbt.getBoolean("leap_shield_mode");
        }

        // Load Crush Data
        if (nbt.contains("crush_active")) {
            this.crushActive = nbt.getBoolean("crush_active");
        }
        if (nbt.contains("crush_powered")) {
            this.crushPowered = nbt.getBoolean("crush_powered");
        }

        if (nbt.contains("manaforge_arcana")) {
            this.manaforgeArcana = nbt.getInt("manaforge_arcana");
        }
        if (nbt.contains("manaforge_last_attack")) {
            this.manaforgeLastAttackTick = nbt.getInt("manaforge_last_attack");
        }

        if (nbt.contains("surge_active")) {
            this.surgeActive = nbt.getBoolean("surge_active");
        }
        if (nbt.contains("surge_ticks")) {
            this.surgeTicks = nbt.getInt("surge_ticks");
        }
        if (nbt.contains("surge_charge_time")) {
            this.surgeChargeTime = nbt.getInt("surge_charge_time");
        }

        if (nbt.contains("rift_active")) {
            this.riftActive = nbt.getBoolean("rift_active");
        }
        if (nbt.contains("rift_ticks")) {
            this.riftTicks = nbt.getInt("rift_ticks");
        }
        if (nbt.contains("rift_x")) {
            double x = nbt.getDouble("rift_x");
            double y = nbt.getDouble("rift_y");
            double z = nbt.getDouble("rift_z");
            this.riftPosition = new Vec3(x, y, z);
        }

        if (nbt.contains("coalescence_active")) {
            this.coalescenceActive = nbt.getBoolean("coalescence_active");
        }
        if (nbt.contains("coalescence_ticks")) {
            this.coalescenceTicks = nbt.getInt("coalescence_ticks");
        }
        if (nbt.contains("coalescence_stored")) {
            this.coalescenceStoredDamage = nbt.getFloat("coalescence_stored");
        }

        if (nbt.contains("marksman_charges")) {
            this.marksmanSeekerCharges = nbt.getInt("marksman_charges");
        }
        if (nbt.contains("marksman_airborne")) {
            this.marksmanAirborneTicks = nbt.getInt("marksman_airborne");
        }

        if (nbt.contains("arrow_rain_active")) {
            this.arrowRainActive = nbt.getBoolean("arrow_rain_active");
        }
        if (nbt.contains("arrow_rain_ticks")) {
            this.arrowRainTicks = nbt.getInt("arrow_rain_ticks");
        }
        if (nbt.contains("arrow_rain_x")) {
            double x = nbt.getDouble("arrow_rain_x");
            double y = nbt.getDouble("arrow_rain_y");
            double z = nbt.getDouble("arrow_rain_z");
            this.arrowRainPosition = new Vec3(x, y, z);
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