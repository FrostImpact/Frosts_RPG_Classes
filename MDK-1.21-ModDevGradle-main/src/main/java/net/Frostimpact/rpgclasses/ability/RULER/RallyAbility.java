package net.Frostimpact.rpgclasses.ability.RULER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class RallyAbility extends Ability {

    private static final int RALLY_DURATION = 240; // 12 seconds

    public RallyAbility() {
        super("rally");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();

        if (rpgData.isRulerRallyActive()) {
            // DEACTIVATE RALLY - Move banner back to player
            rpgData.setRulerRallyActive(false);
            rpgData.setRulerRallyTicks(0);

            Vec3 oldBannerPos = rpgData.getRulerBannerPosition();

            // NEW: Set banner to proper ground level at player position
            Vec3 newBannerPos = findGroundLevel(level, player.position());
            rpgData.setRulerBannerPosition(newBannerPos);

            // Teleport effect at old location
            level.sendParticles(
                    ParticleTypes.PORTAL,
                    oldBannerPos.x, oldBannerPos.y + 2, oldBannerPos.z,
                    80, 0.5, 1.0, 0.5, 0.5
            );

            // Arrival effect at new location
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    newBannerPos.x, newBannerPos.y + 2, newBannerPos.z,
                    80, 0.5, 1.0, 0.5, 0.5
            );

            level.playSound(null, player.blockPosition(),
                    SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.2f);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6⚔ RALLY ended! Banner returned."));

        } else {
            // ACTIVATE RALLY - Place banner at targeted location
            Vec3 lookVec = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition();

            // Raycast to find placement position (up to 10 blocks away)
            Vec3 targetPos = eyePos.add(lookVec.scale(10.0));

            // Find where the ray hits the ground
            net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                    eyePos,
                    targetPos,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    player
            );

            net.minecraft.world.phys.BlockHitResult hit = level.clip(context);
            Vec3 hitPos = hit.getLocation();

            // NEW: Find proper ground level (not inside blocks)
            Vec3 bannerPos = findGroundLevel(level, hitPos);

            rpgData.setRulerRallyActive(true);
            rpgData.setRulerRallyTicks(RALLY_DURATION);
            rpgData.setRulerBannerPosition(bannerPos);

            // Epic placement effect
            spawnBannerPlacementEffect(level, bannerPos);

            level.playSound(null, bannerPos.x, bannerPos.y, bannerPos.z,
                    SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.5f, 1.2f);

            level.playSound(null, bannerPos.x, bannerPos.y, bannerPos.z,
                    SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8f, 1.5f);

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6⚔ RALLY! Banner placed for 12s. Reactivate to recall."));
        }

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    /**
     * Finds proper ground level to place banner (above solid blocks)
     */
    private Vec3 findGroundLevel(Level level, Vec3 startPos) {
        BlockPos startBlock = BlockPos.containing(startPos);

        // Search downward up to 5 blocks to find solid ground
        for (int i = 0; i < 5; i++) {
            BlockPos checkPos = startBlock.below(i);

            if (level.getBlockState(checkPos).isSolid()) {
                // Found solid ground - place banner 1 block above it
                return new Vec3(
                        startPos.x,
                        checkPos.getY() + 1.0, // 1 block above solid ground
                        startPos.z
                );
            }
        }

        // Fallback: use original position + 1
        return new Vec3(startPos.x, startPos.y + 1.0, startPos.z);
    }

    /**
     * Epic visual effect when banner is placed
     */
    private void spawnBannerPlacementEffect(ServerLevel level, Vec3 pos) {
        // Ground impact burst
        level.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                pos.x, pos.y, pos.z,
                1, 0, 0, 0, 0
        );

        // Upward pillar of light
        for (int i = 0; i < 20; i++) {
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.x, pos.y + (i * 0.3), pos.z,
                    3, 0.1, 0, 0.1, 0.02
            );
        }

        // Expanding golden ring
        for (int ring = 1; ring <= 3; ring++) {
            int particleCount = 30 * ring;
            double radius = 3.0 * ring;

            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double x = pos.x + Math.cos(angle) * radius;
                double z = pos.z + Math.sin(angle) * radius;

                level.sendParticles(
                        ParticleTypes.FLAME,
                        x, pos.y + 0.1, z,
                        1, 0, 0, 0, 0
                );

                if (ring == 3) {
                    level.sendParticles(
                            ParticleTypes.TOTEM_OF_UNDYING,
                            x, pos.y + 0.5, z,
                            1, 0, 0.1, 0, 0.05
                    );
                }
            }
        }

        // Floating embers around banner
        for (int i = 0; i < 50; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double dist = Math.random() * 2;
            double x = pos.x + Math.cos(angle) * dist;
            double z = pos.z + Math.sin(angle) * dist;
            double y = pos.y + (Math.random() * 3);

            level.sendParticles(
                    ParticleTypes.FLAME,
                    x, y, z,
                    0, 0, 0.1, 0, 0.05
            );
        }
    }
}