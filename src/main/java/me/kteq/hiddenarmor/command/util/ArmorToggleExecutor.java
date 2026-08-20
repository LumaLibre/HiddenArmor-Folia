package me.kteq.hiddenarmor.command.util;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.handler.MessageHandler;
import me.kteq.hiddenarmor.manager.PlayerManager;
import me.kteq.hiddenarmor.util.ArmorSlot;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class ArmorToggleExecutor {

    public enum Result {
        SUCCESS,
        INVALID_USAGE,
        NO_PERMISSION,
        MISSING_TARGET
    }

    private ArmorToggleExecutor() {}

    public static Result execute(HiddenArmor plugin, CommandSender sender, String action, String[] arguments, boolean canToggleOther) {
        MessageHandler messageHandler = plugin.getMessageHandler();
        PlayerManager playerManager = plugin.getPlayerManager();

        int index = 0;
        ArmorSlot slot = null;
        if (arguments.length > index) {
            slot = ArmorSlot.fromName(arguments[index]);
            if (slot != null || arguments[index].equalsIgnoreCase(ArmorSlot.ALL_KEYWORD)) index++;
        }
        String playerName = arguments.length > index ? arguments[index++] : null;
        if (arguments.length > index) return Result.INVALID_USAGE;

        Player player;
        if (playerName != null) {
            if (!canToggleOther) return Result.NO_PERMISSION;

            player = Bukkit.getPlayer(playerName);
            if (player == null) {
                messageHandler.message(sender, "%player-not-found%");
                return Result.SUCCESS;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            messageHandler.message(sender, "%console-togglearmor-warning%");
            return Result.MISSING_TARGET;
        }

        if (!applyAction(playerManager, player, slot, action)) return Result.INVALID_USAGE;

        if (!player.equals(sender)) {
            informSender(messageHandler, playerManager, sender, player, slot);
        }
        return Result.SUCCESS;
    }

    private static boolean applyAction(PlayerManager playerManager, Player player, ArmorSlot slot, String action) {
        switch (action.toLowerCase()) {
            case "toggle":
                if (slot == null) playerManager.togglePlayer(player, true);
                else playerManager.toggleSlot(player, slot, true);
                return true;
            case "hide":
                if (slot == null) playerManager.enablePlayer(player, true);
                else playerManager.hideSlot(player, slot, true);
                return true;
            case "show":
                if (slot == null) playerManager.disablePlayer(player, true);
                else playerManager.showSlot(player, slot, true);
                return true;
        }
        return false;
    }

    private static void informSender(MessageHandler messageHandler, PlayerManager playerManager, CommandSender sender, Player player, ArmorSlot slot) {
        boolean hidden = slot == null ? playerManager.isEnabled(player) : playerManager.isSlotEnabled(player, slot);

        Map<String, String> placeholderMap = new HashMap<>();
        placeholderMap.put("player", player.getName());
        placeholderMap.put("visibility", hidden ? "%visibility-hidden%" : "%visibility-shown%");

        if (slot == null) {
            messageHandler.message(sender, "%armor-visibility-other%", false, placeholderMap);
        } else {
            placeholderMap.put("slot", slot.getNamePlaceholder());
            messageHandler.message(sender, "%armor-visibility-slot-other%", false, placeholderMap);
        }
    }
}
