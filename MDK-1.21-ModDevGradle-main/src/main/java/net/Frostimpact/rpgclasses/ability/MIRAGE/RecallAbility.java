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

import java.util.List;

public class RecallAbility extends Ability {

    public RecallAbility() {
        super("recall");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        List<Integer> afterimageIds = rpgData.getMirageAfterimageIds();

        if (afterimageIds.isEmpty()) {
            player.sendSystemMessage(Component.literal("§cNo afterimages to recall!"));
            return false;
        }

        // Store cast position
        Vec3 castPosition = player.position();
        rpgData.setMirageRecallActive(true);
        rpgData.setMirageRecallPosition(castPosition);

        // Set all afterimages to glide towards the cast position and spawn particles
        if (player.level() instanceof ServerLevel serverLevel) {
            for (Integer id : afterimageIds) {
                if (player.level().getEntity(id) instanceof AfterimageEntity afterimage) {
                    Vec3 afterimagePos = afterimage.position();
                    Vec3 directionToRecall = castPosition.subtract(afterimagePos).normalize();
                    afterimage.setGlideDirection(directionToRecall);
                    
                    // Spawn witch particles at each afterimage being recalled
                    serverLevel.sendParticles(
                        ParticleTypes.WITCH,
                        afterimagePos.x, afterimagePos.y + 1, afterimagePos.z,
                        15, 0.3, 0.5, 0.3, 0.1
                    );
                    
                    // Spawn soul particles showing the direction
                    serverLevel.sendParticles(
                        ParticleTypes.SOUL,
                        afterimagePos.x, afterimagePos.y + 1, afterimagePos.z,
                        10, directionToRecall.x * 0.3, 0.2, directionToRecall.z * 0.3, 0.3
                    );
                }
            }
            
            // Spawn particles at the recall target position
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                castPosition.x, castPosition.y + 1, castPosition.z,
                30, 0.5, 0.5, 0.5, 0.05
            );
        }

        // Sound effect
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.CHORUS_FRUIT_TELEPORT, player.getSoundSource(), 1f, 0.8f);

        player.sendSystemMessage(Component.literal("§9Afterimages recalled!"));

        // Resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}
