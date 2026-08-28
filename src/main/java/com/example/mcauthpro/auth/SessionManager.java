package com.example.mcauthpro.auth;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private final Map<UUID, LoginSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public SessionManager() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 60, 60, TimeUnit.SECONDS);
    }

    public LoginSession createSession(Player player) {
        LoginSession session = new LoginSession(
            player.getUniqueId(),
            player.getName(),
            System.currentTimeMillis()
        );
        sessions.put(player.getUniqueId(), session);
        return session;
    }

    public LoginSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public boolean hasSession(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public boolean verifyToken(Player player, String token) {
        LoginSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            return false;
        }
        if (session.isTokenUsed()) {
            return false;
        }
        if (session.isExpired(300_000)) {
            sessions.remove(player.getUniqueId());
            return false;
        }
        session.markTokenUsed();
        return true;
    }

    public boolean isVerified(Player player) {
        LoginSession session = sessions.get(player.getUniqueId());
        return session != null && session.isVerified();
    }

    public void markVerified(Player player) {
        LoginSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.setVerified(true);
        }
    }

    public void removeSession(Player player) {
        sessions.remove(player.getUniqueId());
    }

    private void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired(300_000));
    }

    public void shutdown() {
        cleanupExecutor.shutdownNow();
    }

    public static class LoginSession {
        private final UUID playerUUID;
        private final String playerName;
        private final long createdAt;
        private String token;
        private boolean tokenUsed;
        private boolean verified;

        public LoginSession(UUID playerUUID, String playerName, long createdAt) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
            this.createdAt = System.currentTimeMillis();
            this.tokenUsed = false;
            this.verified = false;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public String getPlayerName() {
            return playerName;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public boolean isTokenUsed() {
            return tokenUsed;
        }

        public void markTokenUsed() {
            this.tokenUsed = true;
        }

        public boolean isVerified() {
            return verified;
        }

        public void setVerified(boolean verified) {
            this.verified = verified;
        }

        public boolean isExpired(long ttlMillis) {
            return (System.currentTimeMillis() - createdAt) > ttlMillis;
        }
    }
}