package net.Frostimpact.rpgclasses.ability.MIRAGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;

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

        // Set all afterimages to glide towards the cast position
        for (Integer id : afterimageIds) {
            if (player.level().getEntity(id) instanceof AfterimageEntity afterimage) {
                Vec3 afterimagePos = afterimage.position();
                Vec3 directionToRecall = castPosition.subtract(afterimagePos).normalize();
                afterimage.setGlideDirection(directionToRecall);
            }
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
