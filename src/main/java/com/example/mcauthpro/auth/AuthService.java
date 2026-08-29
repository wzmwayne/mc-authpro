package com.example.mcauthpro.auth;

import com.example.mcauthpro.config.PluginConfig;
import com.example.mcauthpro.network.RealIpResolver;
import com.example.mcauthpro.network.TurnstileValidator;
import org.bukkit.Location;
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
        String remoteIp = realIpResolver.getRealIp(player);
        TurnstileValidator.ValidateResult result = turnstileValidator.validate(token, remoteIp);
        if (result.isSuccess()) {
            sessionManager.markTurnstileVerified(player);
            return true;
        }
        return false;
    }

    public boolean isFullyAuthenticated(Player player) {
        return sessionManager.isLoginVerified(player);
    }

    public void saveLastLocation(Player player) {
        Location loc = player.getLocation();
        PlayerData playerData = storageService.loadPlayer(player.getUniqueId().toString());
        if (playerData == null) return;
        playerData.setLastWorld(loc.getWorld().getName());
        playerData.setLastX(loc.getX());
        playerData.setLastY(loc.getY());
        playerData.setLastZ(loc.getZ());
        playerData.setLastYaw(loc.getYaw());
        playerData.setLastPitch(loc.getPitch());
        storageService.savePlayer(playerData);
    }

    public Location getLastLocation(Player player) {
        PlayerData playerData = storageService.loadPlayer(player.getUniqueId().toString());
        if (playerData == null) return null;
        org.bukkit.World world = plugin.getServer().getWorld(playerData.getLastWorld());
        if (world == null) world = plugin.getServer().getWorlds().get(0);
        return new Location(world, playerData.getLastX(), playerData.getLastY(),
                           playerData.getLastZ(), playerData.getLastYaw(), playerData.getLastPitch());
    }

    public boolean changePassword(Player player, String oldPassword, String newPassword) {
        PlayerData playerData = storageService.loadPlayer(player.getUniqueId().toString());
        if (playerData == null) {
            return false;
        }
        if (!playerData.verifyPassword(oldPassword)) {
            return false;
        }
        String newSalt = PasswordHasher.generateSalt();
        String newHash = PasswordHasher.hashPassword(newPassword, newSalt);
        PlayerData updated = new PlayerData(
            playerData.getUsername(),
            playerData.getUuid(),
            newHash,
            newSalt,
            playerData.getIpHistory(),
            playerData.getRegisteredAt()
        );
        storageService.savePlayer(updated);
        return true;
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