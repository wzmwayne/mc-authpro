package com.example.mcauthpro;

import com.example.mcauthpro.config.PluginConfig;
import com.example.mcauthpro.config.Messages;
import com.example.mcauthpro.auth.AuthService;
import com.example.mcauthpro.auth.SessionManager;
import com.example.mcauthpro.auth.StorageService;
import com.example.mcauthpro.network.RealIpResolver;
import com.example.mcauthpro.network.TurnstileValidator;
import com.example.mcauthpro.listener.PlayerJoinListener;
import com.example.mcauthpro.listener.PlayerMoveListener;
import com.example.mcauthpro.listener.PlayerChatListener;
import com.example.mcauthpro.listener.PlayerCommandListener;
import com.example.mcauthpro.listener.PlayerQuitListener;
import com.example.mcauthpro.listener.BlockBreakListener;
import com.example.mcauthpro.listener.BlockPlaceListener;
import com.example.mcauthpro.listener.PlayerInteractListener;
import com.example.mcauthpro.listener.PlayerInteractEntityListener;
import com.example.mcauthpro.listener.InventoryClickListener;
import com.example.mcauthpro.listener.ChunkLoadListener;
import com.example.mcauthpro.listener.GameModeChangeListener;
import com.example.mcauthpro.command.LoginCommand;
import com.example.mcauthpro.command.RegisterCommand;
import com.example.mcauthpro.command.VerifyCommand;
import com.example.mcauthpro.command.AuthAdminCommand;
import com.example.mcauthpro.command.ChangePasswordCommand;
import com.example.mcauthpro.listener.LoginWorldListener;
import com.example.mcauthpro.world.LoginWorldManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class McAuthPro extends JavaPlugin {
    private static McAuthPro instance;
    private PluginConfig config;
    private Messages messages;
    private AuthService authService;
    private StorageService storageService;
    private SessionManager sessionManager;
    private RealIpResolver realIpResolver;
    private TurnstileValidator turnstileValidator;
    private LoginWorldManager loginWorldManager;

    @Override
    public void onEnable() {
        instance = this;
        this.config = new PluginConfig(this);
        this.messages = new Messages(this);
        this.storageService = new StorageService(this);
        this.realIpResolver = new RealIpResolver(config);
        this.sessionManager = new SessionManager(this);
        this.turnstileValidator = new TurnstileValidator(config);
        this.authService = new AuthService(this, storageService, sessionManager, turnstileValidator, realIpResolver, config);
        this.loginWorldManager = new LoginWorldManager(this);
        registerCommands();
        registerListeners();
        disableOnlineMode();
        printBanner();
    }

    private void disableOnlineMode() {
        java.io.File serverProps = new java.io.File("server.properties");
        if (!serverProps.exists()) {
            getLogger().warning("未找到 server.properties，无法自动关闭在线验证。");
            return;
        }
        java.util.Properties props = new java.util.Properties();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(serverProps)) {
            props.load(fis);
        } catch (Exception e) {
            getLogger().warning("读取 server.properties 失败: " + e.getMessage());
            return;
        }
        if (!"false".equalsIgnoreCase(props.getProperty("online-mode", ""))) {
            props.setProperty("online-mode", "false");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(serverProps)) {
                props.store(fos, "Modified by McAuthPro - online-mode disabled");
            } catch (Exception e) {
                getLogger().warning("写入 server.properties 失败: " + e.getMessage());
                return;
            }
            getLogger().info("已将 online-mode 设置为 false（离线模式）。");
        }
    }

    private void printBanner() {
        String[] lines = {
            "",
            "&8&m========================================",
            "&b   ___    __  _                 _       ",
            "&b  / _ \\  / _\\| |__   ___   ___ | |_     ",
            "&b | | | | \\ \\ | '_ \\ / _ \\ / _ \\| __|    ",
            "&b | | | |  \\ \\| | | | (_) | (_) | |_     ",
            "&b  \\___/   \\/_|_| |_|\\___/ \\___/ \\__|    ",
            "&8&m========================================",
            "&e  Paper 1.21.x 安全插件 &7v" + getDescription().getVersion(),
            "&a  ✓ 在线验证已关闭 (online-mode=false)",
            "&a  ✓ 账号登录 + 人机验证已启用",
            "&a  ✓ 登录世界已就绪",
            "&8&m========================================",
            ""
        };
        for (String line : lines) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    @Override
    public void onDisable() {
        if (sessionManager != null) {
            sessionManager.shutdown();
        }
        getLogger().info("MC AuthPro 插件已停用");
    }

    private void registerCommands() {
        getCommand("login").setExecutor(new LoginCommand(this, authService));
        getCommand("register").setExecutor(new RegisterCommand(this, authService));
        getCommand("verify").setExecutor(new VerifyCommand(this, authService));
        getCommand("changepwd").setExecutor(new ChangePasswordCommand(this, authService));
        getCommand("authadmin").setExecutor(new AuthAdminCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this, authService), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(authService), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this, authService), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(authService), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(sessionManager, authService), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(authService), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(authService), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(authService), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(authService), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(authService), this);
        getServer().getPluginManager().registerEvents(new ChunkLoadListener(authService), this);
        getServer().getPluginManager().registerEvents(new GameModeChangeListener(authService), this);
        getServer().getPluginManager().registerEvents(new LoginWorldListener(this), this);
    }

    public static McAuthPro getInstance() {
        return instance;
    }

    public PluginConfig getPluginConfig() {
        return config;
    }

    public Messages getMessages() {
        return messages;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public RealIpResolver getRealIpResolver() {
        return realIpResolver;
    }

    public LoginWorldManager getLoginWorldManager() {
        return loginWorldManager;
    }
}