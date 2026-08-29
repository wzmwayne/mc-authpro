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

        if (!sessionManager.hasSession(player)) {
            player.sendMessage(msg.get("verify-no-session"));
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(msg.get("verify-usage"));
            return true;
        }

        String token = args[0];
        if (authService.verifyTurnstileToken(player, token)) {
            sessionManager.stopCountdown(player);
            sessionManager.markTurnstileVerified(player);

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