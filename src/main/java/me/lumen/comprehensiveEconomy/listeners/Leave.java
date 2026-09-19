package me.lumen.comprehensiveEconomy.listeners;

import me.lumen.comprehensiveEconomy.bounty.Bounty;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.homes.Home;
import me.lumen.comprehensiveEconomy.utils.DatabaseDebounce;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NonNull;

public class Leave implements Listener {
    @EventHandler
    public void onLeave(@NonNull PlayerQuitEvent e){
        Player player = e.getPlayer();
        //can't be bothered fixing inconsistent capitalization of these methods
        Account.unCache(player);
        Home.uncache(player);
        Bounty.uncache(player);
        DatabaseDebounce.remove(player);
    }
}
