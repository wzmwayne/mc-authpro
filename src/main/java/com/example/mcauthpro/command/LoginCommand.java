package com.example.mcauthpro.command;

import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import com.example.mcauthpro.network.VerificationSite;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LoginCommand implements CommandExecutor {
    private final AuthService authService;

    public LoginCommand(AuthService authService) {
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
        if (!authService.isRegistered(player)) {
            player.sendMessage(ChatColor.RED + "请先注册：/register <密码> <密码>");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "用法：/login <密码>");
            return true;
        }
        String password = args[0];
        if (authService.login(player, password)) {
            SessionManager.LoginSession session = authService.getSessionManager().createSession(player);
            VerificationSite verificationSite = new VerificationSite(authService.getConfig());
            String url = verificationSite.getVerificationUrl(session.getPlayerUUID());
            player.sendMessage(ChatColor.GREEN + "登录验证已启动！请在 5 分钟内访问以下链接完成验证：");
            player.sendMessage(ChatColor.GRAY + url);
            player.sendMessage(ChatColor.YELLOW + "完成验证后，请复制令牌并执行：/verify <令牌>");
        } else {
            player.sendMessage(ChatColor.RED + "登录失败，密码错误。");
        }
        return true;
    }
}