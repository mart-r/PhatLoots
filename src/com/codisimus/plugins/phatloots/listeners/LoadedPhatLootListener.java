package com.codisimus.plugins.phatloots.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLootChest;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsConfig;

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
        long delay = PhatLootsConfig.particleDelayTicks;
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

    public void addChest(PhatLootChest chest) {
        PhatLoots.plugin.getServer().getScheduler().runTask(PhatLoots.plugin, () -> addChestInternal(chest));
    }

    private void addChestInternal(PhatLootChest chest) {
        Block block = chest.getBlock();
        PhatLootChestParticles particles = loadedChest(chest);
        if (particles != null) {
            Chunk chunk = block.getChunk();
            WorldChunks wc = worldChunks.get(block.getWorld());
            if (wc == null) {
                worldChunks.put(block.getWorld(), wc = new WorldChunks());
            }
            LoadedChunk lc = wc.chunks.get(chunk);
            if (lc == null) {
                wc.chunks.put(chunk, lc = new LoadedChunk(chunk));
            }
            lc.add(particles);
        }
    }

    private PhatLootChestParticles loadedChest(PhatLootChest chest) {
        Map<Particle, Integer> particles = new HashMap<>();
        for (PhatLoot loot : PhatLoots.getPhatLoots(chest.getBlock())) {
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
            return new PhatLootChestParticles(chest, particles);
        } else {
            return null;
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
                PhatLootChestParticles particles = loadedChest(chest);
                if (particles != null) {
                    loaded.add(particles);
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

    public void removeChest(PhatLootChest chest) {
        Block block = chest.getBlock();
        WorldChunks wc = worldChunks.get(block.getWorld());
        if (wc == null) {
            return;
        }
        LoadedChunk loaded = wc.chunks.get(block.getChunk());
        if (loaded == null) {
            return;
        }
        loaded.remove(chest);
    }

    private void tick() {
        for (WorldChunks chunks : worldChunks.values()) {
            tickWorld(chunks);
        }
    }

    private void tickWorld(WorldChunks chunks) {
        double offset = PhatLootsConfig.particleOffset;
        double addY = PhatLootsConfig.particleHeightAdd;
        double extra = PhatLootsConfig.particleExtra;
        for (LoadedChunk chunk : chunks.chunks.values()) {
            for (PhatLootChestParticles info : chunk.chests) {
                if (info.chest.getBlock().getType().isAir()) {
                    continue; // ignore broken chests
                }
                Location chestLoc = info.chest.getBlock().getLocation().add(0.5, 0.5 + addY, 0.5);
                double x = chestLoc.getX();
                double y = chestLoc.getY();
                double z = chestLoc.getZ();
                for (Map.Entry<Particle, Integer> entry : info.particles.entrySet()) {
                    Particle particle = entry.getKey();
                    int multiplier = entry.getValue();
                    World world = chunk.chunk.getWorld();
                    int count = DEFAULT_PARTICLES * multiplier;
                    // Particle particle double x, double y, double z, int count, double offsetX,
                    // double offsetY, double offsetZ, double extra, T data, boolean force
                    world.spawnParticle(particle, x, y, z, count, offset, offset, offset, extra, null, false);
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

        public void remove(PhatLootChest chest) {
            chests.removeIf((ccp) -> ccp.chest == chest);
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
