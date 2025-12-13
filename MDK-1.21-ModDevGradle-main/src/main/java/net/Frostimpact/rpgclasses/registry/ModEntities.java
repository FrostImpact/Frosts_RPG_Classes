package net.Frostimpact.rpgclasses.registry;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.entity.projectile.MagicMissileEntity;
import net.Frostimpact.rpgclasses.entity.projectile.SeekerArrowEntity;
import net.Frostimpact.rpgclasses.entity.projectile.StunBoltEntity;
import net.Frostimpact.rpgclasses.entity.projectile.VaultProjectileEntity;
import net.Frostimpact.rpgclasses.entity.summon.KnightSummonEntity;
import net.Frostimpact.rpgclasses.entity.summon.ArcherSummonEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, RpgClassesMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<MagicMissileEntity>> MAGIC_MISSILE =
            ENTITY_TYPES.register("magic_missile", () -> EntityType.Builder
                    .<MagicMissileEntity>of(MagicMissileEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("magic_missile"));

    public static final DeferredHolder<EntityType<?>, EntityType<SeekerArrowEntity>> SEEKER_ARROW =
            ENTITY_TYPES.register("seeker_arrow", () -> EntityType.Builder
                    .<SeekerArrowEntity>of(SeekerArrowEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("seeker_arrow"));

    public static final DeferredHolder<EntityType<?>, EntityType<VaultProjectileEntity>> VAULT_PROJECTILE =
            ENTITY_TYPES.register("vault_projectile", () -> EntityType.Builder
                    .<VaultProjectileEntity>of(VaultProjectileEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("vault_projectile"));

    public static final DeferredHolder<EntityType<?>, EntityType<StunBoltEntity>> STUN_BOLT =
            ENTITY_TYPES.register("stun_bolt", () -> EntityType.Builder
                    .<StunBoltEntity>of(StunBoltEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("stun_bolt"));

    // === RULER SUMMONS ===
    public static final DeferredHolder<EntityType<?>, EntityType<KnightSummonEntity>> KNIGHT_SUMMON =
            ENTITY_TYPES.register("knight_summon", () -> EntityType.Builder
                    .<KnightSummonEntity>of(KnightSummonEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build("knight_summon"));

    public static final DeferredHolder<EntityType<?>, EntityType<ArcherSummonEntity>> ARCHER_SUMMON =
            ENTITY_TYPES.register("archer_summon", () -> EntityType.Builder
                    .<ArcherSummonEntity>of(ArcherSummonEntity::new, MobCategory.CREATURE)
                    .sized(0.6f, 1.8f)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build("archer_summon"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}