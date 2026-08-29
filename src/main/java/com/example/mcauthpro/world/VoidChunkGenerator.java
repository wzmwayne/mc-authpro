package com.example.mcauthpro.world;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;

import java.util.Random;

public class VoidChunkGenerator extends ChunkGenerator {

    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int x, int z, ChunkData chunkData) {
    }

    @Override
    public boolean canSpawn(World world, int x, int z) {
        return true;
    }
}