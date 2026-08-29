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
        getLogger().info("MC AuthPro 插件已启用");
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
        getServer().getPluginManager().registerEvents(new PlayerChatListener(authService), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(authService), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(sessionManager), this);
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