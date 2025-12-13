package net.Frostimpact.rpgclasses.ability.MERCENARY;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class CycleQuiverAbility extends Ability {

    public CycleQuiverAbility() {
        super("cycle_quiver");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        // Cycle to next arrow type
        ArrowType current = rpgData.getMercenaryArrowType();
        ArrowType next = current.next();
        
        rpgData.setMercenaryArrowType(next);

        // Visual/Audio feedback
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);

        // Color-coded messages
        String message = switch (next) {
            case QUILL -> "§7✦ QUILL arrows equipped";
            case PYRE -> "§c✦ PYRE arrows equipped §7(15 MP/shot)";
            case SPORE -> "§a✦ SPORE arrows equipped §7(15 MP/shot)";
        };

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));

        // Consume cooldown (no mana cost)
        rpgData.setAbilityCooldown(id, getCooldownTicks());

        return true;
    }

    public enum ArrowType {
        QUILL,  // Normal arrows
        PYRE,   // Fire pool on impact
        SPORE;  // Poison cloud on impact

        public ArrowType next() {
            return switch (this) {
                case QUILL -> PYRE;
                case PYRE -> SPORE;
                case SPORE -> QUILL;
            };
        }

        public int getManaCost() {
            return this == QUILL ? 0 : 15;
        }

        public String getDisplayName() {
            return switch (this) {
                case QUILL -> "QUILL";
                case PYRE -> "PYRE";
                case SPORE -> "SPORE";
            };
        }
    }
}