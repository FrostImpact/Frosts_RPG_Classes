package net.Frostimpact.rpgclasses.event.classes;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.ability.ARTIFICER.TowerAbility;
import net.Frostimpact.rpgclasses.ability.ARTIFICER.TurretAbility;
import net.Frostimpact.rpgclasses.entity.summon.ShockTowerEntity;
import net.Frostimpact.rpgclasses.entity.summon.TurretSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.WindTowerEntity;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.PathfinderMob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ArtificerPassiveHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            if (!rpg.getCurrentClass().equals("ARTIFICER")) return;

            ServerLevel level = player.serverLevel();

            // === VISION PASSIVE: Apply glowing effect to all summons ===
            List<PathfinderMob> allSummons = new ArrayList<>();
            
            List<TurretSummonEntity> turrets = level.getEntitiesOfClass(
                    TurretSummonEntity.class,
                    player.getBoundingBox().inflate(100),
                    entity -> entity.getOwner() == player
            );
            allSummons.addAll(turrets);
            
            List<ShockTowerEntity> shockTowers = level.getEntitiesOfClass(
                    ShockTowerEntity.class,
                    player.getBoundingBox().inflate(100),
                    entity -> entity.getOwner() == player
            );
            allSummons.addAll(shockTowers);
            
            List<WindTowerEntity> windTowers = level.getEntitiesOfClass(
                    WindTowerEntity.class,
                    player.getBoundingBox().inflate(100),
                    entity -> entity.getOwner() == player
            );
            allSummons.addAll(windTowers);

            // Apply glowing effect to all summons
            for (PathfinderMob summon : allSummons) {
                if (!summon.hasEffect(MobEffects.GLOWING)) {
                    summon.addEffect(new MobEffectInstance(
                            MobEffects.GLOWING,
                            999999,
                            0,
                            false,
                            false,
                            false
                    ));
                }
            }

            // === CONSTRUCTION SYSTEM ===
            if (rpg.isArtificerConstructing()) {
                int ticks = rpg.getArtificerConstructionTicks();
                
                if (ticks > 0) {
                    // Check if player moved too far from construction position
                    double distance = player.position().distanceTo(rpg.getArtificerConstructionPos());
                    if (distance > 3.0) {
                        // Cancel construction
                        rpg.setArtificerConstructing(false);
                        rpg.setArtificerConstructionTicks(0);
                        
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§c⚙ Construction cancelled! (moved too far)"));
                        
                        level.playSound(null, player.blockPosition(),
                                SoundEvents.ANVIL_BREAK, SoundSource.PLAYERS, 0.5f, 0.8f);
                    } else {
                        // Continue construction
                        rpg.setArtificerConstructionTicks(ticks - 1);
                        
                        // Slow player during construction
                        if (!player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                            player.addEffect(new MobEffectInstance(
                                    MobEffects.MOVEMENT_SLOWDOWN,
                                    10,
                                    1,
                                    false,
                                    false,
                                    true
                            ));
                        }
                        
                        // Construction particles
                        if (level.getGameTime() % 5 == 0) {
                            level.sendParticles(
                                    ParticleTypes.CRIT,
                                    rpg.getArtificerConstructionPos().x,
                                    rpg.getArtificerConstructionPos().y + 0.5,
                                    rpg.getArtificerConstructionPos().z,
                                    3, 0.3, 0.3, 0.3, 0.05
                            );
                        }
                        
                        // Progress sound
                        if (ticks % 20 == 0) {
                            level.playSound(null, player.blockPosition(),
                                    SoundEvents.ANVIL_USE, SoundSource.PLAYERS, 0.3f, 1.5f);
                        }
                    }
                } else {
                    // Construction complete!
                    rpg.setArtificerConstructing(false);
                    
                    String type = rpg.getArtificerConstructionType();
                    if (type.equals("TURRET")) {
                        TurretAbility.finishConstruction(player, rpg);
                    } else {
                        TowerAbility.finishConstruction(player, rpg, type);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            if (!rpg.getCurrentClass().equals("ARTIFICER")) return;

            // === RESTORATION PASSIVE: Heal turrets/towers on hit ===
            if (event.getTarget() instanceof TurretSummonEntity turret && turret.getOwner() == player) {
                // Heal turret by 2 HP
                turret.heal(2.0f);
                
                // Healing effect
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            turret.getX(), turret.getY() + 1, turret.getZ(),
                            3, 0.3, 0.3, 0.3, 0
                    );
                    
                    serverLevel.playSound(null, turret.blockPosition(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);
                }
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a⚙ Turret healed! §7(+2 HP)"));
                        
                // Cancel damage
                event.setCanceled(true);
            } else if (event.getTarget() instanceof ShockTowerEntity tower && tower.getOwner() == player) {
                // Heal tower by 2 HP
                tower.heal(2.0f);
                
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            tower.getX(), tower.getY() + 1, tower.getZ(),
                            3, 0.3, 0.3, 0.3, 0
                    );
                    
                    serverLevel.playSound(null, tower.blockPosition(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);
                }
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§b⚙ Shock Tower healed! §7(+2 HP)"));
                        
                event.setCanceled(true);
            } else if (event.getTarget() instanceof WindTowerEntity tower && tower.getOwner() == player) {
                // Heal tower by 2 HP
                tower.heal(2.0f);
                
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.HEART,
                            tower.getX(), tower.getY() + 1, tower.getZ(),
                            3, 0.3, 0.3, 0.3, 0
                    );
                    
                    serverLevel.playSound(null, tower.blockPosition(),
                            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.5f);
                }
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a⚙ Wind Tower healed! §7(+2 HP)"));
                        
                event.setCanceled(true);
            }
        }
    }
}
