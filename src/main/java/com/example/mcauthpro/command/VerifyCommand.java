package com.example.mcauthpro.command;

import com.example.mcauthpro.auth.AuthService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VerifyCommand implements CommandExecutor {
    private final AuthService authService;

    public VerifyCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行。");
            return true;
        }
        Player player = (Player) sender;
        if (authService.isFullyAuthenticated(player)) {
            player.sendMessage(ChatColor.GREEN + "你已登录。");
            return true;
        }
        if (!authService.getSessionManager().hasSession(player)) {
            player.sendMessage(ChatColor.RED + "未找到待验证会话，请先执行 /login。");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "用法：/verify <令牌>");
            return true;
        }
        String token = args[0];
        if (authService.verifyTurnstileToken(player, token)) {
            player.sendMessage(ChatColor.GREEN + "验证成功！欢迎回来，" + player.getName() + "！");
        } else {
            player.sendMessage(ChatColor.RED + "验证失败，令牌无效或已过期。");
        }
        return true;
    }
}