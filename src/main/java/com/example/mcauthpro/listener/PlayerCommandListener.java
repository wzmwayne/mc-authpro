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
        "login", "register", "verify", "logout"
    );

    public PlayerCommandListener(AuthService authService) {
        this.authService = authService;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!authService.isFullyAuthenticated(player)) {
            String command = event.getMessage().toLowerCase();
            String commandName = command.split(" ")[0].substring(1);
            if (!ALLOWED_COMMANDS.contains(commandName)) {
                event.setCancelled(true);
            }
        }
    }
}