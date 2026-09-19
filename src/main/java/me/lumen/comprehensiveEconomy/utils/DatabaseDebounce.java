package me.lumen.comprehensiveEconomy.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.UUID;

/**
 * Utility methods for setting a players debounce
 * Note this is likely local to each plugin instance
 */
public class DatabaseDebounce {
    private static final HashMap<UUID, Long> debounces = new HashMap<>();

    /**
     * Update a players debounce
     * @param player the player to set
     * @param delay how long it is until the debounce is over (in milliseconds)
     */
    public static void update(@NonNull Player player, long delay) {
        debounces.put(player.getUniqueId(), System.currentTimeMillis() + delay);
    }

    /**
     * Remove a player from the cached debounces
     * @param player the player to remove
     */
    public static void remove(@NonNull Player player) {
        debounces.remove(player.getUniqueId());
    }

    public static boolean blocked(@NonNull Player player){
        if (!debounces.containsKey(player.getUniqueId())) {
            return false;
        }
        //allow if it is after the players next use time
        return System.currentTimeMillis() <= debounces.get(player.getUniqueId());
    }

    public static @NonNull Component getTooFastMessage(@NonNull Player player){
        long wait = debounces.get(player.getUniqueId()) - System.currentTimeMillis();
        return Component.text("Too fast, please wait " + wait + "ms").color(NamedTextColor.RED);
    }

    public static @NonNull String getTooFastMessageString(@NonNull Player player){
        long wait = debounces.get(player.getUniqueId()) - System.currentTimeMillis();
        return "Too fast, please wait " + wait + "ms";
    }
}
