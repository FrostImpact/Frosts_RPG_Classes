package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.entity.summon.ArcherSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.KnightSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity;
import net.Frostimpact.rpgclasses.registry.ModEntities;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * This class handles events that occur on both client and server.
 * It registers itself automatically via @EventBusSubscriber.
 */
@EventBusSubscriber(modid = RpgClassesMod.MOD_ID)
public class ModEvents {

    /**
     * This event runs when a player respawns (Clone).
     * It preserves RPG data from the old player to the new one.
     */
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        // Get the old data (from the dead body)
        PlayerRPGData oldData = event.getOriginal().getData(ModAttachments.PLAYER_RPG);
        // Get the new data (for the new body)
        PlayerRPGData newData = event.getEntity().getData(ModAttachments.PLAYER_RPG);

        // Copy the class over (always preserve)
        newData.setCurrentClass(oldData.getCurrentClass());

        newData.setMana(newData.getMaxMana());

        // BLADEMANCER: Reset tempo on death
        newData.resetTempo();
        newData.setFinalWaltzActive(false);
        newData.setParryActive(false);
    }

    /**
     * This event runs when a living entity takes damage.
     * We use it to handle Blade Dance blade removal and Parry success.
     */
    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        // Check if the damaged entity is a player
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            // PARRY HANDLING
            if (rpg.isParryActive() && !rpg.isParrySuccessful()) {
                // Mark parry as successful
                rpg.setParrySuccessful(true);

                // Refresh DASH cooldown
                rpg.setAbilityCooldown("dash", 0);

                // Grant 3 TEMPO stacks
                rpg.setTempoStacks(rpg.getTempoStacks() + 3);

                // Apply TEMPO effect if needed
                if (rpg.getTempoStacks() >= 3) {
                    int strengthLevel = 0;

                    // During Final Waltz, check for Strength II at 6 stacks
                    if (rpg.isFinalWaltzActive() && rpg.getTempoStacks() >= 6) {
                        strengthLevel = 1; // Strength II
                    }

                    player.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST,
                            999999,
                            strengthLevel,
                            false,
                            false,
                            true
                    ));
                    rpg.setTempoActive(true);
                }

                // Success feedback
                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.5f, 2.0f);

                ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                        net.minecraft.core.particles.ParticleTypes.CRIT,
                        player.getX(), player.getY() + 1, player.getZ(),
                        20, 0.5, 0.5, 0.5, 0.1
                );

                player.sendSystemMessage(Component.literal("§e⚔ PERFECT PARRY! §7DASH REFRESHED"));
            }

            // BLADE DANCE HANDLING
            if (rpg.isBladeDanceActive() && rpg.getBladeDanceBlades() > 0) {
                // Remove one sword entity first
                if (!rpg.getBladeDanceSwordIds().isEmpty()) {
                    Integer swordId = rpg.getBladeDanceSwordIds().remove(rpg.getBladeDanceSwordIds().size() - 1);
                    net.minecraft.world.entity.Entity entity = player.level().getEntity(swordId);
                    if (entity != null) {
                        entity.discard();
                    }
                }

                // Remove blade from counter
                rpg.removeBlade();

                // Heal 2 HP
                player.heal(2.0f);

                // Visual/audio feedback
                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.GLASS_BREAK,
                        SoundSource.PLAYERS, 0.8f, 1.5f);

                player.sendSystemMessage(Component.literal(
                        "§cBlade shattered! §a+2 HP §7(" + rpg.getBladeDanceBlades() + " blades remaining)"
                ));

                // If no blades left, end the ability early
                if (rpg.getBladeDanceBlades() <= 0) {
                    // Clean up any remaining sword entities
                    for (Integer swordId : rpg.getBladeDanceSwordIds()) {
                        net.minecraft.world.entity.Entity entity = player.level().getEntity(swordId);
                        if (entity != null) {
                            entity.discard();
                        }
                    }
                    rpg.clearBladeDanceSwords();

                    rpg.setBladeDanceActive(false);
                    player.sendSystemMessage(Component.literal("§cAll blades destroyed!"));
                }
            }
        }
    }

    /**
     * This event runs when a player attacks an entity.
     * We use it to handle the TEMPO passive ability.
     */
    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            // CHECK: Only trigger if attack is fully charged
            // getAttackStrengthScale(0.5f) returns 1.0 for fully charged attacks
            if (player.getAttackStrengthScale(0.5f) < 1.0f) {
                return; // Ignore spam clicks
            }

            // Only trigger TEMPO for Bladedancer class
            if (rpg.getCurrentClass().equals("BLADEDANCER")) {

                // Add a tempo stack
                rpg.addTempoStack();

                // Handle TEMPO logic based on Final Waltz state
                if (rpg.isFinalWaltzActive()) {
                    // FINAL WALTZ: TEMPO can overflow
                    if (rpg.getTempoStacks() == 3) {
                        // Grant Strength I
                        player.addEffect(new MobEffectInstance(
                                MobEffects.DAMAGE_BOOST,
                                999999,
                                0, // Strength I
                                false,
                                false,
                                true
                        ));
                        rpg.setTempoActive(true);

                        player.level().playSound(null, player.blockPosition(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP,
                                SoundSource.PLAYERS, 0.5f, 1.5f);
                    } else if (rpg.getTempoStacks() >= 6) {
                        // Upgrade to Strength II
                        player.removeEffect(MobEffects.DAMAGE_BOOST);
                        player.addEffect(new MobEffectInstance(
                                MobEffects.DAMAGE_BOOST,
                                999999,
                                1, // Strength II
                                false,
                                false,
                                true
                        ));

                        // Track overflow stacks
                        int overflow = rpg.getTempoStacks() - 6;
                        rpg.setFinalWaltzOverflow(overflow);

                        player.level().playSound(null, player.blockPosition(),
                                SoundEvents.PLAYER_LEVELUP,
                                SoundSource.PLAYERS, 0.5f, 2.0f);
                    }
                } else {
                    // NORMAL: Standard TEMPO behavior
                    if (rpg.getTempoStacks() == 3) {
                        // Grant Strength I
                        player.addEffect(new MobEffectInstance(
                                MobEffects.DAMAGE_BOOST,
                                999999,
                                0, // Strength I
                                false,
                                false,
                                true
                        ));
                        rpg.setTempoActive(true);

                        player.level().playSound(null, player.blockPosition(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP,
                                SoundSource.PLAYERS, 0.5f, 1.5f);

                    } else if (rpg.getTempoStacks() >= 4) {
                        // 4th hit - remove Strength and reset
                        player.removeEffect(MobEffects.DAMAGE_BOOST);
                        rpg.resetTempo();

                        player.level().playSound(null, player.blockPosition(),
                                SoundEvents.EXPERIENCE_ORB_PICKUP,
                                SoundSource.PLAYERS, 0.5f, 0.8f);
                    }
                }
            }

            // MIRAGE: ESSENCE OF CREATION passive - Afterimages mimic attacks
            if (rpg.getCurrentClass().equals("MIRAGE")) {
                if (player.getAttackStrengthScale(0.5f) >= 1.0f) {
                    // Get the target entity
                    net.minecraft.world.entity.Entity target = event.getTarget();
                    
                    // Get player's look direction
                    net.minecraft.world.phys.Vec3 playerLookVec = player.getLookAngle();
                    
                    // Make all afterimages attack in the same direction (30% damage)
                    for (Integer id : rpg.getMirageAfterimageIds()) {
                        if (player.level().getEntity(id) instanceof net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity afterimage) {
                            // Trigger swing animation on afterimage
                            afterimage.performSwingAnimation();
                            
                            // Find nearest entity in the direction the afterimage is "looking"
                            net.minecraft.world.phys.Vec3 afterimagePos = afterimage.position();
                            
                            // Use player's attack damage
                            float playerDamage = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
                            float afterimageDamage = playerDamage * 0.3f;
                            
                            // Raycast to find entities in front of afterimage
                            net.minecraft.world.phys.Vec3 lookEnd = afterimagePos.add(playerLookVec.scale(5.0));
                            
                            java.util.List<net.minecraft.world.entity.Entity> nearbyEntities = player.level().getEntities(
                                    afterimage,
                                    new net.minecraft.world.phys.AABB(
                                            afterimagePos.x - 3, afterimagePos.y - 2, afterimagePos.z - 3,
                                            afterimagePos.x + 3, afterimagePos.y + 2, afterimagePos.z + 3
                                    )
                            );
                            
                            for (net.minecraft.world.entity.Entity entity : nearbyEntities) {
                                if (entity instanceof net.minecraft.world.entity.LivingEntity living && 
                                    entity != player && 
                                    !(entity instanceof net.Frostimpact.rpgclasses.entity.summon.AfterimageEntity)) {
                                    // Deal damage
                                    living.hurt(player.damageSources().playerAttack(player), afterimageDamage);
                                    
                                    // Visual feedback with slash particles at impact location
                                    ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                                            net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,
                                            living.getX(), living.getY() + living.getBbHeight() / 2, living.getZ(),
                                            1, 0.0, 0.0, 0.0, 0.0
                                    );
                                    
                                    // Additional crit particles
                                    ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                                            net.minecraft.core.particles.ParticleTypes.CRIT,
                                            living.getX(), living.getY() + living.getBbHeight() / 2, living.getZ(),
                                            5, 0.3, 0.3, 0.3, 0.1
                                    );
                                    
                                    // Soul particles during swing at afterimage location
                                    ((net.minecraft.server.level.ServerLevel) player.level()).sendParticles(
                                            net.minecraft.core.particles.ParticleTypes.SOUL,
                                            afterimagePos.x, afterimagePos.y + 1, afterimagePos.z,
                                            3, 0.2, 0.2, 0.2, 0.05
                                    );
                                    
                                    break; // Only hit one entity per afterimage
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    @EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    public class ModEventBusEvents {

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            // Link the Knight entity to the Knight attributes
            event.put(ModEntities.KNIGHT_SUMMON.get(), KnightSummonEntity.createAttributes().build());

            // Link the Archer entity to the Archer attributes
            event.put(ModEntities.ARCHER_SUMMON.get(), ArcherSummonEntity.createAttributes().build());

            // Link the Artificer entities
            event.put(ModEntities.TURRET_SUMMON.get(), net.Frostimpact.rpgclasses.entity.summon.TurretSummonEntity.createAttributes().build());
            event.put(ModEntities.SHOCK_TOWER.get(), net.Frostimpact.rpgclasses.entity.summon.ShockTowerEntity.createAttributes().build());
            event.put(ModEntities.WIND_TOWER.get(), net.Frostimpact.rpgclasses.entity.summon.WindTowerEntity.createAttributes().build());

            // Link the Mirage entities
            event.put(ModEntities.AFTERIMAGE.get(), AfterimageEntity.createAttributes().build());

            System.out.println("RPG Classes: Entity Attributes Registered!");
        }
    }


}