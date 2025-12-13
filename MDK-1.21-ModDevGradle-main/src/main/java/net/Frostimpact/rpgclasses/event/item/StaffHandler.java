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

        Vec3 lookVec = player.getLookAngle();
        Vec3 spawnPos = player.position()
                .add(0, player.getEyeHeight() - 0.1, 0)
                .add(lookVec.scale(0.5));

        MagicMissileEntity projectile = new MagicMissileEntity(
                ModEntities.MAGIC_MISSILE.get(),
                player.level(),
                (net.minecraft.world.entity.LivingEntity) player
        );

        projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        projectile.setDeltaMovement(lookVec.scale(1.5));
        projectile.hurtMarked = true;

        player.level().addFreshEntity(projectile);

        // Play sound
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.8f, 1.2f);

        // Apply cooldown
        player.getCooldowns().addCooldown(staffStack.getItem(), COOLDOWN_TICKS);
    }
}