package com.example.mcauthpro.config;

import com.example.mcauthpro.McAuthPro;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Messages {
    private final YamlConfiguration config;

    public Messages(McAuthPro plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        plugin.getDataFolder().mkdirs();

        YamlConfiguration defaultConfig = loadDefault(plugin);

        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        boolean patched = patchMissing(defaultConfig, this.config);
        if (patched) {
            try {
                this.config.save(file);
                plugin.getLogger().info("已自动补全 messages.yml 缺失的消息项。");
            } catch (Exception e) {
                plugin.getLogger().warning("保存 messages.yml 失败: " + e.getMessage());
            }
        }
    }

    private YamlConfiguration loadDefault(McAuthPro plugin) {
        InputStream is = plugin.getResource("messages.yml");
        if (is == null) return new YamlConfiguration();
        return YamlConfiguration.loadConfiguration(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    private boolean patchMissing(YamlConfiguration defaults, YamlConfiguration target) {
        boolean patched = false;
        for (String key : defaults.getKeys(true)) {
            if (defaults.isConfigurationSection(key)) continue;
            if (!target.contains(key)) {
                target.set(key, defaults.get(key));
                patched = true;
            }
        }
        return patched;
    }

    public String get(String key) {
        String raw = config.getString(key, "");
        return colorize(raw);
    }

    public String get(String key, String... replacements) {
        String raw = config.getString(key, "");
        for (int i = 0; i < replacements.length - 1; i += 2) {
            raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return colorize(raw);
    }

    public String[] getLines(String key) {
        String raw = config.getString(key, "");
        for (int i = 0; i < getConfig().getKeys(true).size(); i++) {
            // placeholder
        }
        String colored = colorize(raw);
        return colored.split("\n");
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public YamlConfiguration getConfig() {
        return config;
    }
}