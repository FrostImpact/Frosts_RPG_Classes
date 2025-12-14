package net.Frostimpact.rpgclasses.ability.MIRAGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
import net.Frostimpact.rpgclasses.networking.packet.SyncMirageDataPacket;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ShadowstepAbility extends Ability {

    private static final double MAX_DISTANCE = 50.0;
    private static final double FOV_THRESHOLD = 0.7;

    public ShadowstepAbility() {
        super("shadowstep");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Check if we're in reactivation window
        if (rpgData.isMirageShadowstepActive()) {
            return performTeleport(player, rpgData, false);
        } else {
            return performTeleport(player, rpgData, true);
        }
    }

    private boolean performTeleport(ServerPlayer player, PlayerRPGData rpgData, boolean firstActivation) {
        // 1. Get candidates
        List<AfterimageEntity> candidates = getSortedAfterimagesInLineOfSight(player, rpgData);

        if (candidates.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cNo afterimage in line of sight!"));
            return false;
        }

        // 2. Find unused target
        AfterimageEntity target = null;
        for (AfterimageEntity candidate : candidates) {
            if (!candidate.getPersistentData().getBoolean("teleported_to")) {
                target = candidate;
                break;
            }
        }

        if (target == null) {
            player.sendSystemMessage(Component.literal("§cAll visible afterimages used!"));
            return false;
        }

        // 3. Teleport Logic
        Vec3 originPos = player.position();
        Vec3 targetPos = target.position();
        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        target.getPersistentData().putBoolean("teleported_to", true);

        // FX
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            spawnTeleportParticles(serverLevel, originPos, targetPos);
        }
        target.startLifetimeTimer();
        player.level().playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1f, 1f);

        // 4. Handle Window & Sync
        if (firstActivation) {
            // First activation: Start the window at 120 ticks
            rpgData.setMirageShadowstepActive(true);
            rpgData.setMirageShadowstepTicks(120);
            player.sendSystemMessage(Component.literal("§9Shadowstep active! §7(6s window)"));

            // Sync
            PacketDistributor.sendToPlayer(player, new SyncMirageDataPacket(true, 120));
        } else {
            // Reactivation: RESET the timer back to 120 ticks
            rpgData.setMirageShadowstepTicks(120);

            player.sendSystemMessage(Component.literal("§9Shadowstep chained! §7(Window reset)"));

            // Sync the RESET (120) to client so the bar refills
            PacketDistributor.sendToPlayer(player, new SyncMirageDataPacket(true, 120));
        }

        rpgData.useMana(getManaCost());
        return true;
    }

    private void spawnTeleportParticles(net.minecraft.server.level.ServerLevel level, Vec3 origin, Vec3 target) {
        // Origin Spiral
        for (int i = 0; i < 25; i++) {
            double angle = (2 * Math.PI * i) / 25;
            double radius = 0.5;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                    origin.x + radius * Math.cos(angle), origin.y + 1, origin.z + radius * Math.sin(angle),
                    1, 0.0, 0.3, 0.0, 0.1);
        }
        // Destination Burst
        for (int i = 0; i < 25; i++) {
            double angle = (2 * Math.PI * i) / 25;
            double radius = 0.5;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                    target.x + radius * Math.cos(angle), target.y + 1, target.z + radius * Math.sin(angle),
                    1, 0.0, 0.3, 0.0, 0.1);
        }
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                target.x, target.y + 1, target.z, 20, 0.3, 0.5, 0.3, 0.1);
    }

    private List<AfterimageEntity> getSortedAfterimagesInLineOfSight(ServerPlayer player, PlayerRPGData rpgData) {
        List<Integer> afterimageIds = rpgData.getMirageAfterimageIds();
        List<AfterimageEntity> validCandidates = new ArrayList<>();
        Vec3 playerEyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();

        for (Integer id : afterimageIds) {
            if (player.level().getEntity(id) instanceof AfterimageEntity afterimage) {
                Vec3 afterimagePos = afterimage.position();
                if (playerEyePos.distanceTo(afterimagePos) < MAX_DISTANCE) {
                    Vec3 dirTo = afterimagePos.subtract(playerEyePos).normalize();
                    if (lookVec.dot(dirTo) > FOV_THRESHOLD) {
                        HitResult hit = player.level().clip(new ClipContext(playerEyePos, afterimagePos,
                                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                        if (hit.getType() == HitResult.Type.MISS) {
                            validCandidates.add(afterimage);
                        }
                    }
                }
            }
        }
        validCandidates.sort(Comparator.comparingDouble(player::distanceToSqr));
        return validCandidates;
    }
}