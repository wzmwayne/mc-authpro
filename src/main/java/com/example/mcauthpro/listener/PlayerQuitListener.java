package com.example.mcauthpro.listener;

import com.example.mcauthpro.auth.SessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private final SessionManager sessionManager;

    public PlayerQuitListener(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        sessionManager.remove(player);
    }
}