package net.Frostimpact.rpgclasses.ability.ALCHEMIST;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.projectile.InjectionBoltEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public class InjectionAbility extends Ability {

    public InjectionAbility() {
        super("injection");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        if (!rpgData.isAlchemistInjectionActive()) {
            // First activation - enter injection mode
            rpgData.setAlchemistInjectionActive(true);
            
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6⚗ INJECTION mode! Shift to cycle reagents, use again to fire."));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§7Current: §e" + rpgData.getAlchemistSelectedReagent()));

            // Don't consume resources yet - wait for second activation
            return true;
        } else {
            // Second activation - fire the bolt
            Vec3 lookVec = player.getLookAngle();
            Vec3 spawnPos = player.position()
                    .add(0, player.getEyeHeight() - 0.1, 0)
                    .add(lookVec.scale(0.5));

            InjectionBoltEntity bolt = new InjectionBoltEntity(
                    ModEntities.INJECTION_BOLT.get(),
                    player.level(),
                    player
            );

            bolt.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            bolt.setReagentType(rpgData.getAlchemistSelectedReagent());
            
            // Set velocity
            float speed = 2.5f;
            bolt.setDeltaMovement(lookVec.scale(speed));
            bolt.hurtMarked = true;

            player.level().addFreshEntity(bolt);

            // Sound effect
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 1.0f, 1.2f);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6⚗ INJECTION fired! Reagent: §e" + rpgData.getAlchemistSelectedReagent()));

            // Exit injection mode
            rpgData.setAlchemistInjectionActive(false);

            // Consume resources
            rpgData.setAbilityCooldown(id, getCooldownTicks());
            rpgData.useMana(getManaCost());

            return true;
        }
    }
}
