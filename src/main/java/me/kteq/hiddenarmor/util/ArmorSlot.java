package me.kteq.hiddenarmor.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public enum ArmorSlot {
    HELMET(5, "head", "hat"),
    CHESTPLATE(6, "chest", "body"),
    LEGGINGS(7, "legs", "pants"),
    BOOTS(8, "feet", "shoes");

    public static final String ALL_KEYWORD = "all";

    private final int rawSlot;
    private final List<String> aliases;

    ArmorSlot(int rawSlot, String... aliases) {
        this.rawSlot = rawSlot;
        this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
    }

    public int getRawSlot() {
        return rawSlot;
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public List<String> getAliases() {
        return aliases;
    }

    public String getLocaleKey() {
        return "slot-" + getName();
    }

    public String getNamePlaceholder() {
        return "%" + getLocaleKey() + "%";
    }

    public ItemStack getItem(PlayerInventory inventory) {
        switch (this) {
            case HELMET: return inventory.getHelmet();
            case CHESTPLATE: return inventory.getChestplate();
            case LEGGINGS: return inventory.getLeggings();
            case BOOTS: return inventory.getBoots();
        }
        return null;
    }

    public static ArmorSlot fromRawSlot(int rawSlot) {
        for (ArmorSlot slot : values()) {
            if (slot.getRawSlot() == rawSlot) return slot;
        }
        return null;
    }

    public static ArmorSlot fromName(String name) {
        if (name == null) return null;
        for (ArmorSlot slot : values()) {
            if (slot.getName().equalsIgnoreCase(name)) return slot;
            for (String alias : slot.getAliases()) {
                if (alias.equalsIgnoreCase(name)) return slot;
            }
        }
        return null;
    }

    public static Set<ArmorSlot> all() {
        return EnumSet.allOf(ArmorSlot.class);
    }

    public static Set<ArmorSlot> none() {
        return EnumSet.noneOf(ArmorSlot.class);
    }
}
