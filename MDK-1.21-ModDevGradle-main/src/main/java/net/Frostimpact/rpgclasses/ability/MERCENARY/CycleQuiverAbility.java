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

        net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) player.level();

        // Visual/Audio feedback
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);

        // Particle effects based on arrow type
        net.minecraft.core.particles.ParticleOptions particleType;
        switch (next) {
            case PYRE:
                particleType = net.minecraft.core.particles.ParticleTypes.FLAME;
                break;
            case SPORE:
                particleType = net.minecraft.core.particles.ParticleTypes.SPORE_BLOSSOM_AIR;
                break;
            default: // QUILL
                particleType = net.minecraft.core.particles.ParticleTypes.CRIT;
                break;
        }

        // Spiral particle effect around player
        for (int i = 0; i < 20; i++) {
            double angle = (i / 20.0) * Math.PI * 2;
            double radius = 1.0;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            double y = player.getY() + 0.5 + (i / 20.0) * 1.5;

            level.sendParticles(particleType,
                    x, y, z,
                    1, 0, 0, 0, 0);
        }

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