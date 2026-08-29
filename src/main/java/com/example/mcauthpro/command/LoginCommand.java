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

        if (!authService.isRegistered(player)) {
            player.sendMessage(ChatColor.RED + "你未注册，请先执行：/reg <密码> <密码>");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "用法：/login <密码>");
            return true;
        }

        String password = args[0];
        if (authService.login(player, password)) {
            sessionManager.markLoginVerified(player);
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            player.sendTitle("§a登录成功", "§f欢迎回来，" + player.getName() + "！", 0, 60, 20);
            player.sendMessage(ChatColor.GREEN + "登录成功！欢迎回来，" + player.getName() + "！");
        } else {
            player.sendMessage(ChatColor.RED + "密码错误，请重试。");
            player.kickPlayer("§c密码错误，请重新加入并重试");
        }

        return true;
    }
}