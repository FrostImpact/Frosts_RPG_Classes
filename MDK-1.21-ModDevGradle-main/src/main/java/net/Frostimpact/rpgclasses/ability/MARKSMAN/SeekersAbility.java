package net.Frostimpact.rpgclasses.ability.MARKSMAN;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.projectile.SeekerArrowEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class SeekersAbility extends Ability {

    public SeekersAbility() {
        super("seekers");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        int charges = rpgData.getMarksmanSeekerCharges();
        
        if (charges <= 0) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cNo SEEKER charges! Stay airborne to gain charges."));
            return false;
        }

        // Calculate mana cost (5 per charge)
        int manaCost = 5 * charges;
        if (rpgData.getMana() < manaCost) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cNot enough mana! Need " + manaCost));
            return false;
        }

        // Fire seekers in a circle pattern
        double angleStep = (2 * Math.PI) / charges;
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        for (int i = 0; i < charges; i++) {
            double angle = i * angleStep;
            
            // Calculate launch direction
            Vec3 direction = new Vec3(
                Math.cos(angle),
                0.3, // Slight upward angle
                Math.sin(angle)
            ).normalize();

            // Spawn seeker arrow
            SeekerArrowEntity seeker = new SeekerArrowEntity(
                ModEntities.SEEKER_ARROW.get(),
                player.level(),
                player
            );

            Vec3 spawnPos = playerPos.add(direction.scale(0.5));
            seeker.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            seeker.setDeltaMovement(direction.scale(1.2));
            seeker.hurtMarked = true;

            player.level().addFreshEntity(seeker);
        }

        // Visual effects
        ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
            net.minecraft.core.particles.ParticleTypes.GLOW,
            player.getX(), player.getY() + 1, player.getZ(),
            charges * 10, 0.5, 0.5, 0.5, 0.2
        );

        // Sound
        player.level().playSound(null, player.blockPosition(),
            SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f);
        
        player.level().playSound(null, player.blockPosition(),
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.5f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "§a✦ " + charges + " SEEKERS released!"));

        // Consume charges and mana
        rpgData.setMarksmanSeekerCharges(0);
        rpgData.useMana(manaCost);
        rpgData.setAbilityCooldown(id, getCooldownTicks());

        return true;
    }
}