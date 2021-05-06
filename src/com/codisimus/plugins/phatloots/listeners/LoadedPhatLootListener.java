package com.codisimus.plugins.phatloots.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootChest;
import com.codisimus.plugins.phatloots.PhatLoots;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class LoadedPhatLootListener implements Listener {
    private static final int DEFAULT_PARTICLES = 2;
    private final Map<World, WorldChunks> worldChunks = new HashMap<>();

    public LoadedPhatLootListener(PhatLoots plugin) {
        long delay = 4L; // TODO - configurable
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, delay, delay);
        plugin.getServer().getScheduler().runTaskLater(plugin, this::initialize, 5L); // initialize chunks
    }

    private void initialize() {
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                loadedChunk(chunk);
            }
        }
    }

    private void loadedChunk(Chunk chunk) {
        LoadedChunk loaded = new LoadedChunk(chunk);
        for (PhatLootChest chest : PhatLootChest.getChests()) {
            Block block = chest.getBlock();
            Location loc = block.getLocation();
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;
            if (chunkX == chunk.getX() && chunkZ == chunk.getZ() && chunk.getWorld() == loc.getWorld()) {
                Map<Particle, Integer> particles = new HashMap<>();
                for (PhatLoot loot : PhatLoots.getPhatLoots(block)) {
                    if (loot.particle != null) {
                        Integer count = particles.get(loot.particle);
                        if (count == null) {
                            count = 0;
                        }
                        count++;
                        particles.put(loot.particle, count);
                    }
                }
                if (!particles.isEmpty()) {
                    loaded.add(new PhatLootChestParticles(chest, particles));
                }
            }
        }
        if (!loaded.chests.isEmpty()) {
            World world = chunk.getWorld();
            WorldChunks chunks = worldChunks.get(world);
            if (chunks == null) {
                worldChunks.put(world, chunks = new WorldChunks());
            }
            chunks.chunks.put(chunk, loaded);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        loadedChunk(chunk);
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();
        WorldChunks chunks = worldChunks.get(world);
        if (chunks == null) {
            return;
        }
        chunks.chunks.remove(event.getChunk());
    }

    private void tick() {
        for (WorldChunks chunks : worldChunks.values()) {
            tickWorld(chunks);
        }
    }

    private void tickWorld(WorldChunks chunks) {
        for (LoadedChunk chunk : chunks.chunks.values()) {
            for (PhatLootChestParticles info : chunk.chests) {
                Location chestLoc = info.chest.getBlock().getLocation().add(0.5, 1.5, 0.5); // TODO -custom?
                for (Map.Entry<Particle, Integer> entry : info.particles.entrySet()) {
                    Particle particle = entry.getKey();
                    int multiplier = entry.getValue();
                    World world = chunk.chunk.getWorld();
                    world.spawnParticle(particle, chestLoc, DEFAULT_PARTICLES * multiplier);
                }
            }
        }
    }

    private class LoadedChunk {
        private final Chunk chunk;
        // this terribly designed piece of software doesn't allow hashable objects
        private final List<PhatLootChestParticles> chests = new ArrayList<>();

        public LoadedChunk(Chunk chunk) {
            this.chunk = chunk;
        }

        public void add(PhatLootChestParticles chest) {
            if (chest.chest.getBlock().getChunk().getX() != chunk.getX()
                    || chest.chest.getBlock().getChunk().getZ() != chunk.getZ()) {
                throw new IllegalArgumentException("Can only add chests in the same chunk");
            }
            chests.add(chest);
        }

    }

    private class PhatLootChestParticles {
        private final PhatLootChest chest;
        private final Map<Particle, Integer> particles;

        public PhatLootChestParticles(PhatLootChest chest, Map<Particle, Integer> particles) {
            this.chest = chest;
            this.particles = particles;
        }
    }

    private class WorldChunks {
        private final Map<Chunk, LoadedChunk> chunks = new HashMap<>();
    }

}
