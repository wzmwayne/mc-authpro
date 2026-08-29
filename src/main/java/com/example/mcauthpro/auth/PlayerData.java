package com.example.mcauthpro.auth;

import java.util.List;

public class PlayerData {
    private final String username;
    private final String uuid;
    private String passwordHash;
    private String salt;
    private List<String> ipHistory;
    private long registeredAt;
    private String lastWorld;
    private double lastX;
    private double lastY;
    private double lastZ;
    private float lastYaw;
    private float lastPitch;

    public PlayerData(String username, String uuid, String passwordHash, String salt,
                      List<String> ipHistory, long registeredAt) {
        this(username, uuid, passwordHash, salt, ipHistory, registeredAt,
             "world", 0, 100, 0, 0f, 0f);
    }

    public PlayerData(String username, String uuid, String passwordHash, String salt,
                      List<String> ipHistory, long registeredAt,
                      String lastWorld, double lastX, double lastY, double lastZ,
                      float lastYaw, float lastPitch) {
        this.username = username;
        this.uuid = uuid;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.ipHistory = ipHistory;
        this.registeredAt = registeredAt;
        this.lastWorld = lastWorld;
        this.lastX = lastX;
        this.lastY = lastY;
        this.lastZ = lastZ;
        this.lastYaw = lastYaw;
        this.lastPitch = lastPitch;
    }

    public String getUsername() { return username; }
    public String getUuid() { return uuid; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }
    public List<String> getIpHistory() { return ipHistory; }
    public void setIpHistory(List<String> ipHistory) { this.ipHistory = ipHistory; }
    public long getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(long registeredAt) { this.registeredAt = registeredAt; }

    public String getLastWorld() { return lastWorld; }
    public void setLastWorld(String lastWorld) { this.lastWorld = lastWorld; }
    public double getLastX() { return lastX; }
    public void setLastX(double lastX) { this.lastX = lastX; }
    public double getLastY() { return lastY; }
    public void setLastY(double lastY) { this.lastY = lastY; }
    public double getLastZ() { return lastZ; }
    public void setLastZ(double lastZ) { this.lastZ = lastZ; }
    public float getLastYaw() { return lastYaw; }
    public void setLastYaw(float lastYaw) { this.lastYaw = lastYaw; }
    public float getLastPitch() { return lastPitch; }
    public void setLastPitch(float lastPitch) { this.lastPitch = lastPitch; }

    public boolean verifyPassword(String password) {
        return PasswordHasher.verifyPassword(password, this.passwordHash, this.salt);
    }
}