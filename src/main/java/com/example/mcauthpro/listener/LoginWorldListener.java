package com.example.mcauthpro.listener;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.world.LoginWorldManager;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class LoginWorldListener implements Listener {
    private final McAuthPro plugin;

    public LoginWorldListener(McAuthPro plugin) {
        this.plugin = plugin;
    }

    private boolean isInLoginWorld(Player player) {
        World loginWorld = plugin.getLoginWorldManager().getLoginWorld();
        return loginWorld != null && player.getWorld().equals(loginWorld);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isInLoginWorld(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isInLoginWorld(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (isInLoginWorld(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (isInLoginWorld(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (isInLoginWorld(player)) {
                event.setCancelled(true);
            }
        }
        if (event.getEntity() instanceof Player player) {
            if (isInLoginWorld(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (isInLoginWorld(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isInLoginWorld(player)) return;

        LoginWorldManager mgr = plugin.getLoginWorldManager();
        int platformY = plugin.getPluginConfig().getLoginWorldPlatformY();
        int size = plugin.getPluginConfig().getLoginWorldPlatformSize();

        double x = event.getTo().getX();
        double y = event.getTo().getY();
        double z = event.getTo().getZ();

        if (y > platformY + 2 || y < platformY - 1 ||
            Math.abs(x) > size + 2 || Math.abs(z) > size + 2) {
            event.setTo(mgr.getSpawnLocation());
        }
    }
}