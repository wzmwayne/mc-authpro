package com.example.mcauthpro.auth;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        return new PlayerData(username, uuid, passwordHash, salt, ipHistory, registeredAt);
    }

    public void savePlayer(PlayerData playerData) {
        File playerFile = getPlayerFile(playerData.getUuid());
        YamlConfiguration config = new YamlConfiguration();
        config.set("username", playerData.getUsername());
        config.set("password-hash", playerData.getPasswordHash());
        config.set("salt", playerData.getSalt());
        config.set("ip-history", playerData.getIpHistory());
        config.set("registered-at", playerData.getRegisteredAt());
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

    public List<String> getAllRegisteredUUIDs() {
        List<String> uuids = new ArrayList<>();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                uuids.add(name.substring(0, name.length() - 4));
            }
        }
        return uuids;
    }
}