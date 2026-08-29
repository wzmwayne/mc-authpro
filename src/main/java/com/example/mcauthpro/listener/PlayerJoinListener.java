package com.example.mcauthpro.listener;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import com.example.mcauthpro.network.VerificationSite;
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

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            teleportToLoginWorld(player);
            startVerificationFlow(player);
        }, 5L);
    }

    private void startVerificationFlow(Player player) {
        SessionManager sessionManager = plugin.getSessionManager();

        if (sessionManager.isTurnstileVerified(player)) {
            showLoginPrompt(player);
            return;
        }

        SessionManager.LoginSession session = sessionManager.createSession(player);

        VerificationSite verificationSite = new VerificationSite(authService.getConfig());
        String url = verificationSite.getVerificationUrl(session.getPlayerUUID());

        int countdown = plugin.getPluginConfig().getVerificationCountdown();
        session.setCountdownSeconds(countdown);

        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "§e=== 人机验证 ===");
        player.sendMessage(ChatColor.YELLOW + "请在浏览器中打开以下链接完成验证：");
        player.sendMessage(ChatColor.AQUA + url);
        player.sendMessage(ChatColor.YELLOW + "复制令牌后执行：/verify <令牌>");
        player.sendMessage(ChatColor.RED + "§c请在 " + countdown + " 秒内完成，否则将被踢出！");
        player.sendMessage("");

        player.sendTitle("§e人机验证", "§f请在 §c" + countdown + " §f秒内完成验证", 0, 25, 5);

        sessionManager.startCountdown(player, countdown);
    }

    public void showLoginPrompt(Player player) {
        player.sendTitle("§a验证通过", "§f请通过 /login 登陆 或 /reg 注册", 0, 60, 20);
        player.sendMessage("");
        player.sendMessage(ChatColor.GREEN + "§e=== 人机验证通过 ===");
        player.sendMessage(ChatColor.YELLOW + "已注册玩家请执行：/login <密码>");
        player.sendMessage(ChatColor.YELLOW + "未注册玩家请执行：/reg <密码> <密码>");
        player.sendMessage("");
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