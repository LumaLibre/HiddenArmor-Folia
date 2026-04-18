package me.kteq.hiddenarmor;

import org.bukkit.entity.Player;

public final class HiddenArmorAPI {

    private HiddenArmorAPI() {}

    public static void forceHide(Player player) {
        HiddenArmor.getInstance().getPlayerManager().forceHidePlayer(player);
    }

    public static void clearForceHide(Player player) {
        HiddenArmor.getInstance().getPlayerManager().clearForceHidePlayer(player);
    }

    public static void forceShow(Player player) {
        HiddenArmor.getInstance().getPlayerManager().forceShowPlayer(player);
    }

    public static void clearForceShow(Player player) {
        HiddenArmor.getInstance().getPlayerManager().clearForceShowPlayer(player);
    }

    public static boolean isForcedHidden(Player player) {
        return HiddenArmor.getInstance().getPlayerManager().isForcedHidden(player);
    }

    public static boolean isForcedShown(Player player) {
        return HiddenArmor.getInstance().getPlayerManager().isForcedShown(player);
    }
}
