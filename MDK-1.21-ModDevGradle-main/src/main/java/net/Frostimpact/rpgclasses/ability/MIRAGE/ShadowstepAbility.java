package net.Frostimpact.rpgclasses.ability.MIRAGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShadowstepAbility extends Ability {

    private static final double MAX_DISTANCE = 50.0; // Line of sight check distance
    private static final double FOV_THRESHOLD = 0.7; // ~45 degrees field of view

    public ShadowstepAbility() {
        super("shadowstep");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Check if we're in reactivation window
        if (rpgData.isMirageShadowstepActive()) {
            // Reactivate - try to teleport again
            return performTeleport(player, rpgData, false);
        } else {
            // First activation
            return performTeleport(player, rpgData, true);
        }
    }

    private boolean performTeleport(ServerPlayer player, PlayerRPGData rpgData, boolean firstActivation) {
        // 1. Get ALL valid afterimages in line of sight, sorted by distance
        List<AfterimageEntity> candidates = getSortedAfterimagesInLineOfSight(player, rpgData);

        if (candidates.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cNo afterimage in line of sight!"));
            return false;
        }

        // 2. SMART TELEPORT: Find the first candidate we haven't used yet
        AfterimageEntity target = null;
        for (AfterimageEntity candidate : candidates) {
            // If we haven't teleported to this one yet, pick it
            if (!candidate.getPersistentData().getBoolean("teleported_to")) {
                target = candidate;
                break; // Found our target, stop looking
            }
        }

        // If all candidates in sight were already used
        if (target == null) {
            player.sendSystemMessage(Component.literal("§cAll visible afterimages used!"));
            return false;
        }

        // Store origin position for particles
        Vec3 originPos = player.position();

        // Teleport to the chosen target
        Vec3 targetPos = target.position();
        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);

        // Mark this afterimage as used
        target.getPersistentData().putBoolean("teleported_to", true);

        // Spawn particles at origin
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Spiral of soul particles at origin
            for (int i = 0; i < 25; i++) {
                double angle = (2 * Math.PI * i) / 25;
                double radius = 0.5;
                double offsetX = radius * Math.cos(angle);
                double offsetZ = radius * Math.sin(angle);
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.SOUL,
                        originPos.x + offsetX, originPos.y + 1, originPos.z + offsetZ,
                        1, 0.0, 0.3, 0.0, 0.1
                );
            }

            // Particles at destination
            for (int i = 0; i < 25; i++) {
                double angle = (2 * Math.PI * i) / 25;
                double radius = 0.5;
                double offsetX = radius * Math.cos(angle);
                double offsetZ = radius * Math.sin(angle);
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                        targetPos.x + offsetX, targetPos.y + 1, targetPos.z + offsetZ,
                        1, 0.0, 0.3, 0.0, 0.1
                );
            }

            // End rod particles to show teleport trail
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.END_ROD,
                    targetPos.x, targetPos.y + 1, targetPos.z,
                    20, 0.3, 0.5, 0.3, 0.1
            );
        }

        // Start lifetime timer on the afterimage
        target.startLifetimeTimer();

        // Sound effect
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1f, 1f);

        if (firstActivation) {
            // Start reactivation window (6 seconds = 120 ticks)
            rpgData.setMirageShadowstepActive(true);
            rpgData.setMirageShadowstepTicks(120);

            // Don't start cooldown yet
            player.sendSystemMessage(Component.literal("§9Shadowstep active! §7(6s window)"));
        } else {
            // Reactivation - reset timer to allow continuous chaining
            rpgData.setMirageShadowstepTicks(120); // Reset to full duration
            player.sendSystemMessage(Component.literal("§9Shadowstep chained! §7(6s window reset)"));
        }

        // Use mana
        rpgData.useMana(getManaCost());

        return true;
    }

    private List<AfterimageEntity> getSortedAfterimagesInLineOfSight(ServerPlayer player, PlayerRPGData rpgData) {
        List<Integer> afterimageIds = rpgData.getMirageAfterimageIds();
        List<AfterimageEntity> validCandidates = new ArrayList<>();

        Vec3 playerEyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();

        for (Integer id : afterimageIds) {
            if (player.level().getEntity(id) instanceof AfterimageEntity afterimage) {
                Vec3 afterimagePos = afterimage.position();
                double distance = playerEyePos.distanceTo(afterimagePos);

                if (distance < MAX_DISTANCE) {
                    // Check if in line of sight
                    Vec3 dirToAfterimage = afterimagePos.subtract(playerEyePos).normalize();
                    double dotProduct = lookVec.dot(dirToAfterimage);

                    // Must be in front (roughly within 45 degrees field of view)
                    if (dotProduct > FOV_THRESHOLD) {
                        // Raytrace to check for walls
                        HitResult hitResult = player.level().clip(new ClipContext(
                                playerEyePos,
                                afterimagePos,
                                ClipContext.Block.COLLIDER,
                                ClipContext.Fluid.NONE,
                                player
                        ));

                        // If line of sight is clear, add to candidates
                        if (hitResult.getType() == HitResult.Type.MISS) {
                            validCandidates.add(afterimage);
                        }
                    }
                }
            }
        }

        // Sort by distance (Nearest first)
        validCandidates.sort(Comparator.comparingDouble(e -> player.distanceToSqr(e)));

        return validCandidates;
    }
}