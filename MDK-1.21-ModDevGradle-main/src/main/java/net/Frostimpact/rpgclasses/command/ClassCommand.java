package net.Frostimpact.rpgclasses.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.Frostimpact.rpgclasses.networking.ModMessages;
import net.Frostimpact.rpgclasses.networking.packet.PacketSyncClass;
import net.Frostimpact.rpgclasses.rpg.ModAttachments;
import net.Frostimpact.rpgclasses.rpg.PlayerRPGData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.List;

public class ClassCommand {

    private static final List<String> AVAILABLE_CLASSES = Arrays.asList(
            "BERSERKER", "BLADEDANCER",
            "BASTION", "JUGGERNAUT",
            "MARKSMAN", "MERCENARY",
            "MANAFORGE", "REFRACTION",
            "BATTLE_PRIEST", "ALCHEMIST"
    );

    private static final SuggestionProvider<CommandSourceStack> CLASS_SUGGESTIONS = (context, builder) ->
            SharedSuggestionProvider.suggest(AVAILABLE_CLASSES, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rpgclass")
                .then(Commands.literal("set")
                        .then(Commands.argument("class", StringArgumentType.word())
                                .suggests(CLASS_SUGGESTIONS)
                                .executes(ClassCommand::setClass)))
                .then(Commands.literal("list")
                        .executes(ClassCommand::listClasses))
                .then(Commands.literal("current")
                        .executes(ClassCommand::showCurrentClass))
        );
    }

    private static int setClass(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command!"));
            return 0;
        }

        String className = StringArgumentType.getString(context, "class").toUpperCase();

        if (!AVAILABLE_CLASSES.contains(className)) {
            context.getSource().sendFailure(Component.literal("§cInvalid class! Use /rpgclass list to see available classes."));
            return 0;
        }

        PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);
        rpg.setCurrentClass(className);

        // Reset state based on class
        resetPlayerForClass(player, rpg, className);

        // Sync to client
        ModMessages.sendToPlayer(new PacketSyncClass(className), player);

        context.getSource().sendSuccess(() ->
                Component.literal("§a✓ Class set to: §6" + className), true);

        return 1;
    }

    private static int listClasses(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() ->
                Component.literal("§6Available Classes:\n\n" +
                        "§c§lDAMAGE:\n" +
                        "§7- §cBERSERKER §7(Lifesteal berserker)\n" +
                        "§7- §bBLADEDANCER §7(Swift melee fighter)\n\n" +
                        "§b§lTANK:\n" +
                        "§7- §9BASTION §7(Traditional tank)\n" +
                        "§7- §cJUGGERNAUT §7(Stance-swapping warrior)\n\n" +
                        "§a§lRANGED:\n" +
                        "§7- §aMARKSMAN §7(Aerial archer)\n" +
                        "§7- §8MERCENARY §7(Stealth specialist)\n\n" +
                        "§5§lBURST/CONTROL:\n" +
                        "§7- §5MANAFORGE §7(Arcane caster)\n" +
                        "§7- §dREFRACTION §7(Mobile mage)\n\n" +
                        "§6§lSUPPORT:\n" +
                        "§7- §eBATTLE_PRIEST §7(Offensive healer)\n" +
                        "§7- §2ALCHEMIST §7(Potion specialist)"), false);
        return 1;
    }

    private static int showCurrentClass(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("Only players can use this command!"));
            return 0;
        }

        PlayerRPGData rpg = player.getData(ModAttachments.PLAYER_RPG);
        String currentClass = rpg.getCurrentClass();

        context.getSource().sendSuccess(() ->
                Component.literal("§7Your current class: §6" + currentClass), false);

        return 1;
    }

    private static void resetPlayerForClass(ServerPlayer player, PlayerRPGData rpg, String className) {
        // Clear all cooldowns
        rpg.clearAllCooldowns();

        // Reset mana
        rpg.setMana(rpg.getMaxMana());

        // Reset class-specific data
        switch (className) {
            case "BERSERKER":
            case "BLADEDANCER":
                rpg.resetTempo();
                rpg.setFinalWaltzActive(false);
                rpg.setParryActive(false);
                rpg.setBladeDanceActive(false);
                break;

            case "BASTION":
            case "JUGGERNAUT":
                rpg.setJuggernautShieldMode(true);
                rpg.setJuggernautCharge(0);
                rpg.stopChargeDecay();
                rpg.setFortifyActive(false);
                rpg.setLeapActive(false);
                rpg.setCrushActive(false);
                break;

            case "MANAFORGE":
            case "REFRACTION":
                rpg.setManaforgeArcana(0);
                rpg.setSurgeActive(false);
                rpg.setRiftActive(false);
                rpg.setCoalescenceActive(false);
                break;

            case "MARKSMAN":
            case "MERCENARY":
                rpg.setMarksmanSeekerCharges(0);
                rpg.setArrowRainActive(false);
                rpg.setCloakActive(false);
                rpg.setHiredGunActive(false);
                break;

            case "BATTLE_PRIEST":
            case "ALCHEMIST":
                // Support classes - reset when implemented
                break;
        }

        // Remove all potion effects
        player.removeAllEffects();
    }
}