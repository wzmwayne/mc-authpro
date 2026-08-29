package com.example.mcauthpro.auth;

import com.example.mcauthpro.McAuthPro;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private final McAuthPro plugin;
    private final Map<UUID, LoginSession> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> countdownTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public SessionManager(McAuthPro plugin) {
        this.plugin = plugin;
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 60, 60, TimeUnit.SECONDS);
    }

    public LoginSession createSession(Player player) {
        LoginSession session = new LoginSession(player.getUniqueId(), player.getName());
        sessions.put(player.getUniqueId(), session);
        return session;
    }

    public LoginSession getSession(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public LoginSession getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    public boolean hasSession(Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    public boolean isTurnstileVerified(Player player) {
        LoginSession session = sessions.get(player.getUniqueId());
        return session != null && session.isTurnstileVerified();
    }

    public boolean isLoginVerified(Player player) {
        LoginSession session = sessions.get(player.getUniqueId());
        return session != null && session.isLoginVerified();
    }

    public boolean isFullyVerified(Player player) {
        LoginSession session = sessions.get(player.getUniqueId());
        return session != null && session.isTurnstileVerified() && session.isLoginVerified();
    }

    public void markTurnstileVerified(Player player) {
        LoginSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.setTurnstileVerified(true);
        }
    }

    public void markLoginVerified(Player player) {
        LoginSession session = sessions.get(player.getUniqueId());
        if (session != null) {
            session.setLoginVerified(true);
        }
    }

    public void startCountdown(Player player, int seconds) {
        UUID uuid = player.getUniqueId();
        stopCountdown(player);

        ScheduledFuture<?> task = cleanupExecutor.scheduleAtFixedRate(() -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                LoginSession session = sessions.get(uuid);
                if (session == null || session.isTurnstileVerified()) {
                    stopCountdown(player);
                    return;
                }

                int remaining = session.decrementCountdown();
                if (remaining <= 0) {
                    stopCountdown(player);
                    if (player.isOnline()) {
                        player.kickPlayer("§c人机验证超时，请重新加入");
                    }
                    sessions.remove(uuid);
                    return;
                }

                if (player.isOnline()) {
                    player.sendTitle("§e人机验证", "§f请在 §c" + remaining + " §f秒内完成验证", 0, 25, 5);
                }
            });
        }, 1, 1, TimeUnit.SECONDS);

        countdownTasks.put(uuid, task);
    }

    public void stopCountdown(Player player) {
        ScheduledFuture<?> task = countdownTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel(false);
        }
    }

    public void removeSession(Player player) {
        stopCountdown(player);
        sessions.remove(player.getUniqueId());
    }

    private void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> {
            LoginSession session = entry.getValue();
            if (session.isExpired(300_000)) {
                countdownTasks.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    public void shutdown() {
        cleanupExecutor.shutdownNow();
        countdownTasks.values().forEach(task -> task.cancel(false));
        countdownTasks.clear();
    }

    public static class LoginSession {
        private final UUID playerUUID;
        private final String playerName;
        private final long createdAt;
        private boolean turnstileVerified;
        private boolean loginVerified;
        private int countdownSeconds;

        public LoginSession(UUID playerUUID, String playerName) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
            this.createdAt = System.currentTimeMillis();
            this.turnstileVerified = false;
            this.loginVerified = false;
            this.countdownSeconds = 30;
        }

        public UUID getPlayerUUID() { return playerUUID; }
        public String getPlayerName() { return playerName; }
        public long getCreatedAt() { return createdAt; }

        public boolean isTurnstileVerified() { return turnstileVerified; }
        public void setTurnstileVerified(boolean verified) { this.turnstileVerified = verified; }

        public boolean isLoginVerified() { return loginVerified; }
        public void setLoginVerified(boolean verified) { this.loginVerified = verified; }

        public int getCountdownSeconds() { return countdownSeconds; }
        public void setCountdownSeconds(int seconds) { this.countdownSeconds = seconds; }
        public int decrementCountdown() { return --countdownSeconds; }

        public boolean isExpired(long ttlMillis) {
            return (System.currentTimeMillis() - createdAt) > ttlMillis;
        }
    }
}