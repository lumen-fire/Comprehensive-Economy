package me.lumen.comprehensiveEconomy.hooks.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lumen.comprehensiveEconomy.bounty.Bounty;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.homes.Home;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class PAPIExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "comprehensive-economy";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Lumen-Fire";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(final Player player, @NotNull final String params) {
        //economy placeholders
        if (params.startsWith("economy")){
            Account account = Account.getAccount(player);
            if (params.equalsIgnoreCase("economy_balance_formatted")){
                return CurrencyUtils.format(account.getBalance());
            }
            if (params.equalsIgnoreCase("economy_balance")){
                return Double.toString(CurrencyUtils.round(account.getBalance()));
            }
        }
        //homes placeholders
        if (params.equalsIgnoreCase("homes_count")) {
            return Integer.toString(Home.getHomesCount(player));
        }
        if (params.equalsIgnoreCase("homes_teleport_cost")) {
            return Double.toString(Home.calculateTeleportCost(player));
        }
        if (params.equalsIgnoreCase("homes_teleport_cost_formatted")) {
            return CurrencyUtils.format(Home.calculateTeleportCost(player));
        }
        if (params.equalsIgnoreCase("homes_set_cost")) {
            return Double.toString(Home.calculateSetCost(player));
        }
        if (params.equalsIgnoreCase("homes_set_cost_formatted")) {
            return CurrencyUtils.format(Home.calculateSetCost(player));
        }
        //bounty placeholders
        if (params.equalsIgnoreCase("bounty_status_bracketed")){
            Bounty bounty = Bounty.getBounty(player);
            if (bounty == null) return "";
            return "[Wanted: " + CurrencyUtils.format(bounty.getTotal()) + "]";
        }
        if (params.equalsIgnoreCase("bounty_amount_formatted")){
            Bounty bounty = Bounty.getBounty(player);
            if (bounty == null) return CurrencyUtils.format(0);
            return CurrencyUtils.format(bounty.getTotal());
        }
        if (params.equalsIgnoreCase("bounty_amount")){
            Bounty bounty = Bounty.getBounty(player);
            if (bounty == null) return "0";
            return Double.toString(CurrencyUtils.round(bounty.getTotal()));
        }

        return null;
    }

    @Override
    public @NonNull List<String> getPlaceholders(){
        return List.of(
                "%comprehensive-economy_economy_balance%",
                "%comprehensive-economy_economy_balance_formatted%",
                "%comprehensive-economy_homes_count%",
                "%comprehensive-economy_homes_teleport_cost%",
                "%comprehensive-economy_homes_teleport_cost_formatted%",
                "%comprehensive-economy_homes_set_cost%",
                "%comprehensive-economy_homes_set_cost_formatted%",
                "%comprehensive-economy_bounty_status_bracketed%",
                "%comprehensive-economy_bounty_amount%",
                "%comprehensive-economy_bounty_amount_formatted%"
        );
    }


}
