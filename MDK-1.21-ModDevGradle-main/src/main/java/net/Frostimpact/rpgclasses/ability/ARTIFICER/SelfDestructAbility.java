package net.Frostimpact.rpgclasses.ability.ARTIFICER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.ShockTowerEntity;
import net.Frostimpact.rpgclasses.entity.summon.TurretSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.WindTowerEntity;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

import java.util.ArrayList;
import java.util.List;

public class SelfDestructAbility extends Ability {

    private static final double EXPLOSION_RADIUS = 5.0;
    private static final float EXPLOSION_DAMAGE = 8.0f;

    public SelfDestructAbility() {
        super("self_destruct");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();

        // Find all summons owned by player
        List<PathfinderMob> allSummons = new ArrayList<>();
        
        List<TurretSummonEntity> turrets = level.getEntitiesOfClass(
                TurretSummonEntity.class,
                player.getBoundingBox().inflate(100),
                entity -> entity.getOwner() == player
        );
        allSummons.addAll(turrets);
        
        List<ShockTowerEntity> shockTowers = level.getEntitiesOfClass(
                ShockTowerEntity.class,
                player.getBoundingBox().inflate(100),
                entity -> entity.getOwner() == player
        );
        allSummons.addAll(shockTowers);
        
        List<WindTowerEntity> windTowers = level.getEntitiesOfClass(
                WindTowerEntity.class,
                player.getBoundingBox().inflate(100),
                entity -> entity.getOwner() == player
        );
        allSummons.addAll(windTowers);

        if (allSummons.isEmpty()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cNo summons to destruct!"));
            return false;
        }

        int destructedCount = 0;

        // Explode each summon
        for (PathfinderMob summon : allSummons) {
            createExplosion(level, summon, player);
            summon.discard();
            destructedCount++;
        }

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c💥 SELF-DESTRUCT! §7Destroyed " + destructedCount + " summons!"));

        //level.playSound(null, player.blockPosition(),
                //SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.8f);

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    private void createExplosion(ServerLevel level, PathfinderMob summon, ServerPlayer owner) {
        // Explosion particles
        level.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                summon.getX(), summon.getY() + 0.5, summon.getZ(),
                1, 0, 0, 0, 0
        );

        level.sendParticles(
                ParticleTypes.FLAME,
                summon.getX(), summon.getY() + 0.5, summon.getZ(),
                50, 1.0, 1.0, 1.0, 0.2
        );

        level.sendParticles(
                ParticleTypes.SMOKE,
                summon.getX(), summon.getY() + 0.5, summon.getZ(),
                30, 1.0, 1.0, 1.0, 0.1
        );

        // Explosion sound
        //level.playSound(null, summon.blockPosition(),
                //SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.0f, 1.0f);

        // Damage nearby entities
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
                LivingEntity.class,
                summon.getBoundingBox().inflate(EXPLOSION_RADIUS),
                entity -> entity != owner && entity != summon && entity.isAlive()
        );

        DamageSource damageSource = level.damageSources().explosion(summon, owner);

        for (LivingEntity entity : nearbyEntities) {
            double distance = entity.position().distanceTo(summon.position());
            if (distance <= EXPLOSION_RADIUS) {
                // Damage falls off with distance
                float damage = EXPLOSION_DAMAGE * (float)(1.0 - (distance / EXPLOSION_RADIUS));
                entity.hurt(damageSource, damage);
            }
        }
    }
}
