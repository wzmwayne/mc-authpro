package com.example.mcauthpro.listener;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final McAuthPro plugin;
    private final AuthService authService;

    public PlayerJoinListener(McAuthPro plugin, AuthService authService) {
        this.plugin = plugin;
        this.authService = authService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (authService.isFullyAuthenticated(player)) {
            player.sendMessage(ChatColor.GREEN + "欢迎回来，" + player.getName() + "！");
            return;
        }

        if (!authService.isRegistered(player)) {
            player.sendMessage(ChatColor.GREEN + "欢迎来到服务器！");
            player.sendMessage(ChatColor.YELLOW + "请先注册：/register <密码> <密码>");
        } else {
            player.sendMessage(ChatColor.GREEN + "欢迎回来，" + player.getName() + "！");
            player.sendMessage(ChatColor.YELLOW + "请先登录：/login <密码>");
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            teleportToLoginWorld(player);
        }, 5L);
    }

    private void teleportToLoginWorld(Player player) {
        if (player == null || !player.isOnline()) return;

        World loginWorld = plugin.getLoginWorldManager().getLoginWorld();
        if (loginWorld == null) {
            loginWorld = plugin.getServer().getWorlds().get(0);
        }

        Location loc = plugin.getLoginWorldManager().getSpawnLocation();
        if (loc == null) {
            loc = loginWorld.getSpawnLocation();
        }

        player.teleport(loc);

        if (plugin.getPluginConfig().isForceSpectator()) {
            player.setGameMode(GameMode.SPECTATOR);
        }
    }
}