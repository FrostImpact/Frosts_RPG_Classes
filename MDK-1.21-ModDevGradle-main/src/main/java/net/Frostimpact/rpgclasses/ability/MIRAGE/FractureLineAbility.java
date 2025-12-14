package net.Frostimpact.rpgclasses.ability.MIRAGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class FractureLineAbility extends Ability {

    private static final int CHARGE_TIME = 20; // 1 second charge time
    private static final double DASH_DISTANCE = 35.0; // Increased range for more dramatic effect
    private static final double DASH_SPEED = 2.5; // Increased speed for more dramatic dash
    private static final double AFTERIMAGE_COLLECT_RADIUS = 3.0;
    private static final int FRACTURE_EXPLOSION_DELAY_TICKS = 30; // 1.5 seconds

    public FractureLineAbility() {
        super("fracture_line");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Check if already charging or dashing
        if (rpgData.isMirageFractureLineCharging() || rpgData.isMirageFractureLineActive()) {
            return false;
        }

        // Start charging
        rpgData.setMirageFractureLineCharging(true);
        rpgData.setMirageFractureLineTicks(CHARGE_TIME);

        // Store dash direction - HORIZONTAL ONLY (remove vertical component)
        Vec3 lookVec = player.getLookAngle();
        Vec3 dashDirection = new Vec3(lookVec.x, 0, lookVec.z).normalize(); // Y set to 0 for horizontal only
        rpgData.setMirageFractureLineDirection(dashDirection);

        // Spawn enhanced charging particles
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.WITCH,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 0.5, 0.8, 0.5, 0.1
            );
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.4, 0.6, 0.4, 0.08
            );
        }

        // Sound effect for charging
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDER_DRAGON_GROWL, player.getSoundSource(), 0.5f, 2.0f);

        player.sendSystemMessage(Component.literal("§5Charging Fracture Line..."));

        // Resources (consume immediately)
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    // This method is called from ServerEvents when charge completes
    public static void startDash(ServerPlayer player, PlayerRPGData rpgData) {
        rpgData.setMirageFractureLineCharging(false);
        rpgData.setMirageFractureLineActive(true);
        rpgData.setMirageFractureLineTicks(20); // Dash duration (1 second)

        // Set player velocity for dash - horizontal only
        Vec3 dashDirection = rpgData.getMirageFractureLineDirection();
        player.setDeltaMovement(dashDirection.scale(DASH_SPEED));
        player.hurtMarked = true;
        player.hasImpulse = true;

        // Spawn dramatic dash start particles
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 1, player.getZ(),
                80, 0.5, 0.8, 0.5, 0.3
            );
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1, player.getZ(),
                60, 0.4, 0.6, 0.4, 0.2
            );
            serverLevel.sendParticles(
                ParticleTypes.WITCH,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 0.3, 0.5, 0.3, 0.15
            );
            // Add explosion-like effect at start
            serverLevel.sendParticles(
                ParticleTypes.DRAGON_BREATH,
                player.getX(), player.getY() + 1, player.getZ(),
                40, 0.6, 0.8, 0.6, 0.1
            );
        }

        // Collect afterimages hit by the dash path
        // We'll mark them for explosion in ServerEvents
        List<Integer> afterimageIds = new ArrayList<>(rpgData.getMirageAfterimageIds());
        for (Integer id : afterimageIds) {
            if (player.level().getEntity(id) instanceof AfterimageEntity afterimage) {
                // Check if afterimage is in the dash path
                double distance = player.distanceTo(afterimage);
                if (distance < AFTERIMAGE_COLLECT_RADIUS) {
                    // Mark this afterimage for explosion by setting a tag
                    afterimage.getPersistentData().putBoolean("fracture_explode", true);
                    afterimage.getPersistentData().putInt("fracture_timer", FRACTURE_EXPLOSION_DELAY_TICKS);
                    
                    // Spawn particles when afterimage is collected
                    if (player.level() instanceof ServerLevel serverLevel) {
                        Vec3 pos = afterimage.position();
                        serverLevel.sendParticles(
                            ParticleTypes.WITCH,
                            pos.x, pos.y + 1, pos.z,
                            25, 0.3, 0.5, 0.3, 0.1
                        );
                    }
                }
            }
        }

        // Sound effect for dash
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1f, 0.5f);

        player.sendSystemMessage(Component.literal("§5FRACTURE LINE!"));
    }
}
