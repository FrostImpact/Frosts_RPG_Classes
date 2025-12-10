package net.Frostimpact.rpgclasses.registry.abilities;

public class AbilityStats {

    private final String Ability_Name;
    private final int Ability_Cooldown;
    private final int Ability_Mana;

    public AbilityStats(String Ability_Name, int Ability_Cooldown, int Ability_Mana){
        this.Ability_Name = Ability_Name;
        this.Ability_Cooldown = Ability_Cooldown;
        this.Ability_Mana = Ability_Mana;

    }

    public String getAbility_Name(){ return Ability_Name;}
    public int getAbility_Cooldown(){ return Ability_Cooldown;}
    public int getAbility_Mana() { return Ability_Mana;}



}
