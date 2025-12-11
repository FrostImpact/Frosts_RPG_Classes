package net.Frostimpact.rpgclasses.ability.BLADEDANCER.BLADE_DANCE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public class BladeDanceAbility extends Ability {

    private static final int DURATION_TICKS = 80; // 4 seconds
    private static final int INITIAL_BLADES = 4;

    public BladeDanceAbility() {
        super("blade_dance");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Activate blade dance
        rpgData.setBladeDanceActive(true);
        rpgData.setBladeDanceTicks(DURATION_TICKS);
        rpgData.setBladeDanceBlades(INITIAL_BLADES);
        rpgData.clearBladeDanceSwords();

        // Spawn sword entities using armor stands
        for (int i = 0; i < INITIAL_BLADES; i++) {
            ArmorStand swordStand = new ArmorStand(
                    net.minecraft.world.entity.EntityType.ARMOR_STAND,
                    player.level()
            );

            // Position the armor stand
            swordStand.setPos(player.getX(), player.getY() - 1, player.getZ());

            // Configure armor stand properties
            swordStand.setInvisible(true);          // Make the armor stand invisible
            swordStand.setNoGravity(true);          // Float in the air
            swordStand.setInvulnerable(true);       // Can't be destroyed
            swordStand.setShowArms(false);          // No arms visible
            swordStand.setNoBasePlate(true);        // No base plate
            //swordStand.setSmall(true);              // Small size// Make it glow

            // Give it a sword in the main hand
            swordStand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));

            // Add to world
            player.level().addFreshEntity(swordStand);
            rpgData.addBladeDanceSword(swordStand.getId());
        }

        // Play activation sound
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 1.5f);

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}