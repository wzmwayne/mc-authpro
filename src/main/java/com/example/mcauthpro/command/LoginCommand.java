package com.example.mcauthpro.command;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import com.example.mcauthpro.config.Messages;
import org.bukkit.GameMode;
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
            player.setGameMode(GameMode.SURVIVAL);
            player.sendTitle(
                msg.get("login-success-title"),
                msg.get("login-success-subtitle", "{player}", player.getName()),
                0, 60, 20
            );
            player.sendMessage(msg.get("login-success", "{player}", player.getName()));
        } else {
            player.sendMessage(msg.get("login-failed"));
            player.kickPlayer(msg.get("login-failed-kick"));
        }

        return true;
    }
}