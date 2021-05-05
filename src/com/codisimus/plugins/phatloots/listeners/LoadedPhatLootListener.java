package com.codisimus.plugins.phatloots.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootChest;
import com.codisimus.plugins.phatloots.PhatLoots;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class LoadedPhatLootListener implements Listener {
    private static final int DEFAULT_PARTICLES = 2;
    private final Map<Chunk, LoadedChunk> loadedChunks = new HashMap<>();

    public LoadedPhatLootListener(PhatLoots plugin) {
        long delay = 4L; // TODO - configurable
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, delay, delay);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        LoadedChunk loaded = new LoadedChunk(chunk);
        for (PhatLootChest chest : PhatLootChest.getChests()) {
            Block block = chest.getBlock();
            Location loc = block.getLocation();
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;
            if (chunkX == chunk.getX() && chunkZ == chunk.getZ()) {
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
            loadedChunks.put(chunk, loaded);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        loadedChunks.remove(event.getChunk());
    }

    private void tick() {
        for (LoadedChunk chunk : loadedChunks.values()) {
            for (PhatLootChestParticles info : chunk.chests) {
                Location chestLoc = info.chest.getBlock().getLocation();
                for (Map.Entry<Particle, Integer> entry : info.particles.entrySet()) {
                    Particle particle = entry.getKey();
                    int multiplier = entry.getValue();
                    for (Player player : chunk.chunk.getWorld().getPlayers()) {
                        player.spawnParticle(particle, chestLoc, DEFAULT_PARTICLES * multiplier);
                    }
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
            if (chest.chest.getBlock().getChunk() != chunk) {
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

}
