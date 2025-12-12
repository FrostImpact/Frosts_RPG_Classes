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

        // Charging phase
        if (ticksRemaining > 0) {
            rpgData.setSurgeTicks(ticksRemaining - 1);

            // Calculate progress (0.0 at start -> 1.0 at finish)
            float progress = 1.0f - ((float) ticksRemaining / totalTicks);

            // --- VISUAL: CONDENSING BEAM ---
            // Radius starts broad (3.0 blocks) and shrinks to tight (0.2 blocks)
            double startRadius = 3.0;
            double endRadius = 0.2;
            double currentRadius = startRadius - ((startRadius - endRadius) * progress);

            // Render the "ghost" beam tunnel (condensing)
            renderBeam(player, level, 20.0, currentRadius, false);

            // Player charging particles (keep the swirl around the body)
            level.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    2, 0.5, 0.5, 0.5, 0.1);

            // Sound: Pitch rises as we get closer to firing
            if (ticksRemaining % 5 == 0) {
                float pitch = 0.5f + (progress * 1.5f);
                level.playSound(null, player.blockPosition(),
                        SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.2f, pitch);
            }

        } else {
            // RELEASE THE BEAM!
            fireSurgeBeam(player, rpgData, level);
            rpgData.setSurgeActive(false);
        }
    }

    private static void fireSurgeBeam(ServerPlayer player, PlayerRPGData rpgData, ServerLevel level) {
        double beamLength = 20.0;
        Vec3 lookVec = player.getLookAngle().normalize();
        Vec3 eyePos = player.getEyePosition();

        // 1. VISUAL: RAPID EXPANSION
        // We pass 'true' to trigger the explosion particle logic
        renderBeam(player, level, beamLength, 0, true);

        // 2. CORE BEAM (Bright center line)
        for (double d = 0; d < beamLength; d += 0.5) {
            Vec3 pos = eyePos.add(lookVec.scale(d));
            level.sendParticles(ParticleTypes.END_ROD,
                    pos.x, pos.y, pos.z,
                    1, 0, 0, 0, 0);
        }

        // --- DAMAGE CALCULATION ---
        int chargeTime = rpgData.getSurgeChargeTime();
        float baseDamage = 15.0f;
        float bonusDamage = (chargeTime - BASE_CHARGE_TIME) * 0.5f;
        float totalDamage = baseDamage + bonusDamage;

        // Hit detection along beam
        for (double d = 0; d < beamLength; d += 0.5) {
            Vec3 hitPos = eyePos.add(lookVec.scale(d));

            level.getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    new net.minecraft.world.phys.AABB(
                            hitPos.x - 1, hitPos.y - 1, hitPos.z - 1,
                            hitPos.x + 1, hitPos.y + 1, hitPos.z + 1),
                    entity -> entity != player && entity.isAlive()
            ).forEach(entity -> {
                entity.hurt(player.damageSources().playerAttack(player), totalDamage);

                // Impact visuals
                level.sendParticles(ParticleTypes.EXPLOSION,
                        entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                        2, 0.2, 0.2, 0.2, 0.05);
            });
        }

        // Final explosion at end of beam
        Vec3 endPos = eyePos.add(lookVec.scale(beamLength));
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                endPos.x, endPos.y, endPos.z, 1, 0, 0, 0, 0);

        // Sounds
        level.playSound(null, player.blockPosition(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 2.0f, 0.8f);
        level.playSound(null, player.blockPosition(),
                SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 1.5f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§5✦ SURGE FIRED! §c" + String.format("%.1f", totalDamage) + " damage"
        ));
    }

    /**
     * Renders the beam particles.
     * @param radius The distance from the center of the beam to spawn particles.
     * @param expand If true, particles shoot outwards with velocity. If false, they are static/condensing.
     */
    private static void renderBeam(ServerPlayer player, ServerLevel level, double length, double radius, boolean expand) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle().normalize();

        // --- Vector Math: Create a coordinate system relative to the look direction ---
        // This ensures the rings are always perpendicular to the beam, even if looking up/down.
        Vec3 arbitraryUp = Math.abs(lookVec.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = lookVec.cross(arbitraryUp).normalize();
        Vec3 relativeUp = right.cross(lookVec).normalize();

        // Optimization: larger steps during charge (static) to save FPS, detailed steps during fire (expand)
        double stepSize = expand ? 0.5 : 1.0;

        for (double d = 0; d < length; d += stepSize) {
            Vec3 centerPos = eyePos.add(lookVec.scale(d));

            // Draw the ring
            int particleCount = expand ? 8 : 4; // More particles for the explosion

            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI / particleCount) * i;

                // Calculate offset on the perpendicular plane
                // We add a slight twist (d * 0.2) to make the tunnel look spiraled
                double twist = expand ? 0 : (d * 0.2);
                double finalAngle = angle + twist;

                double xOff = (right.x * Math.cos(finalAngle)) + (relativeUp.x * Math.sin(finalAngle));
                double yOff = (right.y * Math.cos(finalAngle)) + (relativeUp.y * Math.sin(finalAngle));
                double zOff = (right.z * Math.cos(finalAngle)) + (relativeUp.z * Math.sin(finalAngle));

                if (expand) {
                    // EXPANSION: Spawn at center, blast OUTWARDS using velocity
                    level.sendParticles(ParticleTypes.DRAGON_BREATH,
                            centerPos.x, centerPos.y, centerPos.z,
                            0, // Count 0 activates Velocity Mode
                            xOff * 0.3, yOff * 0.3, zOff * 0.3, // Velocity vector
                            0.5 // Speed
                    );
                } else {
                    // CONDENSING: Spawn at Radius, stay static (creates the tunnel)
                    level.sendParticles(ParticleTypes.WITCH,
                            centerPos.x + (xOff * radius),
                            centerPos.y + (yOff * radius),
                            centerPos.z + (zOff * radius),
                            1, 0, 0, 0, 0
                    );
                }
            }
        }
    }
}