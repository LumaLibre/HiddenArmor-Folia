package me.kteq.hiddenarmor.manager;

import me.kteq.hiddenarmor.HiddenArmor;
import me.kteq.hiddenarmor.handler.ArmorUpdateHandler;
import me.kteq.hiddenarmor.handler.MessageHandler;
import me.kteq.hiddenarmor.util.ArmorSlot;
import me.kteq.hiddenarmor.util.ConfigHolder;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class PlayerManager implements ConfigHolder {
    private static final String LEGACY_ENABLED_PLAYERS_PATH = "enabled-players";
    private static final String HIDDEN_SLOTS_PATH = "hidden-slots";

    private final HiddenArmor plugin;
    private final ArmorUpdateHandler armorUpdater;
    private final MessageHandler messageHandler;


    private File enabledPlayersFile = null;
    private FileConfiguration enabledPlayersConfig;

    private boolean invisibleAlwaysHideGear;

    private Map<UUID, Set<ArmorSlot>> hiddenSlotsMap = new HashMap<>();
    private final Set<UUID> forceHiddenPlayers = new HashSet<>();
    private final Set<UUID> forceShownPlayers = new HashSet<>();
    private final Set<Predicate<Player>> forceDisablePredicates = new HashSet<>();
    private final Set<Predicate<Player>> forceEnablePredicates = new HashSet<>();


    public PlayerManager(HiddenArmor plugin) {
        this.plugin = plugin;
        plugin.addConfigHolder(this);
        this.armorUpdater = plugin.getArmorUpdater();
        this.messageHandler = plugin.getMessageHandler();
        registerDefaultPredicates();
        loadEnabledPlayers();
    }

    public void togglePlayer(Player player, boolean inform) {
        if (isEnabled(player)) {
            disablePlayer(player, inform);
        } else {
            enablePlayer(player, inform);
        }
    }

    public void enablePlayer(Player player, boolean inform) {
        setHiddenSlots(player, ArmorSlot.all(), inform);
    }

    public void disablePlayer(Player player, boolean inform) {
        setHiddenSlots(player, ArmorSlot.none(), inform);
    }

    public void setHiddenSlots(Player player, Set<ArmorSlot> slots, boolean inform) {
        Set<ArmorSlot> newSlots = ArmorSlot.none();
        newSlots.addAll(slots);
        if (newSlots.equals(getHiddenSlots(player))) return;

        if (inform) {
            informArmorVisibility(player, !newSlots.isEmpty());
        }

        storeHiddenSlots(player, newSlots);
        armorUpdater.updatePlayer(player);
    }

    public void toggleSlot(Player player, ArmorSlot slot, boolean inform) {
        setSlotEnabled(player, slot, !isSlotEnabled(player, slot), inform);
    }

    public void hideSlot(Player player, ArmorSlot slot, boolean inform) {
        setSlotEnabled(player, slot, true, inform);
    }

    public void showSlot(Player player, ArmorSlot slot, boolean inform) {
        setSlotEnabled(player, slot, false, inform);
    }

    private void setSlotEnabled(Player player, ArmorSlot slot, boolean hidden, boolean inform) {
        if (isSlotEnabled(player, slot) == hidden) return;

        if (inform) {
            informSlotVisibility(player, slot, hidden);
        }

        Set<ArmorSlot> slots = getHiddenSlots(player);
        if (hidden) {
            slots.add(slot);
        } else {
            slots.remove(slot);
        }

        storeHiddenSlots(player, slots);
        armorUpdater.updatePlayer(player);
    }

    private void storeHiddenSlots(Player player, Set<ArmorSlot> slots) {
        if (slots.isEmpty()) {
            hiddenSlotsMap.remove(player.getUniqueId());
        } else {
            hiddenSlotsMap.put(player.getUniqueId(), slots);
        }
    }

    public boolean isEnabled(Player player) {
        return !getHiddenSlots(player).isEmpty();
    }

    public boolean isSlotEnabled(Player player, ArmorSlot slot) {
        return getHiddenSlots(player).contains(slot);
    }

    public Set<ArmorSlot> getHiddenSlots(Player player) {
        Set<ArmorSlot> slots = ArmorSlot.none();
        Set<ArmorSlot> playerSlots = hiddenSlotsMap.get(player.getUniqueId());
        if (playerSlots != null) slots.addAll(playerSlots);
        return slots;
    }

    public Set<ArmorSlot> getEffectiveHiddenSlots(Player player) {
        Set<ArmorSlot> hidden = getHiddenSlots(player);
        for (Predicate<Player> predicate : forceDisablePredicates) {
            if (predicate.test(player)) {
                hidden = ArmorSlot.none();
                break;
            }
        }
        for (Predicate<Player> predicate : forceEnablePredicates) {
            if (predicate.test(player)) {
                hidden = ArmorSlot.all();
                break;
            }
        }
        if (forceShownPlayers.contains(player.getUniqueId())) {
            hidden = ArmorSlot.none();
        }
        return hidden;
    }

    public boolean isArmorVisible(Player player) {
        return getEffectiveHiddenSlots(player).isEmpty();
    }

    public boolean isArmorVisible(Player player, ArmorSlot slot) {
        return !getEffectiveHiddenSlots(player).contains(slot);
    }

    private void informArmorVisibility(Player player, boolean hidden) {
        Map<String, String> placeholderMap = new HashMap<>();
        placeholderMap.put("visibility", hidden ? "%visibility-hidden%" : "%visibility-shown%");
        messageHandler.message(ChatMessageType.ACTION_BAR, player, "%armor-visibility%", false, placeholderMap);
    }

    private void informSlotVisibility(Player player, ArmorSlot slot, boolean hidden) {
        Map<String, String> placeholderMap = new HashMap<>();
        placeholderMap.put("slot", slot.getNamePlaceholder());
        placeholderMap.put("visibility", hidden ? "%visibility-hidden%" : "%visibility-shown%");
        messageHandler.message(ChatMessageType.ACTION_BAR, player, "%armor-visibility-slot%", false, placeholderMap);
    }

    private void registerDefaultPredicates() {
        forceDisablePredicates.add(player -> player.getGameMode().equals(GameMode.CREATIVE));
        forceDisablePredicates.add(player -> player.isInvisible() && !invisibleAlwaysHideGear);

        forceEnablePredicates.add(player -> player.isInvisible() && invisibleAlwaysHideGear);
        forceEnablePredicates.add(player -> forceHiddenPlayers.contains(player.getUniqueId()));
    }

    public void forceHidePlayer(Player player) {
        forceHiddenPlayers.add(player.getUniqueId());
        armorUpdater.updatePlayer(player);
    }

    public void clearForceHidePlayer(Player player) {
        forceHiddenPlayers.remove(player.getUniqueId());
        armorUpdater.updatePlayer(player);
    }

    public void forceShowPlayer(Player player) {
        forceShownPlayers.add(player.getUniqueId());
        armorUpdater.updatePlayer(player);
    }

    public void clearForceShowPlayer(Player player) {
        forceShownPlayers.remove(player.getUniqueId());
        armorUpdater.updatePlayer(player);
    }

    public boolean isForcedHidden(Player player) {
        return forceHiddenPlayers.contains(player.getUniqueId());
    }

    public boolean isForcedShown(Player player) {
        return forceShownPlayers.contains(player.getUniqueId());
    }

    public void clearForced(Player player) {
        forceHiddenPlayers.remove(player.getUniqueId());
        forceShownPlayers.remove(player.getUniqueId());
    }

    public void saveCurrentEnabledPlayers() {
        List<String> fullyEnabledUUIDs = hiddenSlotsMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() == ArmorSlot.values().length)
                .map(entry -> entry.getKey().toString())
                .collect(Collectors.toList());

        enabledPlayersConfig.set(LEGACY_ENABLED_PLAYERS_PATH, fullyEnabledUUIDs);
        enabledPlayersConfig.set(HIDDEN_SLOTS_PATH, null);
        for (Map.Entry<UUID, Set<ArmorSlot>> entry : hiddenSlotsMap.entrySet()) {
            List<String> slotNames = entry.getValue().stream().map(ArmorSlot::getName).collect(Collectors.toList());
            enabledPlayersConfig.set(HIDDEN_SLOTS_PATH + "." + entry.getKey(), slotNames);
        }

        try {
            enabledPlayersConfig.save(enabledPlayersFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not save enabled players to " + enabledPlayersFile, e);
        }
    }

    private void loadEnabledPlayers() {
        loadEnabledPlayersConfig();
        this.hiddenSlotsMap = new HashMap<>();

        for (String uuidString : enabledPlayersConfig.getStringList(LEGACY_ENABLED_PLAYERS_PATH)) {
            UUID uuid = parseUUID(uuidString);
            if (uuid != null) hiddenSlotsMap.put(uuid, ArmorSlot.all());
        }

        ConfigurationSection hiddenSlotsSection = enabledPlayersConfig.getConfigurationSection(HIDDEN_SLOTS_PATH);
        if (hiddenSlotsSection == null) return;
        for (String uuidString : hiddenSlotsSection.getKeys(false)) {
            UUID uuid = parseUUID(uuidString);
            if (uuid == null) continue;

            Set<ArmorSlot> slots = ArmorSlot.none();
            for (String slotName : hiddenSlotsSection.getStringList(uuidString)) {
                ArmorSlot slot = ArmorSlot.fromName(slotName);
                if (slot != null) slots.add(slot);
            }

            if (slots.isEmpty()) {
                hiddenSlotsMap.remove(uuid);
            } else {
                hiddenSlotsMap.put(uuid, slots);
            }
        }
    }

    private UUID parseUUID(String uuidString) {
        try {
            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().log(Level.WARNING, "Ignoring invalid UUID " + uuidString + " on " + enabledPlayersFile);
            return null;
        }
    }

    private void loadEnabledPlayersConfig() {
        enabledPlayersFile = new File(plugin.getDataFolder(), "enabled-players.yml");
        if (!enabledPlayersFile.exists()) {
            enabledPlayersFile.getParentFile().mkdirs();
            plugin.saveResource("enabled-players.yml", false);
        }

        enabledPlayersConfig = YamlConfiguration.loadConfiguration(enabledPlayersFile);
    }

    @Override
    public void loadConfig(FileConfiguration config) {
        this.invisibleAlwaysHideGear = config.getBoolean("invisibility-potion.always-hide-gear");
    }
}
