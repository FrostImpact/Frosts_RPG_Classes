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

import java.util.List;

public class CallToArmsAbility extends Ability {

    public CallToArmsAbility() {
        super("call_to_arms");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();
        boolean shiftPressed = player.isShiftKeyDown();

        double range = 100.0;

        // Count existing summons
        List<KnightSummonEntity> knights = level.getEntitiesOfClass(
                KnightSummonEntity.class,
                player.getBoundingBox().inflate(range),
                entity -> entity.getOwner() == player // Optional: Filter by owner right here if you want
        );

        List<ArcherSummonEntity> archers = level.getEntitiesOfClass(
                ArcherSummonEntity.class,
                player.getBoundingBox().inflate(range),
                entity -> entity.getOwner() == player
        );

        if (shiftPressed) {
            // SUMMON ARCHERS
            if (archers.size() >= 4) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cMaximum archers already summoned! (4/4)"));
                return false;
            }

            int toSpawn = Math.min(2, 4 - archers.size());

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§a⚔ Summoning " + toSpawn + " Archers...")); // DEBUG

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

            int toSpawn = Math.min(2, 4 - knights.size());

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6⚔ Summoning " + toSpawn + " Knights...")); // DEBUG

            for (int i = 0; i < toSpawn; i++) {
                spawnKnight(player, level, i);
            }

            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§6⚔ Summoned " + toSpawn + " Knights! §7[" + (knights.size() + toSpawn) + "/4]"));
        }

        // Enhanced visual effects
        level.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 1.5, 1.5, 1.5, 0.2
        );

        // Summoning circle effect
        for (int ring = 0; ring < 3; ring++) {
            int particleCount = 20 * (ring + 1);
            double radius = 2.0 + (ring * 0.5);

            for (int i = 0; i < particleCount; i++) {
                double angle = (2 * Math.PI * i) / particleCount;
                double x = player.getX() + Math.cos(angle) * radius;
                double z = player.getZ() + Math.sin(angle) * radius;

                level.sendParticles(
                        ParticleTypes.ENCHANT,
                        x, player.getY() + 0.1, z,
                        1, 0, 0, 0, 0
                );
            }
        }

        // Sound effect
        level.playSound(null, player.blockPosition(),
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 1.5f);

        level.playSound(null, player.blockPosition(),
                SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 1.2f);

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
        double y = player.getY(); // Use player's Y position

        KnightSummonEntity knight = new KnightSummonEntity(
                ModEntities.KNIGHT_SUMMON.get(),
                level
        );

        knight.setPos(x, y, z);
        knight.setOwner(player);
        level.addFreshEntity(knight);

        // Epic spawn particles
        level.sendParticles(
                ParticleTypes.FLAME,
                x, y + 1, z,
                30, 0.3, 0.5, 0.3, 0.1
        );

        level.sendParticles(
                ParticleTypes.SMOKE,
                x, y, z,
                20, 0.3, 0.1, 0.3, 0.05
        );
    }

    private void spawnArcher(ServerPlayer player, ServerLevel level, int index) {
        // Spawn position in circle around player
        double angle = (2 * Math.PI * index) / 2;
        double x = player.getX() + Math.cos(angle) * 2;
        double z = player.getZ() + Math.sin(angle) * 2;
        double y = player.getY(); // Use player's Y position

        ArcherSummonEntity archer = new ArcherSummonEntity(
                ModEntities.ARCHER_SUMMON.get(),
                level
        );

        archer.setPos(x, y, z);
        archer.setOwner(player);
        level.addFreshEntity(archer);

        // Epic spawn particles
        level.sendParticles(
                ParticleTypes.GLOW,
                x, y + 1, z,
                30, 0.3, 0.5, 0.3, 0.1
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                x, y, z,
                20, 0.3, 0.1, 0.3, 0.05
        );
    }
}