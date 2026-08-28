package com.example.mcauthpro.config;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class Messages {
    private final YamlConfiguration messages;

    public Messages(com.example.mcauthpro.McAuthPro plugin) {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key) {
        return messages.getString(key, key);
    }

    public String getVerifySuccess() {
        return getMessage("verify.success");
    }

    public String getVerifyInvalid() {
        return getMessage("verify.invalid");
    }

    public String getVerifyExpired() {
        return getMessage("verify.expired");
    }

    public String getVerifyNoSession() {
        return getMessage("verify.no-session");
    }

    public String getVerifyDuplicate() {
        return getMessage("verify.duplicate");
    }

    public String getVerifyNetworkError() {
        return getMessage("verify.network-error");
    }

    public String getLoginPrompt() {
        return getMessage("login.prompt");
    }

    public String getBookGiven() {
        return getMessage("login.book-given");
    }

    public String getVerificationStart() {
        return getMessage("login.verification-start");
    }

    public String getRegisterSuccess() {
        return getMessage("register.success");
    }

    public String getRegisterPasswordMismatch() {
        return getMessage("register.password-mismatch");
    }

    public String getRegisterAlreadyRegistered() {
        return getMessage("register.already-registered");
    }

    public String getLoginSuccess() {
        return getMessage("login.success");
    }

    public String getLoginFailed() {
        return getMessage("login.failed");
    }

    public String getLoginLocked() {
        return getMessage("login.locked");
    }

    public String getLogoutSuccess() {
        return getMessage("logout.success");
    }
}