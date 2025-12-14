package net.Frostimpact.rpgclasses.ability.ARTIFICER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.TurretSummonEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.Frostimpact.rpgclasses.rpg.PlayerStats;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;

public class TurretAbility extends Ability {

    private static final int CONSTRUCTION_TIME = 60; // 3 seconds
    private static final int MAX_TURRETS = 3;

    public TurretAbility() {
        super("turret");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();

        // Check if already constructing
        if (rpgData.isArtificerConstructing()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cAlready constructing!"));
            return false;
        }

        // Start construction
        rpgData.setArtificerConstructing(true);
        rpgData.setArtificerConstructionTicks(CONSTRUCTION_TIME);
        rpgData.setArtificerConstructionType("TURRET");
        rpgData.setArtificerConstructionPos(player.position());

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7⚙ Constructing TURRET... (3s)"));

        // Enhanced construction start effect - rising particles and energy convergence
        level.sendParticles(
                ParticleTypes.CRIT,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.5, 0.5, 0.5, 0.1
        );
        
        // Energy convergence particles
        for (int i = 0; i < 8; i++) {
            double angle = (2 * Math.PI * i) / 8;
            double distance = 2.0;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            level.sendParticles(
                    ParticleTypes.FLAME,
                    x, player.getY() + 0.5, z,
                    1, 0, 0.2, 0, 0.05
            );
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5f, 1.2f);

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    public static void finishConstruction(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();

        // Check turret count
        List<TurretSummonEntity> turrets = level.getEntitiesOfClass(
                TurretSummonEntity.class,
                player.getBoundingBox().inflate(100),
                entity -> entity.getOwner() == player
        );

        // Remove oldest if at max
        if (turrets.size() >= MAX_TURRETS) {
            TurretSummonEntity oldest = turrets.get(0);
            oldest.discard();
            
            // Removal effect
            level.sendParticles(
                    ParticleTypes.POOF,
                    oldest.getX(), oldest.getY() + 0.5, oldest.getZ(),
                    20, 0.3, 0.3, 0.3, 0.1
            );
            
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§7⚙ Oldest turret removed (max 3)"));
        }

        // Spawn turret at construction position
        TurretSummonEntity turret = new TurretSummonEntity(
                ModEntities.TURRET_SUMMON.get(),
                level
        );

        turret.setPos(
                rpgData.getArtificerConstructionPos().x,
                rpgData.getArtificerConstructionPos().y,
                rpgData.getArtificerConstructionPos().z
        );
        turret.setOwner(player);
        
        // Set turret damage based on player's damage multiplier
        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        if (stats != null) {
            double playerDamage = stats.getDamageMultiplier();
            turret.setBaseDamage(4.0 * playerDamage);
        }
        
        level.addFreshEntity(turret);

        // Spawn effects
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                turret.getX(), turret.getY() + 0.5, turret.getZ(),
                1, 0, 0, 0, 0
        );

        level.sendParticles(
                ParticleTypes.FLAME,
                turret.getX(), turret.getY() + 0.5, turret.getZ(),
                30, 0.5, 0.5, 0.5, 0.1
        );

        level.playSound(null, turret.blockPosition(),
                SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 1.2f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7⚙ TURRET constructed! §7[" + (turrets.size() + 1) + "/3]"));
    }
}
