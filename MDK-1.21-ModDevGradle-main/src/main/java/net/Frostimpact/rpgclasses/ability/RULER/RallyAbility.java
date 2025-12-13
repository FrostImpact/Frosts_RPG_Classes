package net.Frostimpact.rpgclasses.ability.RULER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class RallyAbility extends Ability {

    private static final int RALLY_DURATION = 240; // 12 seconds

    public RallyAbility() {
        super("rally");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();

        if (rpgData.isRulerRallyActive()) {
            // DEACTIVATE RALLY - Move banner back to player
            rpgData.setRulerRallyActive(false);
            rpgData.setRulerRallyTicks(0);

            Vec3 oldBannerPos = rpgData.getRulerBannerPosition();
            rpgData.setRulerBannerPosition(player.position());

            // Visual effect
            level.sendParticles(
                    ParticleTypes.PORTAL,
                    oldBannerPos.x, oldBannerPos.y + 1, oldBannerPos.z,
                    50, 0.5, 0.5, 0.5, 0.3
            );

            level.playSound(null, player.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.2f);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6⚔ RALLY ended! Banner returned."));

        } else {
            // ACTIVATE RALLY - Place banner at current location
            Vec3 lookVec = player.getLookAngle();
            Vec3 targetPos = player.position().add(lookVec.scale(5.0));

            rpgData.setRulerRallyActive(true);
            rpgData.setRulerRallyTicks(RALLY_DURATION);
            rpgData.setRulerBannerPosition(targetPos);

            // Visual effect
            level.sendParticles(
                    ParticleTypes.FLAME,
                    targetPos.x, targetPos.y + 1, targetPos.z,
                    50, 0.5, 1.0, 0.5, 0.1
            );

            level.playSound(null, player.blockPosition(),
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 1.5f);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6⚔ RALLY! Banner placed for 12s. Reactivate to recall."));
        }

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}
