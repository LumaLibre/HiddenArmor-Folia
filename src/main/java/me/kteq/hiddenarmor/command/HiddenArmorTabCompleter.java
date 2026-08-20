package me.kteq.hiddenarmor.command;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.util.ArmorSlot;
import me.kteq.hiddenarmor.util.ConfigHolder;
import me.kteq.hiddenarmor.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class HiddenArmorTabCompleter implements TabCompleter, ConfigHolder {
    private static final List<String> TOGGLE_ACTIONS = Arrays.asList("toggle", "hide", "show");

    private boolean defaultPermissionToggle;
    private boolean defaultPermissionToggleOther;

    public HiddenArmorTabCompleter(HiddenArmor plugin) {
        plugin.addConfigHolder(this);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 0) return null;
        boolean canToggle = PermissionUtil.canUse(sender, "hiddenarmor.toggle") || defaultPermissionToggle;

        List<String> options;
        switch (command.getName().toLowerCase()) {
            case "togglearmor":
                // /togglearmor [slot] [player]
                if (!canToggle) return null;
                if (args.length == 1) options = slotOrPlayerOptions(sender);
                else if (args.length == 2 && isSlot(args[0])) options = playerOptions(sender);
                else return null;
                break;
            case "hiddenarmor":
                if (args.length == 1) {
                    options = new ArrayList<>();
                    if (canToggle) options.addAll(TOGGLE_ACTIONS);
                    if (PermissionUtil.canUse(sender, "hiddenarmor.reload")) options.add("reload");
                    options.add("help");
                }
                // /hiddenarmor <toggle/hide/show> [slot] [player]
                else if (!canToggle || !TOGGLE_ACTIONS.contains(args[0].toLowerCase())) return null;
                else if (args.length == 2) options = slotOrPlayerOptions(sender);
                else if (args.length == 3 && isSlot(args[1])) options = playerOptions(sender);
                else return null;
                break;
            default:
                return null;
        }

        return filter(options, args[args.length - 1]);
    }

    private boolean isSlot(String argument) {
        return ArmorSlot.fromName(argument) != null || argument.equalsIgnoreCase(ArmorSlot.ALL_KEYWORD);
    }

    private List<String> slotOrPlayerOptions(CommandSender sender) {
        List<String> options = new ArrayList<>();
        for (ArmorSlot slot : ArmorSlot.values()) {
            options.add(slot.getName());
        }
        options.add(ArmorSlot.ALL_KEYWORD);
        options.addAll(playerOptions(sender));
        return options;
    }

    private List<String> playerOptions(CommandSender sender) {
        List<String> options = new ArrayList<>();
        if (!PermissionUtil.canUse(sender, "hiddenarmor.toggle.other") && !defaultPermissionToggleOther) return options;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (sender instanceof Player && !((Player) sender).canSee(player)) continue;
            options.add(player.getName());
        }
        return options;
    }

    private List<String> filter(List<String> options, String argument) {
        String prefix = argument.toLowerCase(Locale.ROOT);
        List<String> filtered = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) filtered.add(option);
        }
        return filtered;
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.defaultPermissionToggle = config.getBoolean("default-permissions.toggle");
        this.defaultPermissionToggleOther = config.getBoolean("default-permissions.toggle-other");
    }
}
