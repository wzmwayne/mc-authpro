package com.example.mcauthpro.command;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import com.example.mcauthpro.config.Messages;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RegisterCommand implements CommandExecutor {
    private final McAuthPro plugin;
    private final AuthService authService;

    public RegisterCommand(McAuthPro plugin, AuthService authService) {
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
            player.sendMessage(msg.get("register-already-logged-in"));
            return true;
        }

        if (!sessionManager.isTurnstileVerified(player)) {
            player.sendMessage(msg.get("register-not-verified"));
            return true;
        }

        if (authService.isRegistered(player)) {
            player.sendMessage(msg.get("register-already-registered"));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(msg.get("register-usage"));
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];
        if (!password.equals(confirmPassword)) {
            player.sendMessage(msg.get("register-password-mismatch"));
            return true;
        }

        if (password.length() < 6) {
            player.sendMessage(msg.get("register-password-too-short"));
            return true;
        }

        if (authService.register(player, password)) {
            player.sendTitle(
                msg.get("register-success-title"),
                msg.get("register-success-subtitle"),
                0, 60, 20
            );
            player.sendMessage(msg.get("register-success-hint"));
        } else {
            player.sendMessage(msg.get("register-failed"));
        }

        return true;
    }
}