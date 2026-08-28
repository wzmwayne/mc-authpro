package com.example.mcauthpro.listener;

import com.example.mcauthpro.auth.AuthService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {
    private final AuthService authService;

    public BlockPlaceListener(AuthService authService) {
        this.authService = authService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (authService.isFullyAuthenticated(player)) return;
        event.setCancelled(true);
    }
}