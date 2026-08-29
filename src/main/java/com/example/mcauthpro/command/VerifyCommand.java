package com.example.mcauthpro.command;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import com.example.mcauthpro.listener.PlayerJoinListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行。");
            return true;
        }

        Player player = (Player) sender;
        SessionManager sessionManager = plugin.getSessionManager();

        if (sessionManager.isTurnstileVerified(player)) {
            player.sendMessage(ChatColor.GREEN + "你已完成人机验证。");
            return true;
        }

        if (!sessionManager.hasSession(player)) {
            player.sendMessage(ChatColor.RED + "未找到验证会话，请重新加入服务器。");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "用法：/verify <令牌>");
            return true;
        }

        String token = args[0];
        if (authService.verifyTurnstileToken(player, token)) {
            sessionManager.stopCountdown(player);
            sessionManager.markTurnstileVerified(player);

            player.sendTitle("§a验证通过", "§f请通过 /login 登陆 或 /reg 注册", 0, 60, 20);
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "§e=== 人机验证通过 ===");
            player.sendMessage(ChatColor.YELLOW + "已注册玩家请执行：/login <密码>");
            player.sendMessage(ChatColor.YELLOW + "未注册玩家请执行：/reg <密码> <密码>");
            player.sendMessage("");
        } else {
            player.sendMessage(ChatColor.RED + "验证失败，令牌无效或已过期。");
        }

        return true;
    }
}