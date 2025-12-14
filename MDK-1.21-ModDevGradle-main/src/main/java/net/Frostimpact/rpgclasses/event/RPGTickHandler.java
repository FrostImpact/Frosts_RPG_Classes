package net.Frostimpact.rpgclasses.event;

import net.Frostimpact.rpgclasses.RpgClassesMod;
import net.Frostimpact.rpgclasses.networking.packet.SyncMirageDataPacket;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = RpgClassesMod.MOD_ID)
public class RPGTickHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        PlayerRPGData rpgData = player.getData(ModAttachments.PLAYER_RPG);
        
        if (rpgData == null) return;

        // --- MIRAGE SHADOWSTEP LOGIC ---
        if (rpgData.isMirageShadowstepActive()) {
            int currentTicks = rpgData.getMirageShadowstepTicks();

            if (currentTicks > 0) {
                // Decrement the timer
                rpgData.setMirageShadowstepTicks(currentTicks - 1);
            } else {
                // Time is up
                rpgData.setMirageShadowstepActive(false);

                // If this is the Server, send a final packet to clear the client bar
                if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncMirageDataPacket(false, 0));
                }
            }
        }

        // --- ALCHEMIST CONCOCTION LOGIC ---
        if (rpgData.isAlchemistConcoction()) {
            int currentTicks = rpgData.getAlchemistConcoctionTicks();

            if (currentTicks > 0) {
                // Decrement the timer
                rpgData.setAlchemistConcoctionTicks(currentTicks - 1);
            } else {
                // Time is up - automatically put flask on cooldown
                rpgData.setAlchemistConcoction(false);
                rpgData.setAlchemistClickPattern("");
                
                if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "§c⚗ CONCOCTION expired! FLASK on cooldown."));
                    
                    // Put flask ability on cooldown
                    rpgData.setAbilityCooldown("flask", 180); // 9s
                }
            }
        }

        // --- ALCHEMIST INJECTION MOVEMENT LOCK ---
        if (rpgData.isAlchemistInjectionActive()) {
            // Prevent movement while in injection mode
            if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                // Apply slowness to simulate movement lock
                serverPlayer.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 2, 9, false, false));
            }
        }
    }
}