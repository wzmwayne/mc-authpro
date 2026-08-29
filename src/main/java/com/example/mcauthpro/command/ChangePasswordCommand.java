package com.example.mcauthpro.command;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.config.Messages;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ChangePasswordCommand implements CommandExecutor {
    private final McAuthPro plugin;
    private final AuthService authService;

    public ChangePasswordCommand(McAuthPro plugin, AuthService authService) {
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
        Messages msg = plugin.getMessages();

        if (!authService.isFullyAuthenticated(player)) {
            player.sendMessage(msg.get("changepwd-not-logged-in"));
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(msg.get("changepwd-usage"));
            return true;
        }

        String oldPassword = args[0];
        String newPassword = args[1];

        if (newPassword.length() < 6) {
            player.sendMessage(msg.get("changepwd-too-short"));
            return true;
        }

        if (authService.changePassword(player, oldPassword, newPassword)) {
            player.sendMessage(msg.get("changepwd-success"));
        } else {
            player.sendMessage(msg.get("changepwd-old-wrong"));
        }

        return true;
    }
}