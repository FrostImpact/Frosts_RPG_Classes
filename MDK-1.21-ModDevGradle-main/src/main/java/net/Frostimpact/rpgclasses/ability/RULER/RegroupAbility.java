package net.Frostimpact.rpgclasses.ability.RULER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.ArcherSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.KnightSummonEntity;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RegroupAbility extends Ability {

    public RegroupAbility() {
        super("regroup");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();
        Vec3 bannerPos = rpgData.getRulerBannerPosition();

        // Find ALL summons (including demoralized)
        List<LivingEntity> summons = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(100),
                entity -> (entity instanceof KnightSummonEntity || entity instanceof ArcherSummonEntity)
        );

        if (summons.isEmpty()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cNo summons to regroup!"));
            return false;
        }

        // Command all summons to move to banner
        for (LivingEntity summon : summons) {
            // Pause demoralize timer
            if (summon.getPersistentData().getBoolean("demoralized")) {
                summon.getPersistentData().putBoolean("regroup_paused", true);
            }

            // Move to banner
            if (summon instanceof Mob mob) {
                mob.getNavigation().moveTo(bannerPos.x, bannerPos.y, bannerPos.z, 1.2);
            }

            // Particle trail
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    summon.getX(), summon.getY() + summon.getBbHeight() / 2, summon.getZ(),
                    3, 0.2, 0.2, 0.2, 0.05
            );

            // Heal when reaching banner (check distance)
            double dist = summon.position().distanceTo(bannerPos);
            if (dist < 3.0) {
                summon.heal(summon.getMaxHealth() * 0.5f); // Heal 50%
                
                level.sendParticles(
                        ParticleTypes.HEART,
                        summon.getX(), summon.getY() + summon.getBbHeight(), summon.getZ(),
                        3, 0.3, 0.3, 0.3, 0.1
                );

                // Un-pause demoralize
                summon.getPersistentData().putBoolean("regroup_paused", false);
            }
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 1.5f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§6⚔ REGROUP! All summons returning to banner."));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}