package net.Frostimpact.rpgclasses.ability;

import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public class DashAbility extends Ability {

    private static final double DASH_DISTANCE = 5.0; // Exact distance in blocks
    private static final double DASH_SPEED = 1.25; // Speed per tick

    public DashAbility() {
        super("dash");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Store starting position and direction in the player's data
        Vec3 startPos = player.position();
        Vec3 direction = player.getLookAngle();

        // Set high velocity in the dash direction
        player.setDeltaMovement(direction.scale(DASH_SPEED));
        player.hurtMarked = true;

        // Store dash data for tracking
        rpgData.setDashActive(true);
        rpgData.setDashStartPos(startPos);
        rpgData.setDashDirection(direction);
        rpgData.setDashTargetDistance(DASH_DISTANCE);

        // Play sound
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1f, 1f);

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}