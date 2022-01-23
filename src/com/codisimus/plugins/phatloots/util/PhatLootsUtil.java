package com.codisimus.plugins.phatloots.util;

import java.io.FilenameFilter;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.codisimus.plugins.phatloots.PhatLoot;
import com.codisimus.plugins.phatloots.PhatLoots;
import com.codisimus.plugins.phatloots.PhatLootsConfig;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Utility Variables/Methods commonly used by PhatLoots and its add-ons
 *
 * @author Codisimus
 */
public class PhatLootsUtil {
    private static Random random = ThreadLocalRandom.current();
    public static final String PROPERTIES_EXTENSION = ".properties";
    public static final String TEXT_EXTENSION = ".txt";
    public static final String YAML_EXTENSION = ".yml";
    public static final FilenameFilter YAML_FILTER = (dir, name) -> name.toLowerCase().endsWith(YAML_EXTENSION);

    /**
     * Returns true if the given player is allowed to loot the specified PhatLoot
     *
     * @param player The Player who is being checked for permission
     * @param phatLoot The PhatLoot in question
     * @return true if the player is allowed to loot the PhatLoot
     */
    public static boolean canLoot(Player player, PhatLoot phatLoot) {
        //Check if the PhatLoot is restricted
        if (PhatLootsConfig.restrictAll || PhatLootsConfig.restricted.contains(phatLoot.name)) {
            //Check for the loot all permission
            return player.hasPermission("phatloots.loot.*") || player.hasPermission("phatloots.loot." + phatLoot.name); //Check if the Player has the specific loot permission
        } else {
            return true;
        }
    }

    /**
     * Returns true if the given Block is a type that is able to be linked
     *
     * @param block the given Block
     * @return true if the given Block is able to be linked
     */
    public static boolean isLinkableType(Block block) {
        return PhatLoots.types.containsKey(block.getType());
    }

    /**
     * Returns a random int between 0 (inclusive) and y (inclusive)
     *
     * @param upper y
     * @return a random int between 0 and y
     */
    public static int rollForInt(int upper) {
        return random.nextInt(upper + 1); //+1 is needed to make it inclusive
    }

    /**
     * Returns a random int between x (inclusive) and y (inclusive)
     *
     * @param lower x
     * @param upper y
     * @return a random int between x and y
     */
    public static int rollForInt(int lower, int upper) {
        return random.nextInt(upper + 1 - lower) + lower;
    }

    /**
     * Returns a random double between 0 (inclusive) and y (exclusive)
     *
     * @param upper y
     * @return a random double between 0 and y
     */
    public static double rollForDouble(double upper) {
        return random.nextDouble() * upper;
    }

    /**
     * Returns a random double between x (inclusive) and y (exclusive)
     *
     * @param lower x
     * @param upper y
     * @return a random double between x and y
     */
    public static double rollForDouble(int lower, int upper) {
        return random.nextInt(upper + 1 - lower) + lower;
    }

    /**
     * Returns a user friendly String of the given ItemStack's name
     *
     * @param item The given ItemStack
     * @return The name of the item
     */
    public static String getItemName(ItemStack item) {
        //Return the Display name of the item if there is one
        if (item.hasItemMeta()) {
            String name = item.getItemMeta().getDisplayName();
            if (name != null && !name.isEmpty()) {
                return name;
            }
        }
        //A display name was not found so use a cleaned up version of the Material name
        return WordUtils.capitalizeFully(item.getType().toString().replace("_", " "));
    }

    /**
     * Returns a user friendly String of the given Block's name
     *
     * @return The name of the item
     */
    public static String getBlockName(Block block) {
        //Return the Display name of the item if there is one
        //A display name was not found so use a cleaned up version of the Material name
        return WordUtils.capitalizeFully(block.getType().toString().replace("_", " "));
    }

    /**
     * Returns the left side of a Chest block
     *
     * @param block The Block which may or may not be a Chest
     * @return The left side of the chest if applicable
     */
    public static Block getLeftSide(Block block) {
        switch (block.getType()) {
            case TRAPPED_CHEST, CHEST -> {
                Chest chest = (Chest) block.getState();
                Inventory inventory = chest.getInventory();
                //We only care about the left side because that is the Block that would be linked
                if (inventory instanceof DoubleChestInventory doubleChestInventory) {
                    chest = (Chest) doubleChestInventory.getLeftSide().getHolder();
                    block = chest.getBlock();
                }
            }
            default -> {
            }
        }
        return block;
    }

    /**
     * Returns the Player that is closest to the given Location.
     * Returns null if no Players are within 50 Blocks
     *
     * @param location The given Location
     * @return the closest Player
     */
    public static Player getNearestPlayer(Location location) {
        Player nearestPlayer = null;
        double shortestDistance = 2500;
        for (Player player: location.getWorld().getPlayers()) {
            Location playerLocation = player.getLocation();
            //Use the squared distance because is it much less resource intensive
            double distanceToPlayer = location.distanceSquared(playerLocation);
            if (distanceToPlayer < shortestDistance) {
                nearestPlayer = player;
                shortestDistance = distanceToPlayer;
            }
        }
        return nearestPlayer;
    }

    /**
     * Returns a LinkedList of PhatLoots that are linked to the target Block
     *
     * @param player The Player targeting a Block
     * @return The LinkedList of PhatLoots
     */
    public static LinkedList<PhatLoot> getPhatLoots(Player player) {
        LinkedList<PhatLoot> phatLoots = new LinkedList<>();
        //Cancel if the sender is not targeting a correct Block
        Block block = player.getTargetBlock(EnumSet.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR), 10);
        String blockName = PhatLootsUtil.getBlockName(block);
        if (!PhatLootsUtil.isLinkableType(block)) {
            player.sendMessage("§6" + blockName + "§4 is not a linkable type.");
            return phatLoots;
        }

        phatLoots = PhatLoots.getPhatLoots(block);

        //Inform the sender if the Block is not linked to any PhatLoots
        if (phatLoots.isEmpty()) {
            player.sendMessage("§4Target §6" + blockName + "§4 is not linked to a PhatLoot");
        }
        return phatLoots;
    }

    /**
     * Concats arguments together to create a sentence from words.
     *
     * @param args the arguments to concat
     * @return The new String that was created
     */
    public static String concatArgs(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= args.length - 1; i++) {
            sb.append(" ");
            sb.append(args[i]);
        }
        return sb.substring(1);
    }

    /**
     * Returns a human friendly String of the remaining time until the PhatLootChest resets
     *
     * @param time The given time
     * @return the remaining time until the PhatLootChest resets
     */
    public static String timeToString(long time) {
        if (time < 0) {
            return "forever";
        }

        //Find the appropriate unit of time and return that amount
        if (time > DateUtils.MILLIS_PER_DAY) {
            return time / DateUtils.MILLIS_PER_DAY + " day(s)";
        } else if (time > DateUtils.MILLIS_PER_HOUR) {
            return time / DateUtils.MILLIS_PER_HOUR + " hour(s)";
        } else if (time > DateUtils.MILLIS_PER_MINUTE) {
            return time / DateUtils.MILLIS_PER_MINUTE + " minute(s)";
        } else if (time > DateUtils.MILLIS_PER_SECOND) {
            return time / DateUtils.MILLIS_PER_SECOND + " second(s)";
        } else {
            return time + " millisecond(s)";
        }
    }

    /**
     * Gets the experience at a certain level.
     *
     * @param level The level to retrieve the experience from
     * @return The experience to level up at that level
     */
    public static int getExpAtLevel(int level) {
        if (level <= 15) {
            return (2 * level) + 7;
        }
        if (level <= 30) {
            return (5 * level) - 38;
        }
        return (9 * level) - 158;
    }

    /**
     * Never use player.getTotalExperience(), use this method instead.
     * player.getTotalExperience() shows XP that has been spent on enchants.
     *
     * player.getExp() is the percentage toward the next level (between 0 & 1).
     *
     * @param player The player to get the total experience of
     * @return The total experience the player has
     */
    public static int getTotalExperience(final Player player) {
        int exp = Math.round(getExpAtLevel(player.getLevel()) * player.getExp());
        int currentLevel = player.getLevel();

        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    /**
     * Checks if the specified version is compatible with the
     * given version.
     *
     * @param version the version to check
     * @param whichVersion the version to check if compatbile with
     * @return if the specified version is compatible with the given version
     */
    public static boolean isCompatible(String version, String whichVersion) {
        int[] currentVersion = parseVersion(version);
        int[] otherVersion = parseVersion(whichVersion);
        int length = Math.max(currentVersion.length, otherVersion.length);
        for (int index = 0; index < length; index = index + 1) {
            int self = (index < currentVersion.length) ? currentVersion[index] : 0;
            int other = (index < otherVersion.length) ? otherVersion[index] : 0;

            if (self != other) {
                return (self - other) > 0;
            }
        }
        return true;
    }

    private static int[] parseVersion(String versionParam) {
        versionParam = (versionParam == null) ? "" : versionParam;
        if (versionParam.contains("(MC: ")) {
            versionParam = versionParam.split("\\(MC: ")[1];
            versionParam = versionParam.split("\\)")[0];
        }
        String[] stringArray = versionParam.split("[_.-]");
        int[] temp = new int[stringArray.length];
        for (int index = 0; index <= (stringArray.length - 1); index = index + 1) {
            String t = stringArray[index].replaceAll("\\D", "");
            try {
                temp[index] = Integer.parseInt(t);
            } catch(NumberFormatException ex) {
                temp[index] = 0;
            }
        }
        return temp;
    }
}
