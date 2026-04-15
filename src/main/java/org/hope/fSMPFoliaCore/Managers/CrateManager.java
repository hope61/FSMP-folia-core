package org.hope.fSMPFoliaCore.Managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.hope.fSMPFoliaCore.FSMPFoliaCore;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CrateManager {
    public static final String CRATE_KEY = "fsmp_crate";

    private final FSMPFoliaCore plugin;
    private final NamespacedKey crateKey;
    private File file;
    private FileConfiguration data;

    // Last daily claim timestamp per player
    private final Map<UUID, Long> dailyClaim = new ConcurrentHashMap<>();
    // Last hourly crate award timestamp per player
    private final Map<UUID, Long> hourlyClaim = new ConcurrentHashMap<>();

    public CrateManager(FSMPFoliaCore plugin) {
        this.plugin = plugin;
        this.crateKey = new NamespacedKey(plugin, CRATE_KEY);
        load();
    }

    public NamespacedKey getCrateKey() { return crateKey; }

    private void load() {
        file = new File(plugin.getDataFolder(), "crate_data.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) {
                plugin.getLogger().severe("Failed to create crate_data.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        for (String key : data.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                dailyClaim.put(uuid, data.getLong(key + ".daily", 0L));
                hourlyClaim.put(uuid, data.getLong(key + ".hourly", 0L));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void saveAsync() {
        Map<UUID, Long> dSnap = Map.copyOf(dailyClaim);
        Map<UUID, Long> hSnap = Map.copyOf(hourlyClaim);
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> {
            YamlConfiguration yml = new YamlConfiguration();
            for (UUID uuid : dSnap.keySet()) {
                String k = uuid.toString();
                yml.set(k + ".daily", dSnap.getOrDefault(uuid, 0L));
                yml.set(k + ".hourly", hSnap.getOrDefault(uuid, 0L));
            }
            try { yml.save(file); } catch (IOException e) {
                plugin.getLogger().severe("Failed to save crate_data.yml: " + e.getMessage());
            }
        });
    }

    /** Create a physical crate item. */
    public ItemStack createCrateItem(int amount) {
        ItemStack item = new ItemStack(Material.CHEST, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("✦ FSMP Кутия ✦")
                .color(NamedTextColor.AQUA)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.empty(),
                Component.text("Десен клик за да отвориш!")
                        .color(NamedTextColor.DARK_AQUA)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(crateKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    /** Check if the given item is an FSMP crate. */
    public boolean isCrateItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(crateKey, PersistentDataType.BYTE);
    }

    /** Give physical crate items to a player (drops at feet if inventory full). */
    public void addCrates(Player player, int amount) {
        ItemStack crate = createCrateItem(amount);
        var leftover = player.getInventory().addItem(crate);
        if (!leftover.isEmpty()) {
            player.getWorld().dropItemNaturally(player.getLocation(), leftover.values().iterator().next());
        }
    }

    /** Returns true if the player can claim their daily login crate. */
    public boolean canClaimDaily(UUID uuid) {
        long last = dailyClaim.getOrDefault(uuid, 0L);
        return (System.currentTimeMillis() - last) >= 20 * 3600_000L;
    }

    public void recordDailyClaim(UUID uuid) {
        dailyClaim.put(uuid, System.currentTimeMillis());
        saveAsync();
    }

    public boolean canClaimHourly(UUID uuid) {
        long last = hourlyClaim.getOrDefault(uuid, 0L);
        return (System.currentTimeMillis() - last) >= 3600_000L;
    }

    public void recordHourlyClaim(UUID uuid) {
        hourlyClaim.put(uuid, System.currentTimeMillis());
        saveAsync();
    }
}
