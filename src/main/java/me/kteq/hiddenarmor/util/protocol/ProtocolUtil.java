package me.kteq.hiddenarmor.util.protocol;

import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.util.ArmorSlot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ProtocolUtil {

    public static void broadcastPlayerPacket(ProtocolManager manager, PacketContainer packet, Player player) {
        World world = player.getWorld();
        Location loc = player.getLocation();
        int viewRadius = Bukkit.getViewDistance() * 16;
        for(Player p : Bukkit.getOnlinePlayers()){
            p.getScheduler().run(HiddenArmor.getInstance(), task -> {
                if(!(p.getWorld().equals(world) && p.getLocation().distance(loc) < viewRadius && !p.equals(player))) {
                    return;
                }
                manager.sendServerPacket(p, packet);
            }, null);
        }
    }

    public static boolean isArmorSlot(Pair<EnumWrappers.ItemSlot, ItemStack> pair) {
        return getArmorSlot(pair.getFirst()) != null;
    }

    public static ArmorSlot getArmorSlot(EnumWrappers.ItemSlot itemSlot) {
        switch (itemSlot) {
            case HEAD: return ArmorSlot.HELMET;
            case CHEST: return ArmorSlot.CHESTPLATE;
            case LEGS: return ArmorSlot.LEGGINGS;
            case FEET: return ArmorSlot.BOOTS;
            default: return null;
        }
    }

    public static EnumWrappers.ItemSlot getItemSlot(ArmorSlot armorSlot) {
        switch (armorSlot) {
            case HELMET: return EnumWrappers.ItemSlot.HEAD;
            case CHESTPLATE: return EnumWrappers.ItemSlot.CHEST;
            case LEGGINGS: return EnumWrappers.ItemSlot.LEGS;
            case BOOTS: return EnumWrappers.ItemSlot.FEET;
        }
        return null;
    }

    public static ItemStack getArmor(ArmorSlot armorSlot, PlayerInventory inv) {
        if (armorSlot != null) {
            ItemStack item = armorSlot.getItem(inv);
            if (item != null) return item.clone();
        }
        return new ItemStack(Material.AIR);
    }
}
