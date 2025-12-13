package net.Frostimpact.rpgclasses.ability.MARKSMAN;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.projectile.VaultProjectileEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class VaultAbility extends Ability {

    public VaultAbility() {
        super("vault");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Get player look direction
        Vec3 lookVec = player.getLookAngle();
        Vec3 horizontalDir = new Vec3(lookVec.x, 0, lookVec.z).normalize();

        // Launch player forward and up
        Vec3 velocity = horizontalDir.scale(1.5).add(0, 0.4, 0);
        player.setDeltaMovement(velocity);
        player.hurtMarked = true;
        player.hasImpulse = true;

        // Spawn vault projectile (low velocity, arcing)
        VaultProjectileEntity projectile = new VaultProjectileEntity(
            ModEntities.VAULT_PROJECTILE.get(),
            player.level(),
            player
        );

        Vec3 spawnPos = player.position().add(0, player.getEyeHeight(), 0);
        projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        
        // Lob forward with arc
        Vec3 projectileVel = horizontalDir.scale(0.8).add(0, 0.3, 0);
        projectile.setDeltaMovement(projectileVel);
        projectile.hurtMarked = true;

        player.level().addFreshEntity(projectile);

        // Visual effects
        ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
            net.minecraft.core.particles.ParticleTypes.GLOW,
            player.getX(), player.getY() + 0.5, player.getZ(),
            20, 0.5, 0.5, 0.5, 0.1
        );

        // Sound
        player.level().playSound(null, player.blockPosition(),
            SoundEvents.ENDER_DRAGON_FLAP, SoundSource.PLAYERS, 0.8f, 1.3f);

        // Add SEEKER charge
        rpgData.addMarksmanSeekerCharge();

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}