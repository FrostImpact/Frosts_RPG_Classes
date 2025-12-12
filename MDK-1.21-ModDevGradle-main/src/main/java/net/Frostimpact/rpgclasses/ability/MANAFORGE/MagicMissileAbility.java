package net.Frostimpact.rpgclasses.ability.MANAFORGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.projectile.MagicMissileEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class MagicMissileAbility extends Ability {

    public MagicMissileAbility() {
        super("magic_missile");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Get ARCANA for bonus projectiles
        int arcana = rpgData.getManaforgeArcana();
        int bonusProjectiles = arcana / 25; // 1 extra per 25 ARCANA (max 4 extra)
        int totalProjectiles = 1 + bonusProjectiles;

        // Spawn projectiles
        Vec3 lookVec = player.getLookAngle();

        if (totalProjectiles == 1) {
            // Single projectile - straight shot
            spawnMissile(player, lookVec, 0);
        } else {
            // Multiple projectiles - spread pattern
            double spreadAngle = 0.15; // Radians
            double angleStep = (spreadAngle * 2) / (totalProjectiles - 1);

            for (int i = 0; i < totalProjectiles; i++) {
                double offsetAngle = -spreadAngle + (i * angleStep);
                spawnMissile(player, lookVec, offsetAngle);
            }
        }

        // Consume ARCANA if not in COALESCENCE
        if (!rpgData.isCoalescenceActive() && bonusProjectiles > 0) {
            rpgData.setManaforgeArcana(0);
        }

        // Sound effect
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.7f, 1.5f);

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    private void spawnMissile(ServerPlayer player, Vec3 lookVec, double angleOffset) {
        // Create rotated direction vector
        Vec3 direction = lookVec;

        if (angleOffset != 0) {
            // Rotate around Y axis for horizontal spread
            double cos = Math.cos(angleOffset);
            double sin = Math.sin(angleOffset);
            direction = new Vec3(
                    lookVec.x * cos - lookVec.z * sin,
                    lookVec.y,
                    lookVec.x * sin + lookVec.z * cos
            ).normalize();
        }

        // Spawn position (slightly in front of player)
        Vec3 spawnPos = player.position()
                .add(0, player.getEyeHeight() - 0.1, 0)
                .add(direction.scale(0.5));

        // Create missile entity
        MagicMissileEntity missile = new MagicMissileEntity(
                ModEntities.MAGIC_MISSILE.get(),
                player.level(),
                player
        );

        missile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        missile.shoot(direction.x, direction.y, direction.z, 1.5f, 0);

        player.level().addFreshEntity(missile);
    }
}