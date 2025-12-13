package net.Frostimpact.rpgclasses.event.item;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.entity.projectile.MagicMissileEntity;
import net.Frostimpact.rpgclasses.item.StaffItem;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class StaffHandler {

    private static final int COOLDOWN_TICKS = 10; // 0.5 seconds

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.getItem() instanceof StaffItem) {
            net.Frostimpact.rpgclasses.networking.ModMessages.sendToServer(
                    new net.Frostimpact.rpgclasses.networking.packet.PacketFireStaff()
            );
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack mainHand = player.getMainHandItem();

            if (mainHand.getItem() instanceof StaffItem) {
                fireStaffProjectile(player, mainHand);
                event.setCanceled(true);
            }
        }
    }

    public static void fireStaffProjectile(Player player, ItemStack staffStack) {
        // Check cooldown
        if (player.getCooldowns().isOnCooldown(staffStack.getItem())) {
            return;
        }

        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Vec3 lookVec = player.getLookAngle();
            Vec3 start = player.position().add(0, player.getEyeHeight() - 0.1, 0);

            // Cast a ray to find hit point
            double maxDistance = 20.0;
            Vec3 end = start.add(lookVec.scale(maxDistance));

            net.minecraft.world.level.ClipContext context = new net.minecraft.world.level.ClipContext(
                    start, end,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    player
            );

            net.minecraft.world.phys.BlockHitResult blockHit = player.level().clip(context);
            Vec3 hitPos = blockHit.getLocation();
            double distance = start.distanceTo(hitPos);

            // Check for entity hits along the way
            net.minecraft.world.phys.AABB searchBox = new net.minecraft.world.phys.AABB(start, hitPos).inflate(1.0);
            java.util.List<net.minecraft.world.entity.LivingEntity> entities = serverLevel.getEntitiesOfClass(
                    net.minecraft.world.entity.LivingEntity.class,
                    searchBox,
                    e -> e != player && e.isAlive()
            );

            net.minecraft.world.entity.LivingEntity hitEntity = null;
            double closestDist = distance;

            for (net.minecraft.world.entity.LivingEntity entity : entities) {
                Vec3 toEntity = entity.position().add(0, entity.getBbHeight() / 2, 0).subtract(start);
                if (toEntity.normalize().dot(lookVec) > 0.95) {
                    double dist = start.distanceTo(entity.position());
                    if (dist < closestDist) {
                        closestDist = dist;
                        hitEntity = entity;
                        hitPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
                    }
                }
            }

            // Spawn particle trail
            int particles = (int)(closestDist * 4);
            for (int i = 0; i < particles; i++) {
                double progress = i / (double)particles;
                Vec3 particlePos = start.add(lookVec.scale(closestDist * progress));

                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.WITCH,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0, 0, 0, 0
                );
            }

            // Deal damage if hit entity
            if (hitEntity != null) {
                hitEntity.hurt(player.damageSources().magic(), 5.0f);

                // Impact particles
                serverLevel.sendParticles(
                        net.minecraft.core.particles.ParticleTypes.CRIT,
                        hitPos.x, hitPos.y, hitPos.z,
                        10, 0.2, 0.2, 0.2, 0.1
                );
            }

            // Impact particles at hit location
            serverLevel.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.WITCH,
                    hitPos.x, hitPos.y, hitPos.z,
                    15, 0.2, 0.2, 0.2, 0.1
            );
        }

        // Play sound
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.8f, 1.2f);

        // Apply cooldown
        player.getCooldowns().addCooldown(staffStack.getItem(), COOLDOWN_TICKS);
    }
}