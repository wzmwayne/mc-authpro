package com.example.mcauthpro.world;

import com.example.mcauthpro.McAuthPro;
import com.example.mcauthpro.config.PluginConfig;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoginWorldManager {
    private final McAuthPro plugin;
    private final PluginConfig config;
    private World loginWorld;

    public LoginWorldManager(McAuthPro plugin) {
        this.plugin = plugin;
        this.config = plugin.getPluginConfig();
        if (config.isLoginWorldEnabled()) {
            initWorld();
        }
    }

    private void initWorld() {
        loginWorld = plugin.getServer().getWorld(config.getLoginWorldName());
        if (loginWorld == null) {
            createLoginWorld();
        } else {
            ensurePlatform();
        }
    }

    public void createLoginWorld() {
        String worldName = config.getLoginWorldName();

        WorldCreator creator = new WorldCreator(worldName);
        creator.environment(World.Environment.NORMAL);
        creator.generator(new VoidChunkGenerator());
        creator.generateStructures(false);
        creator.seed(0);

        loginWorld = plugin.getServer().createWorld(creator);
        if (loginWorld == null) {
            plugin.getLogger().severe("无法创建 login 世界！");
            return;
        }

        configureWorld();
        createPlatform();
        plugin.getLogger().info("login 世界已创建");
    }

    private void configureWorld() {
        loginWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        loginWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        loginWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        loginWorld.setGameRule(GameRule.DO_INSOMNIA, false);
        loginWorld.setGameRule(GameRule.DO_TILE_DROPS, false);
        loginWorld.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        loginWorld.setGameRule(GameRule.DO_FIRE_TICK, false);
        loginWorld.setGameRule(GameRule.DO_VINES_SPREAD, false);
        loginWorld.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        loginWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        loginWorld.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        loginWorld.setTime(6000);
        loginWorld.setStorm(false);
        loginWorld.setThundering(false);
        loginWorld.setSpawnLocation(0, config.getLoginWorldPlatformY() + 1, 0);
    }

    private void createPlatform() {
        int platformY = config.getLoginWorldPlatformY();
        int size = config.getLoginWorldPlatformSize();
        int wallHeight = config.getLoginWorldWallHeight();

        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                new Location(loginWorld, x, platformY, z).getBlock().setType(Material.BARRIER);
            }
        }

        int wallSize = size + 1;
        for (int x = -wallSize; x <= wallSize; x++) {
            for (int z = -wallSize; z <= wallSize; z++) {
                if (Math.abs(x) == wallSize || Math.abs(z) == wallSize) {
                    for (int y = platformY + 1; y <= platformY + wallHeight; y++) {
                        new Location(loginWorld, x, y, z).getBlock().setType(Material.BARRIER);
                    }
                }
            }
        }
    }

    private void ensurePlatform() {
        int platformY = config.getLoginWorldPlatformY();
        Location check = new Location(loginWorld, 0, platformY, 0);
        if (check.getBlock().getType() != Material.BARRIER) {
            createPlatform();
            configureWorld();
        }
    }

    public void rebuildWorld() {
        List<Player> displacedPlayers = new ArrayList<>();
        for (Player player : loginWorld.getPlayers()) {
            World mainWorld = plugin.getServer().getWorlds().get(0);
            player.teleport(mainWorld.getSpawnLocation());
            displacedPlayers.add(player);
        }

        plugin.getServer().unloadWorld(loginWorld, false);

        deleteWorldFiles(config.getLoginWorldName());

        loginWorld = null;
        createLoginWorld();

        for (Player player : displacedPlayers) {
            if (player.isOnline()) {
                player.sendMessage("§e登录世界已重构，请重新加入验证。");
            }
        }
    }

    private void deleteWorldFiles(String worldName) {
        File worldFolder = new File(plugin.getServer().getWorldContainer(), worldName);
        if (worldFolder.exists()) {
            deleteFolder(worldFolder);
        }
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

    public World getLoginWorld() {
        return loginWorld;
    }

    public Location getSpawnLocation() {
        if (loginWorld == null) return null;
        return new Location(loginWorld, 0, config.getLoginWorldPlatformY() + 1, 0);
    }
}