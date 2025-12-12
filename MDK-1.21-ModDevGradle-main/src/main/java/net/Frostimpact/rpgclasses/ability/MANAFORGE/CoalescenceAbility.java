package net.Frostimpact.rpgclasses.ability.MANAFORGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

public class CoalescenceAbility extends Ability {

    private static final int DURATION_TICKS = 120; // 6 seconds

    public CoalescenceAbility() {
        super("coalescence");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Activate Coalescence
        rpgData.setCoalescenceActive(true);
        rpgData.setCoalescenceTicks(DURATION_TICKS);
        rpgData.setCoalescenceStoredDamage(0);

        // Visual activation - purple shield aura
        ServerLevel level = player.serverLevel();

        // Create expanding sphere of particles
        for (int ring = 0; ring < 5; ring++) {
            int particlesInRing = 20 + (ring * 10);
            double radius = 0.5 + (ring * 0.4);

            for (int i = 0; i < particlesInRing; i++) {
                double angle = (2 * Math.PI * i) / particlesInRing;
                double height = -1.0 + (ring * 0.5);

                double x = player.getX() + Math.cos(angle) * radius;
                double y = player.getY() + 1.5 + height;
                double z = player.getZ() + Math.sin(angle) * radius;

                level.sendParticles(
                        ParticleTypes.WITCH,
                        x, y, z,
                        1, 0, 0, 0, 0
                );

                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        x, y, z,
                        1, 0, 0, 0, 0
                );
            }
        }

        // Central burst
        level.sendParticles(
                ParticleTypes.DRAGON_BREATH,
                player.getX(), player.getY() + 1.5, player.getZ(),
                30, 0.5, 0.5, 0.5, 0.1
        );

        // Sounds
        level.playSound(null, player.blockPosition(),
                SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.5f);

        level.playSound(null, player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§5✦ COALESCENCE! §7Converting damage to ARCANA..."
        ));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    public static void tickCoalescence(ServerPlayer player, PlayerRPGData rpgData) {
        if (!rpgData.isCoalescenceActive()) return;

        ServerLevel level = player.serverLevel();
        int ticksRemaining = rpgData.getCoalescenceTicks();

        if (ticksRemaining > 0) {
            rpgData.setCoalescenceTicks(ticksRemaining - 1);

            // Visual effect - protective purple aura
            double time = (DURATION_TICKS - ticksRemaining) * 0.3;

            // Rotating shield layers
            for (int layer = 0; layer < 2; layer++) {
                int particlesPerLayer = 15;
                double radius = 1.2 + (layer * 0.4);
                double angleOffset = time + (layer * Math.PI / 2);

                for (int i = 0; i < particlesPerLayer; i++) {
                    double angle = angleOffset + (i * 2 * Math.PI / particlesPerLayer);
                    double height = Math.sin(time * 2 + i * 0.5) * 0.5;

                    double x = player.getX() + Math.cos(angle) * radius;
                    double y = player.getY() + 1.2 + height;
                    double z = player.getZ() + Math.sin(angle) * radius;

                    // Purple shield particles
                    level.sendParticles(
                            ParticleTypes.WITCH,
                            x, y, z,
                            1, 0, 0, 0, 0
                    );

                    if (i % 2 == 0) {
                        level.sendParticles(
                                ParticleTypes.ENCHANT,
                                x, y, z,
                                1, 0, 0, 0, 0
                        );
                    }
                }
            }

            // Vertical energy streams
            if (level.getGameTime() % 5 == 0) {
                for (int i = 0; i < 3; i++) {
                    double angle = (2 * Math.PI * i) / 3;
                    double x = player.getX() + Math.cos(angle + time) * 0.8;
                    double z = player.getZ() + Math.sin(angle + time) * 0.8;

                    level.sendParticles(
                            ParticleTypes.DRAGON_BREATH,
                            x, player.getY() + 0.5, z,
                            0, 0, 0.3, 0, 0.2
                    );
                }
            }

            // Pulsing center
            if (level.getGameTime() % 10 == 0) {
                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 1.5, player.getZ(),
                        5, 0.2, 0.2, 0.2, 0.3
                );
            }

            // Ambient hum
            if (ticksRemaining % 30 == 0) {
                level.playSound(null, player.blockPosition(),
                        SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.3f, 1.5f);
            }

        } else {
            // End Coalescence - convert stored damage to ARCANA
            float storedDamage = rpgData.getCoalescenceStoredDamage();
            int arcanaGained = (int)(storedDamage * 2); // 1 damage = 2 ARCANA

            int currentArcana = rpgData.getManaforgeArcana();
            int newArcana = Math.min(100, currentArcana + arcanaGained);
            rpgData.setManaforgeArcana(newArcana);

            // Epic finale effect
            spawnCoalescenceEnd(level, player, arcanaGained);

            rpgData.setCoalescenceActive(false);
            rpgData.setCoalescenceStoredDamage(0);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§5✦ COALESCENCE ended! §a+" + arcanaGained + " ARCANA §7[" + newArcana + "/100]"
            ));
        }
    }

    private static void spawnCoalescenceEnd(ServerLevel level, ServerPlayer player, int arcanaGained) {
        // Energy absorption burst
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                player.getX(), player.getY() + 1.5, player.getZ(),
                1, 0, 0, 0, 0
        );

        // Purple energy spiraling into player
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double dist = 2 + Math.random() * 2;
            double height = (Math.random() - 0.5) * 3;

            double x = player.getX() + Math.cos(angle) * dist;
            double y = player.getY() + 1.5 + height;
            double z = player.getZ() + Math.sin(angle) * dist;

            // Particles moving toward player
            Vec3 toPlayer = new Vec3(
                    player.getX() - x,
                    player.getY() + 1.5 - y,
                    player.getZ() - z
            ).normalize().scale(0.3);

            level.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    x, y, z,
                    0, toPlayer.x, toPlayer.y, toPlayer.z, 0.5
            );

            if (i % 3 == 0) {
                level.sendParticles(
                        ParticleTypes.WITCH,
                        x, y, z,
                        0, toPlayer.x, toPlayer.y, toPlayer.z, 0.4
                );
            }
        }

        // Sound effect varies with ARCANA gained
        float pitch = 1.0f + (arcanaGained / 100.0f);
        level.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, pitch);

        level.playSound(null, player.blockPosition(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
}