package net.Frostimpact.rpgclasses.ability.MIRAGE;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class ReflectionsAbility extends Ability {

    private static final int MAX_AFTERIMAGES = 3;
    private static final double GLIDE_DISTANCE = 10.0;

    public ReflectionsAbility() {
        super("reflections");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Check if already at max afterimages
        if (rpgData.getMirageAfterimageIds().size() >= MAX_AFTERIMAGES) {
            player.sendSystemMessage(Component.literal("§cMaximum afterimages reached!"));
            return false;
        }

        // Create afterimage at player's position
        AfterimageEntity afterimage = new AfterimageEntity(ModEntities.AFTERIMAGE.get(), player.level());
        afterimage.setPos(player.getX(), player.getY(), player.getZ());
        afterimage.setOwner(player);

        // Set glide direction based on player's look direction
        Vec3 lookVec = player.getLookAngle();
        Vec3 glideDirection = new Vec3(lookVec.x, 0, lookVec.z).normalize(); // Horizontal only
        afterimage.setGlideDirection(glideDirection);

        // Spawn the entity
        player.level().addFreshEntity(afterimage);

        // Track the afterimage
        rpgData.addMirageAfterimageId(afterimage.getId());

        // Spawn particles when creating afterimage
        if (player.level() instanceof ServerLevel serverLevel) {
            // Soul particles in a circle around the spawn point
            for (int i = 0; i < 20; i++) {
                double angle = (2 * Math.PI * i) / 20;
                double offsetX = 0.5 * Math.cos(angle);
                double offsetZ = 0.5 * Math.sin(angle);
                serverLevel.sendParticles(
                    ParticleTypes.SOUL,
                    player.getX() + offsetX, player.getY() + 1, player.getZ() + offsetZ,
                    1, 0.0, 0.0, 0.0, 0.05
                );
            }
            // Additional soul fire flames upward
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY(), player.getZ(),
                15, 0.3, 0.5, 0.3, 0.05
            );
        }

        // Sound and visual feedback
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDER_PEARL_THROW, player.getSoundSource(), 1f, 1.2f);

        // Resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}
