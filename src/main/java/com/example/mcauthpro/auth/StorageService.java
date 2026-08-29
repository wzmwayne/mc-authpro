package com.example.mcauthpro.auth;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class StorageService {
    private final com.example.mcauthpro.McAuthPro plugin;
    private final File dataFolder;

    public StorageService(com.example.mcauthpro.McAuthPro plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "players");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public PlayerData loadPlayer(String uuid) {
        File playerFile = getPlayerFile(uuid);
        if (!playerFile.exists()) {
            return null;
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        String username = config.getString("username", "");
        String passwordHash = config.getString("password-hash", "");
        String salt = config.getString("salt", "");
        List<String> ipHistory = config.getStringList("ip-history");
        long registeredAt = config.getLong("registered-at", 0);
        String lastWorld = config.getString("last-location.world", "world");
        double lastX = config.getDouble("last-location.x", 0);
        double lastY = config.getDouble("last-location.y", 100);
        double lastZ = config.getDouble("last-location.z", 0);
        float lastYaw = (float) config.getDouble("last-location.yaw", 0);
        float lastPitch = (float) config.getDouble("last-location.pitch", 0);
        return new PlayerData(username, uuid, passwordHash, salt, ipHistory, registeredAt,
                             lastWorld, lastX, lastY, lastZ, lastYaw, lastPitch);
    }

    public void savePlayer(PlayerData playerData) {
        File playerFile = getPlayerFile(playerData.getUuid());
        YamlConfiguration config = new YamlConfiguration();
        config.set("username", playerData.getUsername());
        config.set("password-hash", playerData.getPasswordHash());
        config.set("salt", playerData.getSalt());
        config.set("ip-history", playerData.getIpHistory());
        config.set("registered-at", playerData.getRegisteredAt());
        config.set("last-location.world", playerData.getLastWorld());
        config.set("last-location.x", playerData.getLastX());
        config.set("last-location.y", playerData.getLastY());
        config.set("last-location.z", playerData.getLastZ());
        config.set("last-location.yaw", playerData.getLastYaw());
        config.set("last-location.pitch", playerData.getLastPitch());
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().severe("无法保存玩家数据: " + e.getMessage());
        }
    }

    public boolean isRegistered(String uuid) {
        return getPlayerFile(uuid).exists();
    }

    private File getPlayerFile(String uuid) {
        return new File(dataFolder, uuid + ".yml");
    }
}