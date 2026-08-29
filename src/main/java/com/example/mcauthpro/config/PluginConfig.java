package com.example.mcauthpro.config;

import com.example.mcauthpro.McAuthPro;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class PluginConfig {
    private final YamlConfiguration config;

    public PluginConfig(McAuthPro plugin) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        plugin.getDataFolder().mkdirs();

        YamlConfiguration defaultConfig = loadDefaultConfig(plugin);

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);

        boolean patched = patchMissingKeys(defaultConfig, this.config);

        if (patched) {
            try {
                this.config.save(configFile);
                plugin.getLogger().info("已自动补全 config.yml 缺失的配置项。");
            } catch (Exception e) {
                plugin.getLogger().warning("保存 config.yml 失败: " + e.getMessage());
            }
        }
    }

    private YamlConfiguration loadDefaultConfig(McAuthPro plugin) {
        InputStream is = plugin.getResource("config.yml");
        if (is == null) {
            return new YamlConfiguration();
        }
        InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
        return YamlConfiguration.loadConfiguration(reader);
    }

    private boolean patchMissingKeys(YamlConfiguration defaults, YamlConfiguration target) {
        boolean patched = false;
        Set<String> keys = defaults.getKeys(true);
        for (String key : keys) {
            if (defaults.isConfigurationSection(key)) {
                continue;
            }
            if (!target.contains(key)) {
                target.set(key, defaults.get(key));
                patched = true;
            }
        }
        return patched;
    }

    // ==================== verification ====================
    public String getStaticSiteBaseUrl() {
        return config.getString("verification.static-site-base-url", "https://auth.example.com");
    }

    public String getSiteverifyUrl() {
        return config.getString("verification.siteverify-url", "https://challenges.cloudflare.com/turnstile/v0/siteverify");
    }

    public String getSecretKey() {
        return config.getString("verification.secret-key", "");
    }

    public String getAction() {
        return config.getString("verification.action", "mc-login");
    }

    public int getTokenTtlSeconds() {
        return config.getInt("verification.token-ttl-seconds", 300);
    }

    public int getTimeoutSeconds() {
        return config.getInt("verification.timeout-seconds", 300);
    }

    public String getExpectedHostname() {
        return config.getString("verification.expected-hostname", "");
    }

    public int getHttpTimeoutSeconds() {
        return config.getInt("verification.http-timeout-seconds", 10);
    }

    public int getVerificationCountdown() {
        return config.getInt("verification.countdown-seconds", 30);
    }

    // ==================== session ====================
    public int getSessionTimeoutSeconds() {
        return config.getInt("session.timeout-seconds", 180);
    }

    public int getRememberIpMinutes() {
        return config.getInt("session.remember-ip-minutes", 30);
    }

    // ==================== real-ip ====================
    public boolean isTrustProxy() {
        return config.getBoolean("real-ip.trust-proxy", true);
    }

    public String getCfHeader() {
        return config.getString("real-ip.cf-header", "CF-Connecting-IP");
    }

    public String getForwardedHeader() {
        return config.getString("real-ip.forwarded-header", "X-Forwarded-For");
    }

    public boolean isBungeeGuard() {
        return config.getBoolean("real-ip.bungee-guard", true);
    }

    public int getMaxIpsToRecord() {
        return config.getInt("real-ip.max-ips-to-record", 5);
    }

    // ==================== security ====================
    public int getMaxLoginAttempts() {
        return config.getInt("security.max-login-attempts", 5);
    }

    public int getLockoutSeconds() {
        return config.getInt("security.lockout-seconds", 60);
    }

    public boolean isAllowMultipleLoginsPerIp() {
        return config.getBoolean("security.allow-multiple-logins-per-ip", true);
    }

    // ==================== anti-dos ====================
    public int getMaxConcurrentUnauth() {
        return config.getInt("anti-dos.max-concurrent-unauth", 5);
    }

    public int getVerificationTimeout() {
        return config.getInt("anti-dos.verification-timeout", 300);
    }

    // ==================== login-world ====================
    public boolean isLoginWorldEnabled() {
        return config.getBoolean("login-world.enabled", true);
    }

    public String getLoginWorldName() {
        return config.getString("login-world.name", "login");
    }

    public int getLoginWorldPlatformY() {
        return config.getInt("login-world.platform-y", 100);
    }

    public int getLoginWorldPlatformSize() {
        return config.getInt("login-world.platform-size", 2);
    }

    public int getLoginWorldWallHeight() {
        return config.getInt("login-world.wall-height", 1);
    }

    public boolean isForceSpectator() {
        return config.getBoolean("login-world.force-spectator", true);
    }
}