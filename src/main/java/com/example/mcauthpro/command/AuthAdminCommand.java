package com.example.mcauthpro.command;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.config.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AuthAdminCommand implements CommandExecutor {
    private final McAuthPro plugin;

    public AuthAdminCommand(McAuthPro plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Messages msg = plugin.getMessages();

        if (!sender.hasPermission("mcauthpro.admin")) {
            sender.sendMessage(msg.get("admin-no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "rebuildlogin":
                rebuildLogin(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void rebuildLogin(CommandSender sender) {
        Messages msg = plugin.getMessages();
        sender.sendMessage(msg.get("admin-rebuilding"));
        plugin.getLoginWorldManager().rebuildWorld();
        sender.sendMessage(msg.get("admin-rebuild-done"));
    }

    private void sendHelp(CommandSender sender) {
        Messages msg = plugin.getMessages();
        sender.sendMessage(msg.get("admin-help-title"));
        sender.sendMessage(msg.get("admin-rebuildlogin"));
    }
}