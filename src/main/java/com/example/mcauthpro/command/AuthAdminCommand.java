package com.example.mcauthpro.command;

import com.example.mcauthpro.McAuthPro;
import org.bukkit.ChatColor;
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
        if (!sender.hasPermission("mcauthpro.admin")) {
            sender.sendMessage(ChatColor.RED + "你没有权限执行此命令。");
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
        sender.sendMessage(ChatColor.YELLOW + "正在重构登录世界...");
        plugin.getLoginWorldManager().rebuildWorld();
        sender.sendMessage(ChatColor.GREEN + "登录世界重构完成！");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== MC AuthPro 管理命令 ===");
        sender.sendMessage(ChatColor.YELLOW + "/authadmin rebuildlogin" + ChatColor.WHITE + " - 重构登录世界");
    }
}