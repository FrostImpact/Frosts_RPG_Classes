package net.Frostimpact.rpgclasses.ability.ARTIFICER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.ShockTowerEntity;
import net.Frostimpact.rpgclasses.entity.summon.WindTowerEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.Frostimpact.rpgclasses.rpg.PlayerStats;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;

import java.util.List;

public class TowerAbility extends Ability {

    private static final int CONSTRUCTION_TIME = 100; // 5 seconds
    private static final int MAX_TOWERS = 2;

    public TowerAbility() {
        super("tower");
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

        // Determine tower type based on shift
        boolean shiftPressed = player.isShiftKeyDown();
        String towerType = shiftPressed ? "WIND" : "SHOCK";

        // Start construction
        rpgData.setArtificerConstructing(true);
        rpgData.setArtificerConstructionTicks(CONSTRUCTION_TIME);
        rpgData.setArtificerConstructionType(towerType);
        rpgData.setArtificerConstructionPos(player.position());

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7⚙ Constructing " + towerType + " TOWER... (5s)"));

        // Enhanced construction start effect - rising particles and energy convergence
        level.sendParticles(
                shiftPressed ? ParticleTypes.CLOUD : ParticleTypes.ELECTRIC_SPARK,
                player.getX(), player.getY() + 1, player.getZ(),
                40, 0.5, 0.5, 0.5, 0.1
        );
        
        // Energy convergence particles
        for (int i = 0; i < 12; i++) {
            double angle = (2 * Math.PI * i) / 12;
            double distance = 2.5;
            double x = player.getX() + Math.cos(angle) * distance;
            double z = player.getZ() + Math.sin(angle) * distance;
            level.sendParticles(
                    shiftPressed ? ParticleTypes.CLOUD : ParticleTypes.ELECTRIC_SPARK,
                    x, player.getY() + 0.5, z,
                    2, 0, 0.3, 0, 0.05
            );
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.5f, 1.0f);

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }

    public static void finishConstruction(ServerPlayer player, PlayerRPGData rpgData, String towerType) {
        ServerLevel level = player.serverLevel();

        // Count existing towers (both types)
        List<ShockTowerEntity> shockTowers = level.getEntitiesOfClass(
                ShockTowerEntity.class,
                player.getBoundingBox().inflate(100),
                entity -> entity.getOwner() == player
        );

        List<WindTowerEntity> windTowers = level.getEntitiesOfClass(
                WindTowerEntity.class,
                player.getBoundingBox().inflate(100),
                entity -> entity.getOwner() == player
        );

        int totalTowers = shockTowers.size() + windTowers.size();

        // Remove oldest if at max
        if (totalTowers >= MAX_TOWERS) {
            // Remove the oldest tower (prefer removing same type)
            PathfinderMob toRemove = null;
            
            if (!shockTowers.isEmpty() && towerType.equals("SHOCK")) {
                toRemove = shockTowers.get(0);
            } else if (!windTowers.isEmpty() && towerType.equals("WIND")) {
                toRemove = windTowers.get(0);
            } else if (!shockTowers.isEmpty()) {
                toRemove = shockTowers.get(0);
            } else if (!windTowers.isEmpty()) {
                toRemove = windTowers.get(0);
            }
            
            if (toRemove != null) {
                level.sendParticles(
                        ParticleTypes.POOF,
                        toRemove.getX(), toRemove.getY() + 0.5, toRemove.getZ(),
                        20, 0.3, 0.3, 0.3, 0.1
                );
                toRemove.discard();
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7⚙ Oldest tower removed (max 2)"));
            }
        }

        // Spawn appropriate tower
        PathfinderMob tower;
        if (towerType.equals("WIND")) {
            tower = new WindTowerEntity(ModEntities.WIND_TOWER.get(), level);
        } else {
            tower = new ShockTowerEntity(ModEntities.SHOCK_TOWER.get(), level);
        }

        tower.setPos(
                rpgData.getArtificerConstructionPos().x,
                rpgData.getArtificerConstructionPos().y,
                rpgData.getArtificerConstructionPos().z
        );
        
        // Set tower damage based on player's damage multiplier
        PlayerStats stats = player.getData(ModAttachments.PLAYER_STATS);
        double playerDamageMultiplier = (stats != null) ? stats.getDamageMultiplier() : 1.0;
        
        if (tower instanceof ShockTowerEntity shock) {
            shock.setOwner(player);
            shock.setBaseDamage(3.0 * playerDamageMultiplier);
        } else if (tower instanceof WindTowerEntity wind) {
            wind.setOwner(player);
            wind.setBaseDamage(2.0 * playerDamageMultiplier);
        }
        
        level.addFreshEntity(tower);

        // Spawn effects
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                tower.getX(), tower.getY() + 0.5, tower.getZ(),
                1, 0, 0, 0, 0
        );

        level.sendParticles(
                towerType.equals("WIND") ? ParticleTypes.CLOUD : ParticleTypes.ELECTRIC_SPARK,
                tower.getX(), tower.getY() + 0.5, tower.getZ(),
                40, 0.5, 0.5, 0.5, 0.1
        );

        level.playSound(null, tower.blockPosition(),
                SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 1.0f);

        totalTowers = Math.min(totalTowers + 1, MAX_TOWERS);
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§7⚙ " + towerType + " TOWER constructed! §7[" + totalTowers + "/2]"));
    }
}
