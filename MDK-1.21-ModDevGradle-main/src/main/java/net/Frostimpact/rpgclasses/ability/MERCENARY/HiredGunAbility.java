package net.Frostimpact.rpgclasses.ability.MERCENARY;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class HiredGunAbility extends Ability {

    private static final int DURATION_TICKS = 400; // 20 seconds
    private static final double MAX_RANGE = 64.0;

    public HiredGunAbility() {
        super("hired_gun");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Find first enemy in line of sight
        LivingEntity target = findTargetInSight(player);

        if (target == null) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cNo valid target in sight!"));
            return false;
        }

        // Activate HIRED GUN
        rpgData.setHiredGunActive(true);
        rpgData.setHiredGunTicks(DURATION_TICKS);
        rpgData.setHiredGunTargetId(target.getId());

        // Make target glow
        target.setGlowingTag(true);

        // Visual effects
        ServerLevel level = player.serverLevel();
        
        // Targeting beam effect
        Vec3 start = player.getEyePosition();
        Vec3 end = target.position().add(0, target.getBbHeight() / 2, 0);
        Vec3 diff = end.subtract(start);
        int steps = 20;

        for (int i = 0; i <= steps; i++) {
            Vec3 pos = start.add(diff.scale((double) i / steps));
            level.sendParticles(
                    ParticleTypes.CRIT,
                    pos.x, pos.y, pos.z,
                    1, 0, 0, 0, 0
            );
        }

        // Target marker
        level.sendParticles(
                ParticleTypes.FLASH,
                target.getX(), target.getY() + target.getBbHeight() + 1, target.getZ(),
                1, 0, 0, 0, 0
        );

        // Sound
        level.playSound(null, player.blockPosition(),
                SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0f, 0.5f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c✦ TARGET ACQUIRED: " + target.getDisplayName().getString()));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    private LivingEntity findTargetInSight(ServerPlayer player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 endPos = eyePos.add(lookVec.scale(MAX_RANGE));

        // Get entities in bounding box
        AABB searchBox = new AABB(eyePos, endPos).inflate(2.0);
        List<LivingEntity> entities = player.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive()
        );

        // Find closest entity in line of sight
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            Vec3 toEntity = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(eyePos);
            double dist = toEntity.length();
            
            // Check if entity is roughly in look direction
            if (toEntity.normalize().dot(lookVec) > 0.95) {
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = entity;
                }
            }
        }

        return closest;
    }

    public static void onTargetKilled(ServerPlayer player, PlayerRPGData rpgData, LivingEntity killed) {
        if (!rpgData.isHiredGunActive()) return;
        if (rpgData.getHiredGunTargetId() != killed.getId()) return;

        // SUCCESS! Grant rewards
        rpgData.setHiredGunActive(false);
        rpgData.setHiredGunTicks(0);
        rpgData.setHiredGunTargetId(-1);

        // Reset ALL ability cooldowns
        rpgData.clearAllCooldowns();

        // Grant Speed III
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED,
                60,  // 3 seconds
                2,   // Speed III
                false,
                true,
                true
        ));

        // Epic reward effects
        ServerLevel level = player.serverLevel();

        level.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 0.5, 0.5, 0.5, 0.2
        );

        level.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.5f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§6✦ BOUNTY COMPLETE! §aAll abilities refreshed! §b+Speed III"));
    }
}