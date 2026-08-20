package me.kteq.hiddenarmor.command;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.command.util.AbstractCommand;
import me.kteq.hiddenarmor.command.util.ArmorToggleExecutor;
import me.kteq.hiddenarmor.command.util.CommandStatus;
import me.kteq.hiddenarmor.handler.MessageHandler;
import me.kteq.hiddenarmor.util.ConfigHolder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ToggleArmorCommand extends AbstractCommand implements ConfigHolder {
    HiddenArmor plugin;

    private boolean defaultPermissionToggle;
    private boolean defaultPermissionToggleOther;

    public ToggleArmorCommand(HiddenArmor plugin, String command) {
        super(plugin, command);
        plugin.addConfigHolder(this);
        this.plugin = plugin;
    }

    @Override
    public CommandStatus execute(CommandSender sender, Command command, String[] arguments) {
        if (!hasSubPermission(sender, "toggle") && !defaultPermissionToggle) return CommandStatus.NO_PERMISSION;
        boolean canToggleOther = hasSubPermission(sender, "toggle.other") || defaultPermissionToggleOther;

        ArmorToggleExecutor.Result result = ArmorToggleExecutor.execute(plugin, sender, "toggle", arguments, canToggleOther);
        switch (result) {
            case NO_PERMISSION:
                return CommandStatus.NO_PERMISSION;
            case INVALID_USAGE:
            case MISSING_TARGET:
                sendUsage(sender);
                return CommandStatus.SUCCESS;
            default:
                return CommandStatus.SUCCESS;
        }
    }

    public void sendUsage(CommandSender sender) {
        MessageHandler messageHandler = plugin.getMessageHandler();
        Map<String, String> placeholderMap = new HashMap<>();
        if(sender instanceof Player) {
            String usage = "/togglearmor [%slot%]" + (hasSubPermission(sender, "toggle.other") || defaultPermissionToggleOther ? " [%player%]" : "");
            placeholderMap.put("usage", usage);
        } else {
            placeholderMap.put("usage", "/togglearmor [%slot%] <%player%>");
        }
        messageHandler.message(sender, "%correct-usage%", false, placeholderMap);
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.defaultPermissionToggle = config.getBoolean("default-permissions.toggle");
        this.defaultPermissionToggleOther = config.getBoolean("default-permissions.toggle-other");
    }
}
