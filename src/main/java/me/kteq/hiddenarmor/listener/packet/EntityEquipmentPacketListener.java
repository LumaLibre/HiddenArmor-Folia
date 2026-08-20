package me.kteq.hiddenarmor.listener.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.util.ArmorSlot;
import me.kteq.hiddenarmor.util.ConfigHolder;
import me.kteq.hiddenarmor.util.protocol.ProtocolUtil;
import me.kteq.hiddenarmor.util.protocol.PacketFields;
import me.kteq.hiddenarmor.util.protocol.PacketIndexMapper;
import me.kteq.hiddenarmor.manager.PlayerManager;
import me.kteq.hiddenarmor.util.ItemUtil;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class EntityEquipmentPacketListener extends PacketAdapter implements ConfigHolder {
    private final PlayerManager hiddenArmorManager;
    private final ProtocolManager protocolManager;

    private boolean ignoreLeatherArmor;
    private boolean ignoreTurtleHelmet;
    private boolean ignoreElytra;
    private List<String> ignoreWorlds;

    private final int ENTITY_ID_INDEX;
    private final int SLOT_ITEM_PAIR_LIST_INDEX;

    public EntityEquipmentPacketListener(HiddenArmor plugin, PacketIndexMapper indexMapper) {
        super(plugin, PacketType.Play.Server.ENTITY_EQUIPMENT);
        plugin.addConfigHolder(this);

        this.hiddenArmorManager = plugin.getPlayerManager();
        this.protocolManager = plugin.getProtocolManager();

        this.ENTITY_ID_INDEX = indexMapper.get(PacketFields.ENTITY_EQUIPMENT_$ENTITY_ID);
        this.SLOT_ITEM_PAIR_LIST_INDEX = indexMapper.get(PacketFields.ENTITY_EQUIPMENT_$SLOT_ITEM_PAIR_LIST);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();

        Player packetPlayer = getPlayerByEntityId(packet.getIntegers().read(ENTITY_ID_INDEX));
        if (packetPlayer == null) return;

        if(hiddenArmorManager.isArmorVisible(packetPlayer)) return;

        List<Pair<EnumWrappers.ItemSlot, ItemStack>> pairList = packet.getSlotStackPairLists().read(SLOT_ITEM_PAIR_LIST_INDEX);

        for (Pair<EnumWrappers.ItemSlot, ItemStack> pair : pairList) {
            ArmorSlot armorSlot = ProtocolUtil.getArmorSlot(pair.getFirst());
            if (armorSlot == null) continue;
            if (hiddenArmorManager.isArmorVisible(packetPlayer, armorSlot)) continue;

            ItemStack item = pair.getSecond();
            if (item.getType().equals(Material.ELYTRA)
                    && ((packetPlayer.isGliding() || ignoreElytra)
                    && !packetPlayer.isInvisible()))
            {
                pair.setSecond(new ItemStack(Material.ELYTRA));
            }
            else if (!shouldIgnore(item, packetPlayer.getWorld()))
                pair.setSecond(new ItemStack(Material.AIR));
        }
        packet.getSlotStackPairLists().write(SLOT_ITEM_PAIR_LIST_INDEX, pairList);
    }

    private boolean shouldIgnore(ItemStack itemStack, World world) {
        Material material = itemStack.getType();

        return (ignoreWorlds.contains(world.getName()) ||
                ignoreLeatherArmor && material.toString().startsWith("LEATHER")) ||
                (ignoreTurtleHelmet && material.equals(Material.TURTLE_HELMET)) ||
                (!ItemUtil.isArmor(itemStack) && !itemStack.getType().equals(Material.ELYTRA)) ||
                (ignoreElytra && itemStack.getType().equals(Material.ELYTRA));
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.ignoreLeatherArmor = config.getBoolean("ignore.leather-armor");
        this.ignoreTurtleHelmet = config.getBoolean("ignore.turtle-helmet");
        this.ignoreElytra = config.getBoolean("ignore.elytra");
        this.ignoreWorlds = config.getStringList("ignore.worlds");
    }

    private Player getPlayerByEntityId(int entityId) {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.getEntityId() == entityId) {
                return p;
            }
        }
        return null;
    }
}
