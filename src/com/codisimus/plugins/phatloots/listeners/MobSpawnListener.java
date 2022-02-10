package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Listens for Mob spawning to equip them with armor and a weapon
 *
 * @author Codisimus
 */
public class MobSpawnListener extends MobListener {
    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMobSpawn(final CreatureSpawnEvent event) {
        //Schedule this code so that mob spawning plugins have a chance to change the type of the mob
        Bukkit.getScheduler().runTaskLater(PhatLoots.plugin, () -> {
            LivingEntity entity = event.getEntity();
            PhatLoot phatLoot = getPhatLoot(entity);
            if (phatLoot != null) {
                //The mob's 'level' gives them a looting bonus to get better equipment
                double level = entity.hasMetadata("level")
                        ? entity.getMetadata("level").get(0).asDouble()
                        : 0;
                phatLoot.rollForEquipment(entity, level);
            }
        }, 1);
    }

    @Override
    String getLootType() {
        return "Spawn";
    }
}
