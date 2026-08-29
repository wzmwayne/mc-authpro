package com.example.mcauthpro.command;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import com.example.mcauthpro.config.Messages;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VerifyCommand implements CommandExecutor {
    private final McAuthPro plugin;
    private final AuthService authService;

    public VerifyCommand(McAuthPro plugin, AuthService authService) {
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

        if (sessionManager.isTurnstileVerified(player)) {
            player.sendMessage(msg.get("verify-already-done"));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(msg.get("verify-usage"));
            return true;
        }

        String key = args[0];
        String token = args[1];

        String playerName = sessionManager.getPlayerNameByKey(key);
        if (playerName == null || !playerName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(msg.get("verify-invalid-key"));
            return true;
        }

        if (authService.verifyTurnstileToken(player, key, token)) {
            sessionManager.stopCountdown(player);

            if (authService.isRegistered(player)) {
                player.sendTitle(
                    msg.get("verify-success-title"),
                    msg.get("verify-registered-prompt"),
                    0, 60, 20
                );
                player.sendMessage(msg.get("verify-success-registered"));
            } else {
                player.sendTitle(
                    msg.get("verify-success-title"),
                    msg.get("verify-unregistered-prompt"),
                    0, 60, 20
                );
                player.sendMessage(msg.get("verify-success-unregistered"));
            }
        } else {
            player.sendMessage(msg.get("verify-failed"));
        }

        return true;
    }
}