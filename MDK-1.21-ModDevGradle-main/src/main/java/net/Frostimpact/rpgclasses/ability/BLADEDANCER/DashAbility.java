package net.Frostimpact.rpgclasses.ability.BLADEDANCER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public class DashAbility extends Ability {

    private static final double DASH_DISTANCE = 5.0;
    private static final double DASH_SPEED = 1.25;

    public DashAbility() {
        super("dash");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        Vec3 startPos = player.position();

        // --- CHANGE START: VELOCITY BASED DIRECTION ---

        // Get current velocity and flatten it (remove Y / up-down movement)
        Vec3 currentVel = player.getDeltaMovement();
        Vec3 horizontalMotion = new Vec3(currentVel.x, 0, currentVel.z);

        Vec3 direction;

        // Check if the player is effectively standing still (velocity is near zero)
        if (horizontalMotion.lengthSqr() < 1.0E-7) {
            // FALLBACK: Player is standing still, dash where they are looking
            float yaw = player.getYRot();
            double x = -Math.sin(Math.toRadians(yaw));
            double z = Math.cos(Math.toRadians(yaw));
            direction = new Vec3(x, 0, z).normalize();
        } else {
            // MOVING: Dash in the exact direction the player is already moving
            direction = horizontalMotion.normalize();
        }

        // --- CHANGE END ---

        // Set velocity
        player.setDeltaMovement(direction.scale(DASH_SPEED));

        // IMPORTANT: Mark player as "hurt" or "dirty" to force the server
        // to send the new velocity packet to the client immediately.
        player.hurtMarked = true;
        player.hasImpulse = true; // Ensures the client accepts the physics bump

        // Store dash data
        rpgData.setDashActive(true);
        rpgData.setDashStartPos(startPos);
        rpgData.setDashDirection(direction);
        rpgData.setDashTargetDistance(DASH_DISTANCE);

        // Sound
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1f, 1f);

        // Resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}