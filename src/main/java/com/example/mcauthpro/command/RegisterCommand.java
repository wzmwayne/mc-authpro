package com.example.mcauthpro.command;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行。");
            return true;
        }

        Player player = (Player) sender;
        SessionManager sessionManager = plugin.getSessionManager();

        if (authService.isFullyAuthenticated(player)) {
            player.sendMessage(ChatColor.GREEN + "你已登录。");
            return true;
        }

        if (!sessionManager.isTurnstileVerified(player)) {
            player.sendMessage(ChatColor.RED + "请先完成人机验证：/verify <令牌>");
            return true;
        }

        if (authService.isRegistered(player)) {
            player.sendMessage(ChatColor.RED + "你已注册，请执行：/login <密码>");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "用法：/reg <密码> <密码>");
            return true;
        }

        String password = args[0];
        String confirmPassword = args[1];
        if (!password.equals(confirmPassword)) {
            player.sendMessage(ChatColor.RED + "两次输入的密码不一致。");
            return true;
        }

        if (password.length() < 6) {
            player.sendMessage(ChatColor.RED + "密码长度至少为6位。");
            return true;
        }

        if (authService.register(player, password)) {
            player.sendTitle("§a注册成功", "§f请通过 /login <密码> 登录", 0, 60, 20);
            player.sendMessage("");
            player.sendMessage(ChatColor.GREEN + "§e=== 注册成功 ===");
            player.sendMessage(ChatColor.YELLOW + "请执行：/login <密码>");
            player.sendMessage("");
        } else {
            player.sendMessage(ChatColor.RED + "注册失败，请稍后重试。");
        }

        return true;
    }
}