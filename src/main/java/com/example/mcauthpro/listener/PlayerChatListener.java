package com.example.mcauthpro.listener;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.config.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
    private final McAuthPro plugin;
    private final AuthService authService;

    public PlayerChatListener(McAuthPro plugin, AuthService authService) {
        this.plugin = plugin;
        this.authService = authService;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (authService.isFullyAuthenticated(player)) return;

        event.setCancelled(true);
        Messages msg = plugin.getMessages();

        if (!authService.getSessionManager().isTurnstileVerified(player)) {
            player.sendMessage(msg.get("login-not-verified"));
        } else if (!authService.isRegistered(player)) {
            player.sendMessage(msg.get("verify-success-unregistered"));
        } else {
            player.sendMessage(msg.get("login-not-registered"));
        }
    }
}