package net.Frostimpact.rpgclasses.ability.MANAFORGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public class SurgeAbility extends Ability {

    private static final int BASE_CHARGE_TIME = 40; // 2 seconds
    private static final int ARCANA_PER_TICK = 2; // 2 arcana = 1 tick extra charge time

    public SurgeAbility() {
        super("surge");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Calculate charge time based on ARCANA
        int arcana = rpgData.getManaforgeArcana();
        int extraTicks = 0;

        // Only consume ARCANA if COALESCENCE is not active
        if (!rpgData.isCoalescenceActive() && arcana > 0) {
            extraTicks = arcana / ARCANA_PER_TICK;
            rpgData.setManaforgeArcana(0); // Consume all ARCANA
        }

        int totalChargeTicks = BASE_CHARGE_TIME + extraTicks;

        // Activate surge
        rpgData.setSurgeActive(true);
        rpgData.setSurgeTicks(totalChargeTicks);
        rpgData.setSurgeChargeTime(totalChargeTicks);

        // Play charging sound
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.2f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§5✦ SURGE charging! §7(" + (totalChargeTicks / 20.0) + "s)"
        ));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    public static void tickSurge(ServerPlayer player, PlayerRPGData rpgData) {
        if (!rpgData.isSurgeActive()) return;

        ServerLevel level = player.serverLevel();
        int ticksRemaining = rpgData.getSurgeTicks();
        int totalTicks = rpgData.getSurgeChargeTime();

        // Charging particles - intensify as we get closer
        if (ticksRemaining > 0) {
            rpgData.setSurgeTicks(ticksRemaining - 1);

            // Calculate charge percentage
            float chargePercent = 1.0f - ((float) ticksRemaining / totalTicks);

            // Particle intensity increases with charge
            int particleCount = (int)(5 + (chargePercent * 20));

            // Swirling energy around player
            double time = (totalTicks - ticksRemaining) * 0.2;
            for (int i = 0; i < particleCount; i++) {
                double angle = time + (i * 2 * Math.PI / particleCount);
                double radius = 1.5 - (chargePercent * 0.5); // Contracts inward
                double height = 0.5 + (Math.sin(time * 2) * 0.3);

                double x = player.getX() + Math.cos(angle) * radius;
                double y = player.getY() + 1 + height;
                double z = player.getZ() + Math.sin(angle) * radius;

                // Purple energy particles
                level.sendParticles(
                        ParticleTypes.WITCH,
                        x, y, z,
                        1, 0, 0, 0, 0
                );

                level.sendParticles(
                        ParticleTypes.DRAGON_BREATH,
                        x, y, z,
                        1, 0, 0, 0, 0
                );
            }

            // Central charging effect
            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.5, player.getZ(),
                    (int)(chargePercent * 5), 0.2, 0.2, 0.2, 0.5
            );

            // Sound buildup
            if (ticksRemaining % 10 == 0) {
                float pitch = 0.8f + (chargePercent * 0.8f);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.3f, pitch);
            }

        } else {
            // RELEASE THE BEAM!
            fireSurgeBeam(player, rpgData, level);
            rpgData.setSurgeActive(false);
        }
    }

    private static void fireSurgeBeam(ServerPlayer player, PlayerRPGData rpgData, ServerLevel level) {
        // Calculate beam direction
        Vec3 lookVec = player.getLookAngle();
        double beamLength = 20.0;

        // Calculate damage based on charge time
        int chargeTime = rpgData.getSurgeChargeTime();
        float baseDamage = 15.0f;
        float bonusDamage = (chargeTime - BASE_CHARGE_TIME) * 0.5f; // 0.5 damage per extra tick
        float totalDamage = baseDamage + bonusDamage;

        // Create beam visual effect
        for (double d = 0; d < beamLength; d += 0.3) {
            double x = player.getX() + lookVec.x * d;
            double y = player.getY() + player.getEyeHeight() + lookVec.y * d;
            double z = player.getZ() + lookVec.z * d;

            // Core beam - bright purple
            level.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    x, y, z,
                    3, 0.2, 0.2, 0.2, 0.01
            );

            // Outer glow - witch particles
            level.sendParticles(
                    ParticleTypes.WITCH,
                    x, y, z,
                    2, 0.3, 0.3, 0.3, 0.02
            );

            // Energy crackle
            if (Math.random() < 0.3) {
                level.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        x, y, z,
                        1, 0.1, 0.1, 0.1, 0.1
                );
            }

            // Enchantment sparkles
            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    x, y, z,
                    1, 0.15, 0.15, 0.15, 0.3
            );
        }

        // Hit detection along beam
        for (double d = 0; d < beamLength; d += 0.5) {
            double x = player.getX() + lookVec.x * d;
            double y = player.getY() + player.getEyeHeight() + lookVec.y * d;
            double z = player.getZ() + lookVec.z * d;

            level.getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    new net.minecraft.world.phys.AABB(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1),
                    entity -> entity != player && entity.isAlive()
            ).forEach(entity -> {
                entity.hurt(player.damageSources().playerAttack(player), totalDamage);

                // Hit impact particles
                level.sendParticles(
                        ParticleTypes.EXPLOSION,
                        entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                        5, 0.3, 0.3, 0.3, 0.1
                );

                level.sendParticles(
                        ParticleTypes.WITCH,
                        entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                        10, 0.4, 0.4, 0.4, 0.15
                );
            });
        }

        // Explosion at end of beam
        double endX = player.getX() + lookVec.x * beamLength;
        double endY = player.getY() + player.getEyeHeight() + lookVec.y * beamLength;
        double endZ = player.getZ() + lookVec.z * beamLength;

        level.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                endX, endY, endZ,
                1, 0, 0, 0, 0
        );

        // Purple explosion wave
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double pitch = (Math.random() - 0.5) * Math.PI;
            double dist = Math.random() * 3;

            double xOff = Math.cos(angle) * Math.cos(pitch) * dist;
            double yOff = Math.sin(pitch) * dist;
            double zOff = Math.sin(angle) * Math.cos(pitch) * dist;

            level.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    endX, endY, endZ,
                    0, xOff * 0.3, yOff * 0.3, zOff * 0.3, 0.5
            );
        }

        // Sounds
        level.playSound(null, player.blockPosition(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.8f);

        level.playSound(null, player.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.5f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§5✦ SURGE! §c" + String.format("%.1f", totalDamage) + " damage"
        ));
    }
}