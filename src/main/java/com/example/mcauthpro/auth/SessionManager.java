package com.example.mcauthpro.auth;

import com.example.mcauthpro.McAuthPro;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SessionManager {
    private final McAuthPro plugin;
    private final Set<String> turnstileVerified = ConcurrentHashMap.newKeySet();
    private final Set<String> loginVerified = ConcurrentHashMap.newKeySet();
    private final Map<String, Integer> countdowns = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> countdownTasks = new ConcurrentHashMap<>();
    private final Map<String, String> pendingKeys = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

    public SessionManager(McAuthPro plugin) {
        this.plugin = plugin;
    }

    public String createVerificationKey(Player player) {
        String key;
        do {
            long value = ThreadLocalRandom.current().nextLong(0x100000000L);
            key = String.format("%08x", value);
        } while (pendingKeys.containsKey(key));
        pendingKeys.put(key, player.getName());
        return key;
    }

    public String getPlayerNameByKey(String key) {
        return pendingKeys.get(key);
    }

    public void removeKey(String key) {
        pendingKeys.remove(key);
    }

    public void removeKeysForPlayer(Player player) {
        String name = player.getName();
        pendingKeys.entrySet().removeIf(entry -> entry.getValue().equals(name));
    }

    public boolean isTurnstileVerified(Player player) {
        return turnstileVerified.contains(player.getName());
    }

    public boolean isLoginVerified(Player player) {
        return loginVerified.contains(player.getName());
    }

    public void markTurnstileVerified(Player player) {
        turnstileVerified.add(player.getName());
    }

    public void markLoginVerified(Player player) {
        loginVerified.add(player.getName());
    }

    public void startCountdown(Player player, int seconds) {
        String name = player.getName();
        stopCountdown(player);
        countdowns.put(name, seconds);

        ScheduledFuture<?> task = cleanupExecutor.scheduleAtFixedRate(() -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (turnstileVerified.contains(name)) {
                    stopCountdown(player);
                    countdowns.remove(name);
                    return;
                }

                Integer remaining = countdowns.get(name);
                if (remaining == null) {
                    stopCountdown(player);
                    return;
                }

                remaining--;
                if (remaining <= 0) {
                    stopCountdown(player);
                    countdowns.remove(name);
                    removeKeysForPlayer(plugin.getServer().getPlayerExact(name));
                    if (player.isOnline()) {
                        player.kickPlayer("§c人机验证超时，请重新加入");
                    }
                    return;
                }

                countdowns.put(name, remaining);
                if (player.isOnline()) {
                    player.sendTitle("§e人机验证", "§f请在 §c" + remaining + " §f秒内完成验证", 0, 25, 5);
                }
            });
        }, 1, 1, TimeUnit.SECONDS);

        countdownTasks.put(name, task);
    }

    public void stopCountdown(Player player) {
        String name = player.getName();
        ScheduledFuture<?> task = countdownTasks.remove(name);
        if (task != null) {
            task.cancel(false);
        }
    }

    public void remove(Player player) {
        String name = player.getName();
        stopCountdown(player);
        turnstileVerified.remove(name);
        loginVerified.remove(name);
        countdowns.remove(name);
        removeKeysForPlayer(player);
    }

    public int getTurnstileVerifiedCount() {
        return turnstileVerified.size();
    }

    public int getLoginVerifiedCount() {
        return loginVerified.size();
    }

    public void shutdown() {
        cleanupExecutor.shutdownNow();
        countdownTasks.values().forEach(task -> task.cancel(false));
        countdownTasks.clear();
        turnstileVerified.clear();
        loginVerified.clear();
        countdowns.clear();
        pendingKeys.clear();
    }
}