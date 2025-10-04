package io.github.mikeandjoski.hardcore;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HomeManager {

    private final Hardcore plugin;
    private final Map<Integer, Home> homes = new HashMap<>();
    private int unlockedSlots;

    public HomeManager(Hardcore plugin) {
        this.plugin = plugin;
    }

    public void loadHomes(Player player) {
        File playerFile = new File(plugin.getDataFolder(), player.getUniqueId().toString() + ".yml");
        if (!playerFile.exists()) {
            this.unlockedSlots = 1; // Default to 1 free slot
            this.homes.clear();
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        this.unlockedSlots = config.getInt("unlocked-slots", 1);
        this.homes.clear();

        if (config.isConfigurationSection("homes")) {
            for (String slotStr : config.getConfigurationSection("homes").getKeys(false)) {
                try {
                    int slot = Integer.parseInt(slotStr);
                    String name = config.getString("homes." + slot + ".name");
                    Location location = config.getLocation("homes." + slot + ".location");
                    if (name != null && location != null) {
                        homes.put(slot, new Home(name, location));
                    }
                } catch (NumberFormatException e) {
                    // Ignore if the key is not an integer
                }
            }
        }
    }

    public void saveHomes(Player player) {
        File playerFile = new File(plugin.getDataFolder(), player.getUniqueId().toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);

        config.set("unlocked-slots", this.unlockedSlots);
        config.set("homes", null); // Clear old homes before saving new ones
        for (Map.Entry<Integer, Home> entry : homes.entrySet()) {
            int slot = entry.getKey();
            Home home = entry.getValue();
            config.set("homes." + slot + ".name", home.getName());
            config.set("homes." + slot + ".location", home.getLocation());
        }

        try {
            config.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Home> getHomes() {
        return homes;
    }

    public int getUnlockedSlots() {
        return unlockedSlots;
    }

    public void setHome(int slot, String name, Location location) {
        if (slot < unlockedSlots) {
            homes.put(slot, new Home(name, location));
        }
    }

    public Home getHome(int slot) {
        return homes.get(slot);
    }

    public void deleteHome(int slot) {
        homes.remove(slot);
    }

    public boolean unlockSlot() {
        if (this.unlockedSlots < 5) {
            this.unlockedSlots++;
            return true;
        }
        return false;
    }
}