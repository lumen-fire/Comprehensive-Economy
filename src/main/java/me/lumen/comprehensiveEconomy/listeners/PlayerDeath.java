package me.lumen.comprehensiveEconomy.listeners;

import me.lumen.comprehensiveEconomy.bounty.Bounty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jspecify.annotations.NonNull;

public class PlayerDeath implements Listener {
    @EventHandler
    public void onPlayerDeath(@NonNull PlayerDeathEvent event){
        Player player = event.getPlayer();
        //noinspection UnstableApiUsage
        if (event.getDamageSource().getCausingEntity() instanceof Player killer && killer != player){
            Bounty bounty = Bounty.getBounty(player);
            if (bounty != null){
                bounty.spawnClaimItem(player.getLocation(), killer);
            }
        }
    }
}
