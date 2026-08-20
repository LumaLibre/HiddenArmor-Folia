package me.kteq.hiddenarmor;

import me.kteq.hiddenarmor.util.ArmorSlot;
import org.bukkit.entity.Player;

import java.util.Set;

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

    public static void hideSlot(Player player, ArmorSlot slot) {
        HiddenArmor.getInstance().getPlayerManager().hideSlot(player, slot, false);
    }

    public static void showSlot(Player player, ArmorSlot slot) {
        HiddenArmor.getInstance().getPlayerManager().showSlot(player, slot, false);
    }

    public static void setHiddenSlots(Player player, Set<ArmorSlot> slots) {
        HiddenArmor.getInstance().getPlayerManager().setHiddenSlots(player, slots, false);
    }

    public static boolean isSlotHidden(Player player, ArmorSlot slot) {
        return HiddenArmor.getInstance().getPlayerManager().isSlotEnabled(player, slot);
    }

    public static Set<ArmorSlot> getHiddenSlots(Player player) {
        return HiddenArmor.getInstance().getPlayerManager().getHiddenSlots(player);
    }

    public static Set<ArmorSlot> getEffectiveHiddenSlots(Player player) {
        return HiddenArmor.getInstance().getPlayerManager().getEffectiveHiddenSlots(player);
    }
}
