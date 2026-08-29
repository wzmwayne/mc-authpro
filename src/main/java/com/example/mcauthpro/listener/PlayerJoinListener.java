package com.example.mcauthpro.listener;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import com.example.mcauthpro.config.Messages;
import com.example.mcauthpro.network.VerificationSite;
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
        Messages msg = plugin.getMessages();

        if (authService.isFullyAuthenticated(player)) {
            player.sendMessage(msg.get("join-welcome", "{player}", player.getName()));
            return;
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            teleportToLoginWorld(player);
            startVerificationFlow(player);
        }, 5L);
    }

    private void startVerificationFlow(Player player) {
        SessionManager sessionManager = plugin.getSessionManager();
        Messages msg = plugin.getMessages();

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
        player.sendMessage(msg.get("verify-url-message",
            "{url}", url,
            "{seconds}", String.valueOf(countdown)
        ));
        player.sendMessage("");

        player.sendTitle(
            msg.get("verify-title"),
            msg.get("verify-subtitle", "{seconds}", String.valueOf(countdown)),
            0, 25, 5
        );

        sessionManager.startCountdown(player, countdown);
    }

    public void showLoginPrompt(Player player) {
        Messages msg = plugin.getMessages();
        player.sendTitle(
            msg.get("verify-success-title"),
            msg.get("verify-registered-prompt"),
            0, 60, 20
        );
        player.sendMessage("");
        player.sendMessage(msg.get("verify-success-registered"));
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