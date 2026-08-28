package com.example.mcauthpro.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class PluginConfig {
    private final YamlConfiguration config;

    public PluginConfig(com.example.mcauthpro.McAuthPro plugin) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

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

    public int getMaxLoginAttempts() {
        return config.getInt("security.max-login-attempts", 5);
    }

    public int getLockoutSeconds() {
        return config.getInt("security.lockout-seconds", 60);
    }

    public boolean isAllowMultipleLoginsPerIp() {
        return config.getBoolean("security.allow-multiple-logins-per-ip", true);
    }

    public int getSessionTimeoutSeconds() {
        return config.getInt("session.timeout-seconds", 180);
    }

    public int getRememberIpMinutes() {
        return config.getInt("session.remember-ip-minutes", 30);
    }

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

    public int getHttpTimeoutSeconds() {
        return config.getInt("verification.http-timeout-seconds", 10);
    }

    public boolean isWhitelistEnabled() {
        return config.getBoolean("whitelist.enabled", false);
    }

    public String getWhitelistKickMessage() {
        return config.getString("whitelist.kick-message", "你不在白名单中");
    }

    public boolean isBlacklistEnabled() {
        return config.getBoolean("blacklist.enabled", false);
    }

    public String getBlacklistKickMessage() {
        return config.getString("blacklist.kick-message", "你已被加入黑名单");
    }

    public boolean isVerifyEnabled() {
        return config.getBoolean("verification.enabled", true);
    }
}