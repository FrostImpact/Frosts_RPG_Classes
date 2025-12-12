package net.Frostimpact.rpgclasses.ability.MANAFORGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public class OpenRiftAbility extends Ability {

    private static final int DURATION_TICKS = 100; // 5 seconds
    private static final double PULL_RADIUS = 8.0;
    private static final double PULL_STRENGTH = 0.3;

    public OpenRiftAbility() {
        super("open_rift");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Calculate rift position (slightly in front of player)
        Vec3 lookVec = player.getLookAngle();
        double riftX = player.getX() + lookVec.x * 3;
        double riftY = player.getY() + 1.5;
        double riftZ = player.getZ() + lookVec.z * 3;

        // Store rift data
        rpgData.setRiftActive(true);
        rpgData.setRiftTicks(DURATION_TICKS);
        rpgData.setRiftPosition(new Vec3(riftX, riftY, riftZ));

        // Spawn initial rift creation effect
        spawnRiftCreation(player.serverLevel(), riftX, riftY, riftZ);

        // Sound
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5f, 0.5f);

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.0f, 0.8f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§5✦ RIFT OPENED!"
        ));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    private void spawnRiftCreation(ServerLevel level, double x, double y, double z) {
        // Explosive creation burst
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                x, y, z,
                100, 0.5, 0.5, 0.5, 0.5
        );

        // Purple explosion ring
        for (int i = 0; i < 40; i++) {
            double angle = (2 * Math.PI * i) / 40;
            double radius = 2.0;
            double xOff = Math.cos(angle) * radius;
            double zOff = Math.sin(angle) * radius;

            level.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    x + xOff, y, z + zOff,
                    2, 0, 0, 0, 0.1
            );
        }
    }

    public static void tickRift(ServerPlayer player, PlayerRPGData rpgData) {
        if (!rpgData.isRiftActive()) return;

        ServerLevel level = player.serverLevel();
        int ticksRemaining = rpgData.getRiftTicks();
        Vec3 riftPos = rpgData.getRiftPosition();

        if (ticksRemaining > 0) {
            rpgData.setRiftTicks(ticksRemaining - 1);

            double riftX = riftPos.x;
            double riftY = riftPos.y;
            double riftZ = riftPos.z;

            // ===  RIFT VISUAL EFFECT ===
            double time = (DURATION_TICKS - ticksRemaining) * 0.15;

            // Core vortex - spinning particles
            int coreParticles = 30;
            for (int i = 0; i < coreParticles; i++) {
                double angle = time + (i * 2 * Math.PI / coreParticles);
                double spiralRadius = 0.3 + (Math.sin(time * 2 + i * 0.5) * 0.2);
                double height = Math.sin(angle * 3) * 0.8;

                double x = riftX + Math.cos(angle) * spiralRadius;
                double y = riftY + height;
                double z = riftZ + Math.sin(angle) * spiralRadius;

                // Dark center
                level.sendParticles(
                        ParticleTypes.PORTAL,
                        x, y, z,
                        1, 0, 0, 0, 0
                );
            }

            // Outer ring - pulsing
            int ringParticles = 50;
            double pulseRadius = 1.5 + (Math.sin(time * 4) * 0.3);

            for (int i = 0; i < ringParticles; i++) {
                double angle = (2 * Math.PI * i) / ringParticles;
                double x = riftX + Math.cos(angle) * pulseRadius;
                double z = riftZ + Math.sin(angle) * pulseRadius;

                // Purple ring
                level.sendParticles(
                        ParticleTypes.DRAGON_BREATH,
                        x, riftY, z,
                        1, 0, 0, 0, 0
                );

                // Witch sparkles
                if (i % 3 == 0) {
                    level.sendParticles(
                            ParticleTypes.WITCH,
                            x, riftY, z,
                            1, 0, 0, 0, 0
                    );
                }
            }

            // Energy strands being sucked in
            if (level.getGameTime() % 2 == 0) {
                for (int i = 0; i < 5; i++) {
                    double angle = Math.random() * 2 * Math.PI;
                    double dist = 3 + Math.random() * 4;
                    double x = riftX + Math.cos(angle) * dist;
                    double z = riftZ + Math.sin(angle) * dist;
                    double y = riftY + (Math.random() - 0.5) * 2;

                    // Particles being pulled toward center
                    Vec3 toCenter = new Vec3(riftX - x, riftY - y, riftZ - z).normalize().scale(0.3);

                    level.sendParticles(
                            ParticleTypes.ENCHANT,
                            x, y, z,
                            0, toCenter.x, toCenter.y, toCenter.z, 0.5
                    );
                }
            }

            // Distortion effect - reverse portal particles
            if (level.getGameTime() % 3 == 0) {
                level.sendParticles(
                        ParticleTypes.REVERSE_PORTAL,
                        riftX, riftY, riftZ,
                        10, 0.5, 0.5, 0.5, 0.2
                );
            }

            // === PULL ENEMIES ===
            if (level.getGameTime() % 5 == 0) { // Pull every 5 ticks
                level.getEntitiesOfClass(
                        net.minecraft.world.entity.LivingEntity.class,
                        new net.minecraft.world.phys.AABB(
                                riftX - PULL_RADIUS, riftY - PULL_RADIUS, riftZ - PULL_RADIUS,
                                riftX + PULL_RADIUS, riftY + PULL_RADIUS, riftZ + PULL_RADIUS
                        ),
                        entity -> entity != player && entity.isAlive()
                ).forEach(entity -> {
                    // Calculate pull direction
                    Vec3 toRift = new Vec3(
                            riftX - entity.getX(),
                            riftY - entity.getY(),
                            riftZ - entity.getZ()
                    );

                    double distance = toRift.length();
                    if (distance < 0.1) return; // Don't pull if too close

                    // Stronger pull when closer
                    double pullMult = 1.0 - (distance / PULL_RADIUS);
                    Vec3 pullVec = toRift.normalize().scale(PULL_STRENGTH * pullMult);

                    // Apply velocity
                    Vec3 currentVel = entity.getDeltaMovement();
                    entity.setDeltaMovement(currentVel.add(pullVec));
                    entity.hurtMarked = true;

                    // Visual feedback on pulled enemies
                    level.sendParticles(
                            ParticleTypes.WITCH,
                            entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                            2, 0.3, 0.3, 0.3, 0.05
                    );
                });
            }

            // Ambient sound
            if (ticksRemaining % 20 == 0) {
                level.playSound(null, riftPos.x, riftPos.y, riftPos.z,
                        SoundEvents.PORTAL_AMBIENT, SoundSource.PLAYERS, 0.5f, 0.7f);
            }

        } else {
            // Rift closes
            closeRift(level, riftPos);
            rpgData.setRiftActive(false);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§7Rift collapsed..."
            ));
        }
    }

    private static void closeRift(ServerLevel level, Vec3 pos) {
        // Implosion effect
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                pos.x, pos.y, pos.z,
                1, 0, 0, 0, 0
        );

        // Purple burst inward
        for (int i = 0; i < 60; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double pitch = (Math.random() - 0.5) * Math.PI;
            double dist = 2 + Math.random();

            double xOff = Math.cos(angle) * Math.cos(pitch) * dist;
            double yOff = Math.sin(pitch) * dist;
            double zOff = Math.sin(angle) * Math.cos(pitch) * dist;

            // Particles moving toward center
            level.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    pos.x + xOff, pos.y + yOff, pos.z + zOff,
                    0, -xOff * 0.3, -yOff * 0.3, -zOff * 0.3, 0.5
            );
        }

        level.playSound(null, pos.x, pos.y, pos.z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.5f, 1.5f);
    }
}