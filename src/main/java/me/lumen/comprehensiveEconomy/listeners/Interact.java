package me.lumen.comprehensiveEconomy.listeners;

import me.lumen.comprehensiveEconomy.bounty.Bounty;
import me.lumen.comprehensiveEconomy.economy.Account;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jspecify.annotations.NonNull;

public class Interact implements Listener {
    @EventHandler
    public void onInteract(@NonNull PlayerInteractEvent e) {
        if (e.hasItem()) {
            assert e.getItem() != null;
            if (e.getItem().getType() == Material.PAPER) {
                Bounty.tryUseClaimItem(e);
                Account.tryCacheCheque(e);
            }
        }
    }
}
