package io.github.mikeandjoski.hardcore;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final JavaPlugin plugin;
    private final Map<UUID, PlayerStats> playerStatsMap = new HashMap<>();
    private File statsFile;
    private FileConfiguration statsConfig;

    public StatsManager(JavaPlugin plugin) {
        this.plugin = plugin;
        setupStatsFile();
    }

    private void setupStatsFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        statsFile = new File(plugin.getDataFolder(), "player_stats.yml");
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create player_stats.yml!" + e.getMessage());
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }

    private void saveStatsFile() {
        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player_stats.yml!" + e.getMessage());
        }
    }

    public void addPlayer(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (statsConfig.contains(playerUUID.toString())) {
            // 기존 스탯 불러오기
            PlayerStats loadedStats = new PlayerStats(statsConfig.getConfigurationSection(playerUUID.toString()));
            playerStatsMap.put(playerUUID, loadedStats);
            // 불러온 스탯으로 현재 지구력 초기화 (최대치로)
            loadedStats.setCurrentStamina(loadedStats.getMaxStamina());
        } else {
            // 새 플레이어 스탯 생성
            playerStatsMap.put(playerUUID, new PlayerStats());
        }
    }

    public void removePlayer(Player player) {
        savePlayerStats(player); // 플레이어 나갈 때 스탯 저장
        playerStatsMap.remove(player.getUniqueId());
    }

    public PlayerStats getPlayerStats(Player player) {
        return playerStatsMap.get(player.getUniqueId());
    }

    public void savePlayerStats(Player player) {
        PlayerStats stats = playerStatsMap.get(player.getUniqueId());
        if (stats != null) {
            stats.save(statsConfig.createSection(player.getUniqueId().toString()));
            saveStatsFile();
        }
    }

    public void saveAllStats() {
        for (UUID uuid : playerStatsMap.keySet()) {
            PlayerStats stats = playerStatsMap.get(uuid);
            stats.save(statsConfig.createSection(uuid.toString()));
        }
        saveStatsFile();
    }
}
