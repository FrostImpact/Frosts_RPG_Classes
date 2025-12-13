package net.Frostimpact.rpgclasses.event.classes;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.ability.MERCENARY.CloakAbility;
import net.Frostimpact.rpgclasses.ability.MERCENARY.CycleQuiverAbility;
import net.Frostimpact.rpgclasses.ability.MERCENARY.HiredGunAbility;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class MercenaryHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            if (!rpg.getCurrentClass().equals("MERCENARY")) return;

            ServerLevel level = player.serverLevel();

            // === CLOAK HANDLING ===
            boolean isSneaking = player.isShiftKeyDown();
            boolean cloakActive = rpg.isCloakActive();

            if (isSneaking && !cloakActive) {
                // Try to activate CLOAK
                if (rpg.getAbilityCooldown("cloak") <= 0 && rpg.getMana() >= 10) {
                    CloakAbility cloakAbility = new CloakAbility();
                    cloakAbility.execute(player, rpg);
                }
            } else if (!isSneaking && cloakActive) {
                // Deactivate CLOAK when not sneaking
                CloakAbility.deactivateCloak(player, rpg);
            }

            // === HIRED GUN TIMER ===
            if (rpg.isHiredGunActive()) {
                int ticks = rpg.getHiredGunTicks();

                if (ticks > 0) {
                    rpg.setHiredGunTicks(ticks - 1);

                    // Find target entity
                    Entity targetEntity = level.getEntity(rpg.getHiredGunTargetId());
                    
                    if (targetEntity instanceof LivingEntity target && target.isAlive()) {
                        // Visual indicator above target
                        if (ticks % 10 == 0) {
                            level.sendParticles(
                                    ParticleTypes.CRIT,
                                    target.getX(), 
                                    target.getY() + target.getBbHeight() + 1, 
                                    target.getZ(),
                                    3, 0.3, 0.1, 0.3, 0.05
                            );
                        }

                        // Countdown warning
                        if (ticks == 100 || ticks == 40) {
                            int secondsLeft = ticks / 20;
                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    "§c⚠ " + secondsLeft + " seconds remaining!"));
                        }
                    } else {
                        // Target died or despawned - end ability
                        rpg.setHiredGunActive(false);
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                "§7Target lost..."));
                    }
                } else {
                    // Time expired - failed
                    rpg.setHiredGunActive(false);
                    
                    Entity targetEntity = level.getEntity(rpg.getHiredGunTargetId());
                    if (targetEntity != null) {
                        targetEntity.setGlowingTag(false);
                    }

                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§c✦ HIRED GUN expired!"));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);

            if (!rpg.getCurrentClass().equals("MERCENARY")) return;

            // Deactivate CLOAK on attack
            if (rpg.isCloakActive()) {
                CloakAbility.deactivateCloak(player, rpg);
                
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§7CLOAK broken by attack!"));
            }
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        // Check if killer is MERCENARY with active HIRED GUN
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            PlayerRPGData rpg = killer.getData(ModAttachments.PLAYER_RPG);

            if (rpg.getCurrentClass().equals("MERCENARY") && rpg.isHiredGunActive()) {
                HiredGunAbility.onTargetKilled(killer, rpg, event.getEntity());
            }
        }
    }

    @SubscribeEvent
    public static void onArrowShoot(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Level level = event.getLevel();

        // Only handle arrows on server side
        if (level.isClientSide || !(entity instanceof AbstractArrow arrow)) return;

        // Check if arrow was shot by MERCENARY
        if (!(arrow.getOwner() instanceof ServerPlayer player)) return;

        PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);
        if (!rpg.getCurrentClass().equals("MERCENARY")) return;

        // Get current arrow type
        CycleQuiverAbility.ArrowType arrowType = rpg.getMercenaryArrowType();

        // Check mana cost for special arrows
        int manaCost = arrowType.getManaCost();
        if (manaCost > 0) {
            if (rpg.getMana() < manaCost) {
                // Not enough mana - cancel arrow and refund
                event.setCanceled(true);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§cNot enough mana for " + arrowType.getDisplayName() + " arrows!"));
                return;
            }

            // Consume mana
            rpg.useMana(manaCost);
        }

        // Apply arrow modifications
        switch (arrowType) {
            case PYRE:
                // Tag arrow as PYRE type
                arrow.addTag("mercenary_pyre");
                break;
            case SPORE:
                // Tag arrow as SPORE type
                arrow.addTag("mercenary_spore");
                break;
            case QUILL:
            default:
                // Normal arrow - no modification
                break;
        }
    }

    // Handle PYRE and SPORE arrow impacts in a separate event
    @SubscribeEvent
    public static void onArrowHit(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        if (event.getProjectile().level().isClientSide) return;

        ServerLevel level = (ServerLevel) arrow.level();
        double x = arrow.getX();
        double y = arrow.getY();
        double z = arrow.getZ();

        // PYRE - Fire pool
        if (arrow.getTags().contains("mercenary_pyre")) {
            // Create fire particles
            level.sendParticles(
                    ParticleTypes.FLAME,
                    x, y, z,
                    30, 1.5, 0.1, 1.5, 0.05
            );

            // Set fire in area
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos firePos = new BlockPos(
                            (int) x + dx, (int) y, (int) z + dz);
                    if (level.getBlockState(firePos).isAir()) {
                        level.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
            }

            // Ignite nearby entities
            level.getEntitiesOfClass(
                    LivingEntity.class,
                    arrow.getBoundingBox().inflate(2.0),
                    e -> e.isAlive()
            ).forEach(e -> e.setRemainingFireTicks(100));
        }

        // SPORE - Poison cloud
        if (arrow.getTags().contains("mercenary_spore")) {
            // Create poison cloud particles
            for (int i = 0; i < 50; i++) {
                double dx = (Math.random() - 0.5) * 3;
                double dy = Math.random() * 2;
                double dz = (Math.random() - 0.5) * 3;

                level.sendParticles(
                        ParticleTypes.SPORE_BLOSSOM_AIR,
                        x + dx, y + dy, z + dz,
                        1, 0, 0, 0, 0
                );
            }

            // Apply poison to nearby entities
            level.getEntitiesOfClass(
                    LivingEntity.class,
                    arrow.getBoundingBox().inflate(3.0),
                    e -> e.isAlive() && e != arrow.getOwner()
            ).forEach(e -> {
                e.addEffect(new MobEffectInstance(
                        MobEffects.POISON,
                        100, // 5 seconds
                        1    // Poison II
                ));
            });
        }
    }
}