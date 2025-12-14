package net.Frostimpact.rpgclasses.ability.MIRAGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class FractureLineAbility extends Ability {

    private static final int CHARGE_TIME = 20; // 1 second charge time
    private static final double DASH_SPEED = 4.5; // Increased from 2.5 to 4.5 for much longer distance
    private static final double AFTERIMAGE_COLLECT_RADIUS = 4.0; // Slightly larger collection radius
    private static final int FRACTURE_EXPLOSION_DELAY_TICKS = 30;

    public FractureLineAbility() {
        super("fracture_line");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        if (rpgData.isMirageFractureLineCharging() || rpgData.isMirageFractureLineActive()) {
            return false;
        }

        rpgData.setMirageFractureLineCharging(true);
        rpgData.setMirageFractureLineTicks(CHARGE_TIME);

        Vec3 lookVec = player.getLookAngle();
        Vec3 dashDirection = new Vec3(lookVec.x, 0, lookVec.z).normalize();
        rpgData.setMirageFractureLineDirection(dashDirection);

        // Visuals: DNA/Helix Spiral for charging
        if (player.level() instanceof ServerLevel serverLevel) {
            spawnHelixParticles(serverLevel, player.position(), 1.0, 2.0, ParticleTypes.SOUL_FIRE_FLAME);
            spawnHelixParticles(serverLevel, player.position(), 1.0, 2.0, ParticleTypes.SCULK_SOUL);
        }

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.WARDEN_SONIC_CHARGE, player.getSoundSource(), 0.5f, 1.5f);

        player.sendSystemMessage(Component.literal("§5Charging Fracture Line..."));

        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    public static void startDash(ServerPlayer player, PlayerRPGData rpgData) {
        rpgData.setMirageFractureLineCharging(false);
        rpgData.setMirageFractureLineActive(true);
        rpgData.setMirageFractureLineTicks(20);

        Vec3 dashDirection = rpgData.getMirageFractureLineDirection();
        player.setDeltaMovement(dashDirection.scale(DASH_SPEED));
        player.hurtMarked = true;
        player.hasImpulse = true;

        // Visuals: Sonic Boom Ring + Directional Burst
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = player.position();

            // 1. Expanding Ring (Sonic Boom effect)
            spawnRingParticles(serverLevel, pos.add(0, 0.5, 0), 2.5, 40, ParticleTypes.SOUL);
            spawnRingParticles(serverLevel, pos.add(0, 0.5, 0), 1.5, 30, ParticleTypes.END_ROD);

            // 2. Directional Cone (Speed lines)
            for (int i = 0; i < 20; i++) {
                double speed = 0.5 + Math.random() * 0.5;
                serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        pos.x, pos.y + 1, pos.z,
                        0, // count 0 allows setting velocity manually
                        dashDirection.x * speed, 0.1, dashDirection.z * speed, 1.0
                );
            }
        }

        // Collect afterimages
        List<Integer> afterimageIds = new ArrayList<>(rpgData.getMirageAfterimageIds());
        for (Integer id : afterimageIds) {
            if (player.level().getEntity(id) instanceof AfterimageEntity afterimage) {
                double distance = player.distanceTo(afterimage);
                if (distance < AFTERIMAGE_COLLECT_RADIUS) {
                    afterimage.getPersistentData().putBoolean("fracture_explode", true);
                    afterimage.getPersistentData().putInt("fracture_timer", FRACTURE_EXPLOSION_DELAY_TICKS);

                    if (player.level() instanceof ServerLevel serverLevel) {
                        spawnRingParticles(serverLevel, afterimage.position().add(0, 1, 0), 1.0, 15, ParticleTypes.WITCH);
                    }
                }
            }
        }

        //player.level().playSound(null, player.blockPosition(),
                //.TRIDENT_RIPTIDE_3, player.getSoundSource(), 1f, 0.8f);

        player.sendSystemMessage(Component.literal("§5FRACTURE LINE!"));
    }

    // --- Particle Shape Helpers ---

    /**
     * Spawns a spiral of particles rising up.
     */
    private static void spawnHelixParticles(ServerLevel level, Vec3 center, double radius, double height, SimpleParticleType particle) {
        double density = 0.4; // Distance between points
        for (double y = 0; y < height; y += 0.1) {
            double angle = y * 5.0; // Rotation speed
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);

            // Create the particle
            level.sendParticles(particle, x, center.y + y, z, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Spawns a flat horizontal ring of particles.
     */
    private static void spawnRingParticles(ServerLevel level, Vec3 center, double radius, int count, SimpleParticleType particle) {
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            double x = center.x + radius * Math.cos(angle);
            double z = center.z + radius * Math.sin(angle);

            // Spawn particle with slight outward motion
            double motionX = Math.cos(angle) * 0.1;
            double motionZ = Math.sin(angle) * 0.1;

            level.sendParticles(particle, x, center.y, z, 0, motionX, 0, motionZ, 1.0);
        }
    }
}