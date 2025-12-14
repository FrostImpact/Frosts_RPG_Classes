package net.Frostimpact.rpgclasses.ability.MIRAGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class FractureLineAbility extends Ability {

    private static final int CHARGE_TIME = 20; // 1 second charge time
    private static final double DASH_DISTANCE = 15.0;
    private static final double DASH_SPEED = 1.5;
    private static final double AFTERIMAGE_COLLECT_RADIUS = 3.0;

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

        // Store dash direction
        Vec3 lookVec = player.getLookAngle();
        Vec3 dashDirection = new Vec3(lookVec.x, lookVec.y, lookVec.z).normalize();
        rpgData.setMirageFractureLineDirection(dashDirection);

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

        // Set player velocity for dash
        Vec3 dashDirection = rpgData.getMirageFractureLineDirection();
        player.setDeltaMovement(dashDirection.scale(DASH_SPEED));
        player.hurtMarked = true;
        player.hasImpulse = true;

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
                    afterimage.getPersistentData().putInt("fracture_timer", 30); // 1.5 seconds
                }
            }
        }

        // Sound effect for dash
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1f, 0.5f);

        player.sendSystemMessage(Component.literal("§5FRACTURE LINE!"));
    }
}
