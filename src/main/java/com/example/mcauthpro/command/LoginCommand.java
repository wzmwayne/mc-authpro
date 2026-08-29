package com.example.mcauthpro.command;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import com.example.mcauthpro.config.Messages;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LoginCommand implements CommandExecutor {
    private final McAuthPro plugin;
    private final AuthService authService;

    public LoginCommand(McAuthPro plugin, AuthService authService) {
        this.plugin = plugin;
        this.authService = authService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessages().get("player-only"));
            return true;
        }

        Player player = (Player) sender;
        SessionManager sessionManager = plugin.getSessionManager();
        Messages msg = plugin.getMessages();

        if (authService.isFullyAuthenticated(player)) {
            player.sendMessage(msg.get("login-already-logged-in"));
            return true;
        }

        if (!sessionManager.isTurnstileVerified(player)) {
            player.sendMessage(msg.get("login-not-verified"));
            return true;
        }

        if (!authService.isRegistered(player)) {
            player.sendMessage(msg.get("login-not-registered"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(msg.get("login-usage"));
            return true;
        }

        String password = args[0];
        if (authService.login(player, password)) {
            sessionManager.markLoginVerified(player);

            player.sendTitle(
                msg.get("login-success-title"),
                msg.get("login-success-subtitle"),
                0, 25, 5
            );

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (!player.isOnline()) return;

                for (World world : plugin.getServer().getWorlds()) {
                    for (Player p : world.getPlayers()) {
                        if (p.getName().equals(player.getName()) && !p.equals(player)) {
                            p.kickPlayer(msg.get("world-kick-self"));
                        }
                    }
                }

                Location lastLoc = authService.getLastLocation(player);

                player.sendTitle(
                    msg.get("login-success-enter-title"),
                    msg.get("login-success-enter-subtitle", "{player}", player.getName()),
                    0, 40, 20
                );

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (!player.isOnline()) return;
                    player.setGameMode(GameMode.SURVIVAL);
                    player.teleport(lastLoc);
                }, 60L);
            }, 60L);
        } else {
            player.sendMessage(msg.get("login-failed"));
            player.kickPlayer(msg.get("login-failed-kick"));
        }

        return true;
    }
}