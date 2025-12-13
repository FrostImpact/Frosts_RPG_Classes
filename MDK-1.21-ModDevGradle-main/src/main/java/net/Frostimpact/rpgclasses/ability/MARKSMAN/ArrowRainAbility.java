package net.Frostimpact.rpgclasses.ability.MARKSMAN;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class ArrowRainAbility extends Ability {

    private static final int DURATION_TICKS = 100; // 5 seconds
    private static final double RADIUS = 8.0;

    public ArrowRainAbility() {
        super("arrow_rain");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Calculate target position (where player is looking)
        Vec3 lookVec = player.getLookAngle();
        double distance = 10.0;
        
        Vec3 targetPos = player.position().add(
            lookVec.x * distance,
            0,
            lookVec.z * distance
        );

        // Activate arrow rain
        rpgData.setArrowRainActive(true);
        rpgData.setArrowRainTicks(DURATION_TICKS);
        rpgData.setArrowRainPosition(targetPos);

        // Visual activation
        net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
        
        // Ground indicator circle
        for (int i = 0; i < 50; i++) {
            double angle = (2 * Math.PI * i) / 50;
            double x = targetPos.x + Math.cos(angle) * RADIUS;
            double z = targetPos.z + Math.sin(angle) * RADIUS;
            
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.GLOW,
                x, targetPos.y + 0.1, z,
                1, 0, 0, 0, 0
            );
        }

        // Sound
        player.level().playSound(null, player.blockPosition(),
            SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 1.5f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§a☁ ARROW RAIN activated!"));

        // Add SEEKER charge
        rpgData.addMarksmanSeekerCharge();

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}