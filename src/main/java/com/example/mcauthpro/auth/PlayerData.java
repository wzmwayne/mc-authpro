package com.example.mcauthpro.auth;

import java.util.List;

public class PlayerData {
    private final String username;
    private final String uuid;
    private String passwordHash;
    private String salt;
    private List<String> ipHistory;
    private long registeredAt;

    public PlayerData(String username, String uuid, String passwordHash, String salt, List<String> ipHistory, long registeredAt) {
        this.username = username;
        this.uuid = uuid;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.ipHistory = ipHistory;
        this.registeredAt = registeredAt;
    }

    public String getUsername() {
        return username;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public List<String> getIpHistory() {
        return ipHistory;
    }

    public void setIpHistory(List<String> ipHistory) {
        this.ipHistory = ipHistory;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(long registeredAt) {
        this.registeredAt = registeredAt;
    }

    public boolean verifyPassword(String password) {
        return PasswordHasher.verifyPassword(password, this.passwordHash, this.salt);
    }
}