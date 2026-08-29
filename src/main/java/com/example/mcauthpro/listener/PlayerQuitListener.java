package com.example.mcauthpro.listener;

import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final SessionManager sessionManager;
    private final AuthService authService;

    public PlayerQuitListener(SessionManager sessionManager, AuthService authService) {
        this.sessionManager = sessionManager;
        this.authService = authService;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (authService.isFullyAuthenticated(player)) {
            authService.saveLastLocation(player);
        }
        sessionManager.remove(player);
    }
}