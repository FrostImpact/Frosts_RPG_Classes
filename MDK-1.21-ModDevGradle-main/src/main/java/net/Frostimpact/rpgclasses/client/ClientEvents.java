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

        // Debug output
        if (mc.level.getGameTime() % 100 == 0) {
            System.out.println("[CLIENT] Current class: " + currentClass);
        }

        // BLADEDANCER Keys
        if (currentClass.equals("BLADEDANCER")) {
            if (KeyBinding.DASH_KEY.consumeClick()) {
                System.out.println("[CLIENT] Sending DASH ability packet");
                ModMessages.sendToServer(new PacketUseAbility("dash"));
            }

            if (KeyBinding.BLADE_DANCE_KEY.consumeClick()) {
                System.out.println("[CLIENT] Sending BLADE_DANCE ability packet");
                ModMessages.sendToServer(new PacketUseAbility("blade_dance"));
            }

            if (KeyBinding.PARRY_KEY.consumeClick()) {
                System.out.println("[CLIENT] Sending PARRY ability packet");
                ModMessages.sendToServer(new PacketUseAbility("parry"));
            }

            if (KeyBinding.FINAL_WALTZ_KEY.consumeClick()) {
                System.out.println("[CLIENT] Sending BLADE_WALTZ ability packet");
                ModMessages.sendToServer(new PacketUseAbility("blade_waltz"));
            }
        }

        // JUGGERNAUT Keys
        if (currentClass.equals("JUGGERNAUT")) {
            if (KeyBinding.SWAP_KEY.consumeClick()) {
                System.out.println("[CLIENT] Sending SWAP ability packet");
                ModMessages.sendToServer(new PacketUseAbility("swap"));
            }

            if (KeyBinding.CRUSH_KEY.consumeClick()) {
                System.out.println("[CLIENT] Sending CRUSH ability packet");
                ModMessages.sendToServer(new PacketUseAbility("crush"));
            }

            if (KeyBinding.FORTIFY_KEY.consumeClick()) {
                System.out.println("[CLIENT] Sending FORTIFY ability packet");
                ModMessages.sendToServer(new PacketUseAbility("fortify"));
            }

            if (KeyBinding.LEAP_KEY.consumeClick()) {
                System.out.println("[CLIENT] Sending LEAP ability packet");
                ModMessages.sendToServer(new PacketUseAbility("leap"));
            }
        }
    }
}