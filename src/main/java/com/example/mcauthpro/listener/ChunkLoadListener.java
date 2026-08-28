package com.example.mcauthpro.listener;

import com.example.mcauthpro.auth.AuthService;
import io.papermc.paper.event.packet.PlayerChunkLoadEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChunkLoadListener implements Listener {
    private final AuthService authService;

    public ChunkLoadListener(AuthService authService) {
        this.authService = authService;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChunkLoad(PlayerChunkLoadEvent event) {
        Player player = event.getPlayer();
        if (authService.isFullyAuthenticated(player)) return;
        player.setViewDistance(2);
    }
}