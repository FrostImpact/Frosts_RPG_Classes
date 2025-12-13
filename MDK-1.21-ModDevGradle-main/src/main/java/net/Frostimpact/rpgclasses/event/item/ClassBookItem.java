package net.Frostimpact.rpgclasses.event.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClassBookItem extends Item {

    public ClassBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            // Open GUI through packet
            net.Frostimpact.rpgclasses.networking.ModMessages.sendToPlayer(
                    new net.Frostimpact.rpgclasses.networking.packet.PacketOpenClassGui(),
                    serverPlayer
            );
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}