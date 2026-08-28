package com.example.mcauthpro.command;

import com.example.mcauthpro.auth.AuthService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RegisterCommand implements CommandExecutor {
    private final AuthService authService;

    public RegisterCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "此命令只能由玩家执行。");
            return true;
        }
        Player player = (Player) sender;
        if (authService.isRegistered(player)) {
            player.sendMessage(ChatColor.RED + "你已注册。");
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "用法：/register <密码> <密码>");
            return true;
        }
        String password = args[0];
        String confirmPassword = args[1];
        if (!password.equals(confirmPassword)) {
            player.sendMessage(ChatColor.RED + "两次输入的密码不一致。");
            return true;
        }
        if (password.length() < 6) {
            player.sendMessage(ChatColor.RED + "密码长度至少为 6 位。");
            return true;
        }
        if (authService.register(player, password)) {
            player.sendMessage(ChatColor.GREEN + "注册成功！请记住你的密码。");
            player.sendMessage(ChatColor.YELLOW + "请执行 /login <密码> 进行登录验证。");
        } else {
            player.sendMessage(ChatColor.RED + "注册失败，请稍后重试。");
        }
        return true;
    }
}