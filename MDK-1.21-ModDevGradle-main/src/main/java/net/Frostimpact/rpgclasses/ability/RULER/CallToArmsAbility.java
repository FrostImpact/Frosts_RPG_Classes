package net.Frostimpact.rpgclasses.ability.RULER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.ArcherSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.KnightSummonEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CallToArmsAbility extends Ability {

    public CallToArmsAbility() {
        super("call_to_arms");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();
        boolean shiftPressed = player.isShiftKeyDown();

        // Count existing summons using LivingEntity
        List<LivingEntity> knights = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(100),
                entity -> entity instanceof KnightSummonEntity
        );

        List<LivingEntity> archers = level.getEntitiesOfClass(
                LivingEntity.class,
                player.getBoundingBox().inflate(100),
                entity -> entity instanceof ArcherSummonEntity
        );

        if (shiftPressed) {
            // SUMMON ARCHERS
            if (archers.size() >= 4) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cMaximum archers already summoned! (4/4)"));
                return false;
            }

            // Spawn 2 archers (or less if approaching limit)
            int toSpawn = Math.min(2, 4 - archers.size());

            for (int i = 0; i < toSpawn; i++) {
                spawnArcher(player, level, i);
            }

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§a⚔ Summoned " + toSpawn + " Archers! §7[" + (archers.size() + toSpawn) + "/4]"));

        } else {
            // SUMMON KNIGHTS
            if (knights.size() >= 4) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cMaximum knights already summoned! (4/4)"));
                return false;
            }

            // Spawn 2 knights (or less if approaching limit)
            int toSpawn = Math.min(2, 4 - knights.size());

            for (int i = 0; i < toSpawn; i++) {
                spawnKnight(player, level, i);
            }

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6⚔ Summoned " + toSpawn + " Knights! §7[" + (knights.size() + toSpawn) + "/4]"));
        }

        // Sound effect
        level.playSound(null, player.blockPosition(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 1.5f);

        // Particle burst
        level.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 1.0, 1.0, 1.0, 0.1
        );

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    private void spawnKnight(ServerPlayer player, ServerLevel level, int index) {
        // Spawn position in circle around player
        double angle = (2 * Math.PI * index) / 2;
        double x = player.getX() + Math.cos(angle) * 2;
        double z = player.getZ() + Math.sin(angle) * 2;

        KnightSummonEntity knight = new KnightSummonEntity(
                ModEntities.KNIGHT_SUMMON.get(),
                level
        );

        knight.setPos(x, player.getY(), z);
        knight.setOwner(player);
        level.addFreshEntity(knight);

        // Spawn particles
        level.sendParticles(
                ParticleTypes.FLAME,
                x, player.getY() + 1, z,
                20, 0.5, 0.5, 0.5, 0.05
        );
    }

    private void spawnArcher(ServerPlayer player, ServerLevel level, int index) {
        // Spawn position in circle around player
        double angle = (2 * Math.PI * index) / 2;
        double x = player.getX() + Math.cos(angle) * 2;
        double z = player.getZ() + Math.sin(angle) * 2;

        ArcherSummonEntity archer = new ArcherSummonEntity(
                ModEntities.ARCHER_SUMMON.get(),
                level
        );

        archer.setPos(x, player.getY(), z);
        archer.setOwner(player);
        level.addFreshEntity(archer);

        // Spawn particles
        level.sendParticles(
                ParticleTypes.GLOW,
                x, player.getY() + 1, z,
                20, 0.5, 0.5, 0.5, 0.05
        );
    }
}