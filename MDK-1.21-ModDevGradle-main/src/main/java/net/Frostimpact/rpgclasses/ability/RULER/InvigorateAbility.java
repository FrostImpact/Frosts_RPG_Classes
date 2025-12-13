package net.Frostimpact.rpgclasses.ability.RULER;

import net.Frostimpact.rpgclasses.ability.Ability;
import net.Frostimpact.rpgclasses.entity.summon.ArcherSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.KnightSummonEntity;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class InvigorateAbility extends Ability {

    private static final int GLORY_DURATION = 200; // 10 seconds

    public InvigorateAbility() {
        super("invigorate");
    }

    @Override
    public boolean execute(ServerPlayer player, PlayerRPGData rpgData) {
        ServerLevel level = player.serverLevel();
        Vec3 bannerPos = rpgData.getRulerBannerPosition();
        double radius = 12.0;

        // Find all summons in range
        List<LivingEntity> summons = level.getEntitiesOfClass(
                LivingEntity.class,
                new net.minecraft.world.phys.AABB(
                        bannerPos.x - radius, bannerPos.y - radius, bannerPos.z - radius,
                        bannerPos.x + radius, bannerPos.y + radius, bannerPos.z + radius
                ),
                entity -> (entity instanceof KnightSummonEntity || entity instanceof ArcherSummonEntity)
        );

        if (summons.isEmpty()) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§cNo summons in range of banner!"));
            return false;
        }

        // Apply GLORY to all summons
        for (LivingEntity summon : summons) {
            // Speed buff
            summon.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED,
                    GLORY_DURATION,
                    0, // Speed I = 20%
                    false,
                    true,
                    true
            ));

            // 4 bonus hearts (Absorption III = 6 hearts, but we want 4)
            summon.addEffect(new MobEffectInstance(
                    MobEffects.ABSORPTION,
                    GLORY_DURATION,
                    1, // Absorption II = 4 hearts
                    false,
                    true,
                    true
            ));

            // Visual effect
            level.sendParticles(
                    ParticleTypes.TOTEM_OF_UNDYING,
                    summon.getX(), summon.getY() + summon.getBbHeight() / 2, summon.getZ(),
                    15, 0.3, 0.3, 0.3, 0.1
            );
        }

        // Epic activation effect at banner
        level.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                bannerPos.x, bannerPos.y + 1, bannerPos.z,
                1, 0, 0, 0, 0
        );

        level.playSound(null, bannerPos.x, bannerPos.y, bannerPos.z,
                SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.5f, 1.8f);

        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§6✦ GLORY granted to " + summons.size() + " summons!"));

        // Consume resources
        rpgData.setAbilityCooldown(id, getCooldownTicks());
        rpgData.useMana(getManaCost());

        return true;
    }
}
