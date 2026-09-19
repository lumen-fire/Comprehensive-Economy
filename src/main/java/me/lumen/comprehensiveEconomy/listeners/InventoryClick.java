package me.lumen.comprehensiveEconomy.listeners;

import me.lumen.comprehensiveEconomy.menu.BalTopGUI;
import me.lumen.comprehensiveEconomy.menu.SellGUI;
import me.lumen.comprehensiveEconomy.menu.bounty.BountyInventoryGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NonNull;

public class InventoryClick implements Listener {
    @EventHandler
    public void onInventoryClick(@NonNull InventoryClickEvent event) {
        Inventory inventory =  event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        if (holder instanceof SellGUI sellGUI){
            event.setCancelled(true);
            sellGUI.click(event);
        } else if (holder instanceof BalTopGUI){
            event.setCancelled(true);
        } else if (holder instanceof BountyInventoryGUI bountyGUI){
            event.setCancelled(true);
            bountyGUI.click(event);
        }
    }
}
