package me.lumen.comprehensiveEconomy.hooks.ultimateteams;

import dev.xf3d3.ultimateteams.api.events.TeamTeleportEvent;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.homes.Home;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jspecify.annotations.NonNull;

public class TeamTeleportListener implements Listener {
    @EventHandler
    public void onTeamTeleport(@NonNull TeamTeleportEvent event){
        Player player = event.getPlayer();
        Account account = Account.getAccount(player);
        double cost = Home.calculateTeleportCost(player);
        if (account.withdrawAndSave(cost)){
            player.sendActionBar(Component.text("Charged " + CurrencyUtils.format(cost) + " for teleporting to team " + event.getTeam().getName() + "'s home").color(NamedTextColor.GREEN));
        } else {
            //if not enough money
            event.setCancelled(true);
            player.sendMessage(Component.text("You do not have enough " + CurrencyUtils.getCurrencyNamePlural() + " to use homes, including team ones!").color(NamedTextColor.RED));
            player.sendMessage(Component.text("You need " + CurrencyUtils.format(cost) + " to teleport, consider lowering the amount of personal homes you have to reduce the price - team homes do not increase the total price but still cost " + CurrencyUtils.getCurrencyNamePlural() + " to use").color(NamedTextColor.RED));
        }
    }
}
