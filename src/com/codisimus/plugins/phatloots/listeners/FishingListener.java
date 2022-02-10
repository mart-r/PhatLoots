package com.codisimus.plugins.phatloots.listeners;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.loot.CommandLoot;
import com.codisimus.plugins.phatloots.loot.LootBundle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Listens for Fishing events and handles what is caught
 *
 * @author Codisimus
 */
public class FishingListener implements Listener {

    @EventHandler (ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        //Check if there is a PhatLoot for Fishing
        PhatLoot phatLoot = PhatLoots.getPhatLoot("Fishing");
        if (PhatLoots.plugin.getConfig().getBoolean("RegionFishingDropLoot", true)
                && MobListener.regionHook != null) {

            List<String> regionNames = MobListener.regionHook.getRegionNames(event.getPlayer().getLocation());
            for (String regionName : regionNames) {
                phatLoot = PhatLoots.getPhatLoot("Fishing@" + regionName);
                if (phatLoot != null)
                    break;
            }
        }

        if (phatLoot == null)
            return;

        //Check if something has been caught
        if (event.getCaught() instanceof Item) {
            //Get the looting bonus of the fishing pole
            ItemStack pole = event.getPlayer().getInventory().getItemInMainHand();
            double lootingBonus = PhatLoot.lootingBonusPerLvl * pole.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);

            //Change the 'fish' to which ever Item has been looted
            Item fish = (Item) event.getCaught();
            LootBundle lootBundle = phatLoot.rollForLoot(lootingBonus);
            fish.setItemStack(lootBundle.getItemList().get(0));

            //Execute commands that were rolled for
            for (CommandLoot command : lootBundle.getCommandList()) {
                command.execute(event.getPlayer());
            }

            //Set experience to be gained by the fisher
            event.setExpToDrop(lootBundle.getExp());
        }
    }
}
