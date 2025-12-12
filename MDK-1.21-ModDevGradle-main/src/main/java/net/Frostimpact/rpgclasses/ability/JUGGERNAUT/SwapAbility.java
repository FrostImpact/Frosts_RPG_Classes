package net.Frostimpact.rpgclasses.ability.JUGGERNAUT;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.joml.Vector3f;

public class SwapAbility extends Ability {

    public SwapAbility() {
        super("swap");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        boolean isShieldMode = rpgData.isJuggernautShieldMode();
        int charge = rpgData.getJuggernautCharge();
        ServerLevel level = (ServerLevel) player.level();

        if (isShieldMode) {
            // === SHIELD → SHATTER ===
            rpgData.setJuggernautShieldMode(false);
            rpgData.startChargeDecay();



            // Refresh LEAP cooldown
            rpgData.setAbilityCooldown("leap", 0);

            // 1. AOE damage
            double radius = 4.0;
            float baseDamage = charge / 10.0f; // 1 damage per 10 charge

            player.level().getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    player.getBoundingBox().inflate(radius),
                    entity -> entity != player && entity.isAlive()
            ).forEach(entity -> {
                entity.hurt(player.damageSources().playerAttack(player), baseDamage);
            });

            // 2. VISUALS: Red Aura & Red Sphere Pulse
            Vector3f redColor = new Vector3f(1.0f, 0.0f, 0.0f);

            spawnAura(level, player, redColor); // Cylinder aura
            spawnSphericalPulse(level, player, redColor); // 3D Expanding Sphere

            // 3. SOUND
            level.playSound(null, player.blockPosition(),
                    SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1.0f, 0.5f);

            player.sendSystemMessage(Component.literal("§c⚔ SHATTER MODE §7| Charge: " + charge));

        } else {
            // === SHATTER → SHIELD ===
            rpgData.setJuggernautShieldMode(true);
            rpgData.stopChargeDecay();

            // 1. VISUALS: Blue Aura
            Vector3f blueColor = new Vector3f(0.0f, 0.8f, 1.0f); // Light Blue
            spawnAura(level, player, blueColor);

            // 2. LOGIC: Grant absorption
            if (charge >= 50) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.ABSORPTION,
                        999999,
                        2, // 6 hearts
                        false,
                        true,
                        true
                ));
                player.sendSystemMessage(Component.literal("§b🛡 SHIELD MODE §7| §a+6 Absorption Hearts"));
            } else {
                player.sendSystemMessage(Component.literal("§b🛡 SHIELD MODE"));
            }

            player.removeEffect(MobEffects.DAMAGE_BOOST);

            level.playSound(null, player.blockPosition(),
                    SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.8f);
        }

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    // === HELPER METHODS ===

    // Spawns a temporary cylinder aura around the player
    private void spawnAura(ServerLevel level, ServerPlayer player, Vector3f color) {
        net.minecraft.core.particles.DustParticleOptions dust =
                new net.minecraft.core.particles.DustParticleOptions(color, 1.0f);

        int particles = 30;
        double radius = 0.75;
        double height = 2.0;

        for (int i = 0; i < particles; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double yOffset = level.random.nextDouble() * height;

            double x = player.getX() + Math.cos(angle) * radius;
            double y = player.getY() + yOffset;
            double z = player.getZ() + Math.sin(angle) * radius;

            level.sendParticles(dust, x, y, z, 1, 0, 0, 0, 0);
        }
    }

    // Spawns an EXPANDING SPHERE of particles
    private void spawnSphericalPulse(ServerLevel level, ServerPlayer player, Vector3f color) {
        net.minecraft.core.particles.DustParticleOptions dust =
                new net.minecraft.core.particles.DustParticleOptions(color, 1.5f);

        int particles = 150; // High count for a dense sphere look
        double sphereSpeed = 0.5; // How fast the sphere expands

        // Using Fibonacci Sphere algorithm to distribute points evenly on a sphere surface
        double phi = Math.PI * (3. - Math.sqrt(5.)); // Golden angle

        for (int i = 0; i < particles; i++) {
            double y = 1 - (i / (float) (particles - 1)) * 2; // y goes from 1 to -1
            double radius = Math.sqrt(1 - y * y); // radius at y

            double theta = phi * i; // golden angle increment

            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;

            // Apply velocity directly (particles move away from center)
            level.sendParticles(dust,
                    player.getX(), player.getY() + 1.0, player.getZ(), // Start at player's center mass (approx)
                    0, // Count 0 allows velocity
                    x * sphereSpeed, y * sphereSpeed, z * sphereSpeed, // XYZ Velocity
                    1.0 // Speed Multiplier
            );
        }
    }
}