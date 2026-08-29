package com.example.mcauthpro.listener;

import com.example.mcauthpro.auth.AuthService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;
import java.util.List;

public class PlayerCommandListener implements Listener {
    private final AuthService authService;
    private static final List<String> ALLOWED_COMMANDS = Arrays.asList(
        "/login", "/register", "/reg", "/verify", "/changepwd", "/cpwd", "/changepassword"
    );

    public PlayerCommandListener(AuthService authService) {
        this.authService = authService;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (authService.isFullyAuthenticated(player)) return;
        String message = event.getMessage().toLowerCase();
        for (String cmd : ALLOWED_COMMANDS) {
            if (message.startsWith(cmd)) return;
        }
        event.setCancelled(true);
    }
}