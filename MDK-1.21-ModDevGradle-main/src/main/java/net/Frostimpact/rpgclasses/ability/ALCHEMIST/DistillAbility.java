package net.Frostimpact.rpgclasses.ability.ALCHEMIST;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

public class DistillAbility extends Ability {

    private static final Random RANDOM = new Random();

    public DistillAbility() {
        super("distill");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Find nearest enemy
        LivingEntity nearestEnemy = findNearestEnemy(player);

        if (nearestEnemy != null) {
            // Remove all debuffs from the nearest enemy
            nearestEnemy.removeAllEffects();

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§b⚗ DISTILL! Removed all debuffs from nearest enemy."));

            // Apply random buffs to player for 3 seconds (60 ticks)
            int duration = 60;
            int numBuffs = 2 + RANDOM.nextInt(2); // 2-3 random buffs

            for (int i = 0; i < numBuffs; i++) {
                applyRandomBuff(player, duration);
            }

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§a⚗ Gained " + numBuffs + " random buffs!"));
        } else {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§c⚗ No nearby enemy to distill!"));
            return false;
        }

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    private LivingEntity findNearestEnemy(ServerPlayer player) {
        AABB searchBox = player.getBoundingBox().inflate(10.0);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            entity -> entity != player && !entity.isAlliedTo(player)
        );

        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            double distance = player.distanceToSqr(entity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = entity;
            }
        }

        return nearest;
    }

    private void applyRandomBuff(ServerPlayer player, int duration) {
        MobEffectInstance[] possibleBuffs = new MobEffectInstance[] {
            new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 0),
            new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 0),
            new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 0),
            new MobEffectInstance(MobEffects.REGENERATION, duration, 0),
            new MobEffectInstance(MobEffects.ABSORPTION, duration, 0),
            new MobEffectInstance(MobEffects.JUMP, duration, 0),
            new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 0)
        };

        MobEffectInstance randomBuff = possibleBuffs[RANDOM.nextInt(possibleBuffs.length)];
        player.addEffect(randomBuff);
    }
}
