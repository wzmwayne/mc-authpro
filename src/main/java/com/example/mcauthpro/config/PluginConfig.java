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

    public String getChamberWorld() {
        return config.getString("verification-chamber.world", "world");
    }

    public double getChamberX() {
        return config.getDouble("verification-chamber.x", 0);
    }

    public double getChamberY() {
        return config.getDouble("verification-chamber.y", 100);
    }

    public double getChamberZ() {
        return config.getDouble("verification-chamber.z", 0);
    }

    public boolean isForceSpectator() {
        return config.getBoolean("verification-chamber.force-spectator", true);
    }

    public int getMaxViewDistance() {
        return config.getInt("verification-chamber.max-view-distance", 2);
    }

    public boolean isChamberEnabled() {
        return config.getBoolean("verification-chamber.enabled", true);
    }

    public int getMaxConcurrentUnauth() {
        return config.getInt("anti-dos.max-concurrent-unauth", 5);
    }

    public int getVerificationTimeout() {
        return config.getInt("anti-dos.verification-timeout", 300);
    }

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

    public int getVerificationCountdown() {
        return config.getInt("verification.countdown-seconds", 30);
    }
}