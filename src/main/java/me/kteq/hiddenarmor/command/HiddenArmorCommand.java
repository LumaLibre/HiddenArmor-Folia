package me.kteq.hiddenarmor.command;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.command.util.AbstractCommand;
import me.kteq.hiddenarmor.command.util.ArmorToggleExecutor;
import me.kteq.hiddenarmor.command.util.CommandStatus;
import me.kteq.hiddenarmor.handler.MessageHandler;
import me.kteq.hiddenarmor.util.ConfigHolder;
import me.kteq.hiddenarmor.util.PermissionUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Arrays;

public class HiddenArmorCommand extends AbstractCommand implements ConfigHolder {

    private boolean defaultPermissionToggle;
    private boolean defaultPermissionToggleOther;

    public HiddenArmorCommand(HiddenArmor plugin, String command) {
        super(plugin, command);
        plugin.addConfigHolder(this);
    }

    @Override
    public CommandStatus execute(CommandSender sender, Command command, String[] arguments) throws Exception {
        if((arguments.length < 1) || (arguments[0].equalsIgnoreCase("help"))) {
            help(sender);
            return CommandStatus.SUCCESS;
        }

        MessageHandler messageHandler = plugin.getMessageHandler();

        String subcommand = arguments[0].toLowerCase();

        switch (subcommand) {
            case "reload":
                if (!hasSubPermission(sender, "reload")) break;
                plugin.saveDefaultConfig();
                plugin.reloadConfig();
                messageHandler.reloadLocales();
                messageHandler.message(sender, "%reload-success%", true);
                return CommandStatus.SUCCESS;
            case "toggle":
            case "hide":
            case "show":
                return toggleArmor(sender, subcommand, Arrays.copyOfRange(arguments, 1, arguments.length));
        }

        return CommandStatus.INVALID_USAGE;
    }

    private CommandStatus toggleArmor(CommandSender sender, String action, String[] arguments) {
        if (!hasSubPermission(sender, "toggle") && !defaultPermissionToggle) return CommandStatus.NO_PERMISSION;
        boolean canToggleOther = hasSubPermission(sender, "toggle.other") || defaultPermissionToggleOther;

        ArmorToggleExecutor.Result result = ArmorToggleExecutor.execute(plugin, sender, action, arguments, canToggleOther);
        switch (result) {
            case NO_PERMISSION:
                return CommandStatus.NO_PERMISSION;
            case INVALID_USAGE:
                return CommandStatus.INVALID_USAGE;
            default:
                return CommandStatus.SUCCESS;
        }
    }

    private void help(CommandSender sender){
        MessageHandler messageHandler = plugin.getMessageHandler();
        messageHandler.message(sender,"&6----------[ &fHiddenArmor &6]-----------------");

        // hiddenarmor <toggle/hide/show>
        if(PermissionUtil.canUse(sender ,"hiddenarmor.toggle") || defaultPermissionToggle) {
            messageHandler.message(sender, "&e/hiddenarmor <toggle/hide/show> &6- %help-togglearmor%");

            // hiddenarmor <toggle/hide/show> <slot>
            messageHandler.message(sender, "&e/hiddenarmor <toggle/hide/show> [%slot%] &6- %help-togglearmor-slot%");
        }

        // hiddenarmor <toggle/hide/show> <slot> <player>
        if(PermissionUtil.canUse(sender ,"hiddenarmor.toggle.other") || (defaultPermissionToggle && defaultPermissionToggleOther))
            messageHandler.message(sender, "&e/hiddenarmor <toggle/hide/show> [%slot%] [%player%] &6- %help-togglearmor-other%");

        // hiddenarmor reload
        if(PermissionUtil.canUse(sender, "hiddenarmor.reload"))
            messageHandler.message(sender, "&e/hiddenarmor reload &6- %help-reload%");

        // help
        messageHandler.message(sender, "&e/hiddenarmor help &6- %help-help%");

        messageHandler.message(sender,"&6----------------------------------------");
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.defaultPermissionToggle = config.getBoolean("default-permissions.toggle");
        this.defaultPermissionToggleOther = config.getBoolean("default-permissions.toggle-other");
    }
}
