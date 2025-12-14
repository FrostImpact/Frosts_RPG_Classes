package net.Frostimpact.rpgclasses.ability.ARTIFICER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.ShockTowerEntity;
import net.Frostimpact.rpgclasses.entity.summon.TurretSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.WindTowerEntity;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class RepositionAbility extends Ability {

    private static final double MAX_DISTANCE = 50.0;

    public RepositionAbility() {
        super("reposition");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();

        // Find all summons owned by player
        List<PathfinderMob> allSummons = new ArrayList<>();
        
        List<TurretSummonEntity> turrets = level.getEntitiesOfClass(
                TurretSummonEntity.class,
                player.getBoundingBox().inflate(MAX_DISTANCE),
                entity -> entity.getOwner() == player
        );
        allSummons.addAll(turrets);
        
        List<ShockTowerEntity> shockTowers = level.getEntitiesOfClass(
                ShockTowerEntity.class,
                player.getBoundingBox().inflate(MAX_DISTANCE),
                entity -> entity.getOwner() == player
        );
        allSummons.addAll(shockTowers);
        
        List<WindTowerEntity> windTowers = level.getEntitiesOfClass(
                WindTowerEntity.class,
                player.getBoundingBox().inflate(MAX_DISTANCE),
                entity -> entity.getOwner() == player
        );
        allSummons.addAll(windTowers);

        if (allSummons.isEmpty()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cNo summons nearby!"));
            return false;
        }

        // Find closest summon in line of sight
        PathfinderMob closest = null;
        double closestDistance = Double.MAX_VALUE;
        Vec3 playerEye = player.getEyePosition();

        for (PathfinderMob summon : allSummons) {
            Vec3 summonPos = summon.position();
            double distance = playerEye.distanceTo(summonPos);

            if (distance < closestDistance) {
                // Check line of sight
                ClipContext context = new ClipContext(
                        playerEye,
                        summonPos,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                );

                BlockHitResult hit = level.clip(context);

                if (hit.getType() == HitResult.Type.MISS || 
                    hit.getLocation().distanceTo(summonPos) < 1.0) {
                    // Line of sight is clear
                    closest = summon;
                    closestDistance = distance;
                }
            }
        }

        if (closest == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cNo summons in line of sight!"));
            return false;
        }

        // Teleport to summon
        Vec3 oldPos = player.position();
        Vec3 targetPos = closest.position();

        // Departure effect
        level.sendParticles(
                ParticleTypes.PORTAL,
                oldPos.x, oldPos.y + 1, oldPos.z,
                50, 0.5, 1.0, 0.5, 0.5
        );

        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);

        // Arrival effect
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                targetPos.x, targetPos.y + 1, targetPos.z,
                50, 0.5, 1.0, 0.5, 0.5
        );

        level.playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7⚙ Repositioned to summon!"));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}
