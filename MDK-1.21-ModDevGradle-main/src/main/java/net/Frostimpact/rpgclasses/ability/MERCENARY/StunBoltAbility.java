package net.Frostimpact.rpgclasses.ability.MERCENARY;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.projectile.StunBoltEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class StunBoltAbility extends Ability {

    public StunBoltAbility() {
        super("stun_bolt");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Spawn stun bolt projectile
        Vec3 lookVec = player.getLookAngle();
        Vec3 spawnPos = player.position()
                .add(0, player.getEyeHeight() - 0.1, 0)
                .add(lookVec.scale(0.5));

        StunBoltEntity bolt = new StunBoltEntity(
                ModEntities.STUN_BOLT.get(),
                player.level(),
                player
        );

        bolt.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        
        // Set velocity
        float speed = 2.0f;
        bolt.setDeltaMovement(lookVec.scale(speed));
        bolt.hurtMarked = true;

        player.level().addFreshEntity(bolt);

        // Sound effect
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.2f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§e⚡ STUN BOLT fired!"));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}