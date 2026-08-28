package com.example.mcauthpro.network;

import com.example.mcauthpro.config.PluginConfig;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class RealIpResolver {
    private final PluginConfig config;

    public RealIpResolver(PluginConfig config) {
        this.config = config;
    }

    public String getRealIp(Player player) {
        if (!config.isTrustProxy()) {
            return player.getAddress().getAddress().getHostAddress();
        }
        List<String> ips = getAllIps(player);
        return ips.isEmpty() ? player.getAddress().getAddress().getHostAddress() : ips.get(0);
    }

    public List<String> getAllIps(Player player) {
        List<String> ips = new ArrayList<>();
        if (config.isBungeeGuard()) {
            String bungeeGuardIp = player.getAddress().getAddress().getHostAddress();
            if (!isInternalIp(bungeeGuardIp)) {
                ips.add(bungeeGuardIp);
            }
        }
        String cfIp = player.getAddress().getAddress().getHostAddress();
        if (!cfIp.equals(player.getAddress().getAddress().getHostAddress()) && !isInternalIp(cfIp)) {
            ips.add(cfIp);
        }
        String forwarded = player.getAddress().getAddress().getHostAddress();
        if (!forwarded.equals(player.getAddress().getAddress().getHostAddress()) && !isInternalIp(forwarded)) {
            ips.add(forwarded);
        }
        String socketIp = player.getAddress().getAddress().getHostAddress();
        if (!isInternalIp(socketIp)) {
            ips.add(socketIp);
        }
        if (ips.isEmpty()) {
            ips.add(player.getAddress().getAddress().getHostAddress());
        }
        return ips.subList(0, Math.min(ips.size(), config.getMaxIpsToRecord()));
    }

    private boolean isInternalIp(String ip) {
        return ip.startsWith("10.") ||
               ip.startsWith("192.168.") ||
               ip.startsWith("172.") ||
               ip.equals("127.0.0.1") ||
               ip.equals("0:0:0:0:0:0:0:1");
    }
}