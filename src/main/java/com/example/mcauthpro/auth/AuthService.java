package com.example.mcauthpro.auth;

import com.example.mcauthpro.config.PluginConfig;
import com.example.mcauthpro.network.RealIpResolver;
import com.example.mcauthpro.network.TurnstileValidator;
import com.example.mcauthpro.auth.SessionManager.LoginSession;
import org.bukkit.entity.Player;

import java.util.List;

public class AuthService {
    private final com.example.mcauthpro.McAuthPro plugin;
    private final StorageService storageService;
    private final SessionManager sessionManager;
    private final TurnstileValidator turnstileValidator;
    private final RealIpResolver realIpResolver;
    private final PluginConfig config;

    public AuthService(com.example.mcauthpro.McAuthPro plugin,
                       StorageService storageService,
                       SessionManager sessionManager,
                       TurnstileValidator turnstileValidator,
                       RealIpResolver realIpResolver,
                       PluginConfig config) {
        this.plugin = plugin;
        this.storageService = storageService;
        this.sessionManager = sessionManager;
        this.turnstileValidator = turnstileValidator;
        this.realIpResolver = realIpResolver;
        this.config = config;
    }

    public boolean isRegistered(Player player) {
        return storageService.isRegistered(player.getUniqueId().toString());
    }

    public boolean register(Player player, String password) {
        if (isRegistered(player)) {
            return false;
        }
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(password, salt);
        PlayerData playerData = new PlayerData(
            player.getName(),
            player.getUniqueId().toString(),
            hash,
            salt,
            List.of(realIpResolver.getRealIp(player)),
            System.currentTimeMillis()
        );
        storageService.savePlayer(playerData);
        return true;
    }

    public boolean login(Player player, String password) {
        PlayerData playerData = storageService.loadPlayer(player.getUniqueId().toString());
        if (playerData == null) {
            return false;
        }
        return playerData.verifyPassword(password);
    }

    public boolean verifyTurnstileToken(Player player, String token) {
        LoginSession session = sessionManager.getSession(player);
        if (session == null) {
            return false;
        }
        String remoteIp = realIpResolver.getRealIp(player);
        TurnstileValidator.ValidateResult result = turnstileValidator.validate(token, remoteIp);
        if (result.isSuccess()) {
            sessionManager.markVerified(player);
            sessionManager.removeSession(player);
            return true;
        }
        return false;
    }

    public boolean isFullyAuthenticated(Player player) {
        if (!isRegistered(player)) {
            return false;
        }
        return sessionManager.isVerified(player);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public RealIpResolver getRealIpResolver() {
        return realIpResolver;
    }

    public PluginConfig getConfig() {
        return config;
    }

    public StorageService getStorageService() {
        return storageService;
    }
}