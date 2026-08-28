package com.example.mcauthpro.listener;

import com.example.mcauthpro.auth.AuthService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {
    private final AuthService authService;

    public PlayerInteractListener(AuthService authService) {
        this.authService = authService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (authService.isFullyAuthenticated(player)) return;
        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK ||
            action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK ||
            action == Action.PHYSICAL) {
            event.setCancelled(true);
        }
    }
}