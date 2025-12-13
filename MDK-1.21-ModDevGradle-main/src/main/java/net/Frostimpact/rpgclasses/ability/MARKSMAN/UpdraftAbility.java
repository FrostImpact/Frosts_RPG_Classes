package net.Frostimpact.rpgclasses.ability.MARKSMAN;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class UpdraftAbility extends Ability {

    public UpdraftAbility() {
        super("updraft");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Launch player upward
        Vec3 currentVel = player.getDeltaMovement();
        player.setDeltaMovement(currentVel.x * 0.5, 1.5, currentVel.z * 0.5);
        player.hurtMarked = true;
        player.hasImpulse = true;

        // Visual effects - upward spiral
        net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();
        
        for (int i = 0; i < 30; i++) {
            double angle = (i / 30.0) * Math.PI * 4;
            double radius = 0.8;
            double height = i * 0.15;
            
            double x = player.getX() + Math.cos(angle) * radius;
            double y = player.getY() + height;
            double z = player.getZ() + Math.sin(angle) * radius;
            
            level.sendParticles(
                net.minecraft.core.particles.ParticleTypes.GLOW,
                x, y, z,
                1, 0, 0, 0, 0
            );
        }

        // Ground burst
        level.sendParticles(
            net.minecraft.core.particles.ParticleTypes.EXPLOSION,
            player.getX(), player.getY(), player.getZ(),
            1, 0, 0, 0, 0
        );

        // Sound
        player.level().playSound(null, player.blockPosition(),
            SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0f, 1.2f);

        // Add SEEKER charge
        rpgData.addMarksmanSeekerCharge();

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§a⬆ UPDRAFT!"));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}