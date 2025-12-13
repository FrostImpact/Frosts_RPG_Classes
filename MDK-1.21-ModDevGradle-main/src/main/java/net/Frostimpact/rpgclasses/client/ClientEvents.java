package net.Frostimpact.rpgclasses.client;

import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketUseAbility;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.Frostimpact.rpgclasses.util.KeyBinding;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;

public class ClientEvents {

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PlayerRPGData rpg = mc.player.getData(ModAttachments.PLAYER_RPG);
        String currentClass = rpg.getCurrentClass();

        // BLADEDANCER Keys
        if (currentClass.equals("BLADEDANCER")) {
            if (KeyBinding.DASH_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("dash"));
            }
            if (KeyBinding.BLADE_DANCE_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("blade_dance"));
            }
            if (KeyBinding.PARRY_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("parry"));
            }
            if (KeyBinding.FINAL_WALTZ_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("blade_waltz"));
            }
        }

        // JUGGERNAUT Keys
        if (currentClass.equals("JUGGERNAUT")) {
            if (KeyBinding.SWAP_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("swap"));
            }
            if (KeyBinding.CRUSH_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("crush"));
            }
            if (KeyBinding.FORTIFY_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("fortify"));
            }
            if (KeyBinding.LEAP_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("leap"));
            }
        }

        // MANAFORGE Keys
        if (currentClass.equals("MANAFORGE")) {
            if (KeyBinding.MAGIC_MISSILE_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("magic_missile"));
            }
            if (KeyBinding.SURGE_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("surge"));
            }
            if (KeyBinding.OPEN_RIFT_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("open_rift"));
            }
            if (KeyBinding.COALESCENCE_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("coalescence"));
            }
        }

        // MARKSMAN Keys
        if (currentClass.equals("MARKSMAN")) {
            if (KeyBinding.SEEKERS_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("seekers"));
            }
            if (KeyBinding.VAULT_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("vault"));
            }
            if (KeyBinding.UPDRAFT_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("updraft"));
            }
            if (KeyBinding.ARROW_RAIN_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("arrow_rain"));
            }
        }

        // MERCENARY Keys
        if (currentClass.equals("MERCENARY")) {
            if (KeyBinding.STUN_BOLT_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("stun_bolt"));
            }
            if (KeyBinding.CYCLE_QUIVER_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("cycle_quiver"));
            }
            if (KeyBinding.HIRED_GUN_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("hired_gun"));
            }
        }

        // RULER Keys
        if (currentClass.equals("RULER")) {
            if (KeyBinding.CALL_TO_ARMS_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("call_to_arms"));
            }
            if (KeyBinding.INVIGORATE_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("invigorate"));
            }
            if (KeyBinding.REGROUP_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("regroup"));
            }
            if (KeyBinding.RALLY_KEY.consumeClick()) {
                ModMessages.sendToServer(new PacketUseAbility("rally"));
            }
        }
    }
}