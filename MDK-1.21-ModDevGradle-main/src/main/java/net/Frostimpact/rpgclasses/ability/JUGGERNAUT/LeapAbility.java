package net.Frostimpact.rpgclasses.ability.JUGGERNAUT;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

public class LeapAbility extends Ability {

    private static final double LEAP_DISTANCE = 7.0;
    private static final int CHARGE_COST = 15;

    public LeapAbility() {
        super("leap");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        boolean isShieldMode = rpgData.isJuggernautShieldMode();
        int charge = rpgData.getJuggernautCharge();

        // Calculate leap direction
        float yaw = player.getYRot();
        double x = -Math.sin(Math.toRadians(yaw));
        double z = Math.cos(Math.toRadians(yaw));
        Vec3 direction = new Vec3(x, 0, z).normalize();

        // Calculate leap velocity
        double horizontalSpeed = 1.5;
        double verticalSpeed = 0.6;

        Vec3 velocity = direction.scale(horizontalSpeed).add(0, verticalSpeed, 0);
        player.setDeltaMovement(velocity);
        player.hurtMarked = true;
        player.hasImpulse = true;

        // Store leap data for landing detection
        rpgData.setLeapActive(true);
        rpgData.setLeapStartY(player.getY());

        // Mode-specific effects
        if (isShieldMode) {
            // SHIELD MODE: Grant absorption to allies on landing
            rpgData.setLeapShieldMode(true);
        } else {
            // SHATTER MODE: Check charge and adjust cooldown
            if (charge >= CHARGE_COST) {
                rpgData.removeJuggernautCharge(CHARGE_COST);
                // Reduced cooldown (3.5s = 70 ticks)
                rpgData.setAbilityCooldown(id, 70);
            } else {
                // Normal cooldown
                rpgData.setAbilityCooldown(id, getCooldownTicks());
            }
            rpgData.setLeapShieldMode(false);
        }

        // If SHIELD mode, set normal cooldown (will be set above for SHATTER)
        if (isShieldMode) {
            rpgData.setAbilityCooldown(id, getCooldownTicks());
        }

        // Visual effects
        ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                net.minecraft.core.particles.ParticleTypes.CLOUD,
                player.getX(), player.getY(), player.getZ(),
                20, 0.5, 0.5, 0.5, 0.1
        );

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.7f, 1.2f);

        // Consume mana
        rpgData.useMana(getManaCost());

        return true;
    }
}