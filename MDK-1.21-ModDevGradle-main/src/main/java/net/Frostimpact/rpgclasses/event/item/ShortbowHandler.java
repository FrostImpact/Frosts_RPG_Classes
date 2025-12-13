package net.Frostimpact.rpgclasses.event.item;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.item.ShortbowItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ShortbowHandler {

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        // CLIENT SIDE: Detect left-click in air
        // We need to send this to server
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.getItem() instanceof ShortbowItem) {
            // Send packet to server to fire arrow
            // We'll use the networking system
            net.Frostimpact.rpgclasses.networking.ModMessages.sendToServer(
                    new net.Frostimpact.rpgclasses.networking.packet.PacketFireShortbow()
            );
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        // Also fire when left-clicking a block (so it feels responsive)
        if (event.getEntity() instanceof ServerPlayer player) {
            ItemStack mainHand = player.getMainHandItem();

            if (mainHand.getItem() instanceof ShortbowItem) {
                fireShortbowArrow(player, mainHand);
                event.setCanceled(true); // Don't break the block
            }
        }
    }

    public static void fireShortbowArrow(Player player, ItemStack bowStack) {
        Level level = player.level();

        // Check for arrows in inventory
        ItemStack arrowStack = player.getProjectile(bowStack);
        if (arrowStack.isEmpty()) {
            // No arrows - play fail sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 1.0f, 1.0f);
            return;
        }

        // Create arrow projectile
        AbstractArrow arrow = createArrow(level, arrowStack, player);

        // Set arrow properties
        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.0f, 1.0f);

        // Lower damage for quick shots
        arrow.setBaseDamage(4.0); // Normal bow fully charged = 9.0 damage
        arrow.setCritArrow(false); // No crits for quick shots

        // Apply enchantments
        arrow = customArrow(arrow, arrowStack, bowStack);

        // Add arrow to world
        level.addFreshEntity(arrow);

        // Play shoot sound
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.2f);

        // Consume arrow (unless in creative or Infinity)
        if (!player.getAbilities().instabuild) {
            boolean hasInfinity = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                    level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                            .getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.INFINITY),
                    bowStack
            ) > 0;

            if (!hasInfinity || arrowStack.getItem() != Items.ARROW) {
                arrowStack.shrink(1);
                if (arrowStack.isEmpty()) {
                    player.getInventory().removeItem(arrowStack);
                }
            }
        }

        // Damage bow slightly
        bowStack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(player.getUsedItemHand()));
    }

    private static AbstractArrow createArrow(Level level, ItemStack arrowStack, Player player) {
        if (arrowStack.getItem() instanceof ArrowItem arrowItem) {
            return arrowItem.createArrow(level, arrowStack, player, arrowStack);
        }

        return new net.minecraft.world.entity.projectile.Arrow(level, player, arrowStack.copyWithCount(1), null);
    }

    private static AbstractArrow customArrow(AbstractArrow arrow, ItemStack arrowStack, ItemStack bowStack) {
        var registryAccess = arrow.level().registryAccess();
        var enchantmentRegistry = registryAccess.registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);

        // Flame
        int flameLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                enchantmentRegistry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.FLAME),
                bowStack
        );
        if (flameLevel > 0) {
            arrow.setRemainingFireTicks(100);
        }

        // Power
        int powerLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                enchantmentRegistry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.POWER),
                bowStack
        );
        if (powerLevel > 0) {
            arrow.setBaseDamage(arrow.getBaseDamage() + (powerLevel * 0.5) + 0.5);
        }

        // Punch
        int punchLevel = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(
                enchantmentRegistry.getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.PUNCH),
                bowStack
        );
        //if (punchLevel > 0) {
            //arrow.setKnockback(punchLevel);
        //}

        return arrow;
    }
}