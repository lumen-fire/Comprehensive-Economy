package me.lumen.comprehensiveEconomy.hooks.vault;

import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultEconomy implements Economy {
    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "ifrax-economy";
    }

    /**For now, I don't need bank support, so it is disabled, as I don't think any plugins hooking in use it, even the teams plugin for its team banks*/
    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return CurrencyUtils.decimalPlaces();
    }

    @Override
    public String format(double amount) {
        return CurrencyUtils.format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return CurrencyUtils.getCurrencyNamePlural();
    }

    @Override
    public String currencyNameSingular() {
        return CurrencyUtils.getName();
    }
    /*
    * Has account methods
    * We can just treat it like they always have an account, as it will be created automattically if we try and get one
    */
        @Override
        public boolean hasAccount(String playerName) {
            return true;
        }

        @Override
        public boolean hasAccount(OfflinePlayer player) {
            return true;
        }

        @Override
        public boolean hasAccount(String playerName, String worldName) {
            return true;
        }

        @Override
        public boolean hasAccount(OfflinePlayer player, String worldName) {
            return true;
        }
    /*
    * Get balance methods
    */
        @Override
        public double getBalance(String playerName) {
            return Account.VaultUtils.getForName(playerName, Account::getBalance);
        }

        @Override
        public double getBalance(OfflinePlayer player) {
            return Account.VaultUtils.getForPotentiallyOffline(player, Account::getBalance);
        }
        /**I don't need, nor care for per world balances, so just returning normal*/
        @Override
        public double getBalance(String playerName, String world) {
            return getBalance(playerName);
        }
        /**I don't need, nor care for per world balances, so just returning normal*/
        @Override
        public double getBalance(OfflinePlayer player, String world) {
            return getBalance(player);
        }
    /*
    * Has certain balance methods
     */
        @Override
        public boolean has(String playerName, double amount) {
            return Account.VaultUtils.getForName(playerName, account ->  account.has(amount));
        }

        @Override
        public boolean has(OfflinePlayer player, double amount) {
            return Account.VaultUtils.getForPotentiallyOffline(player, account ->  account.has(amount));
        }

        @Override
        public boolean has(String playerName, String worldName, double amount) {
            return has(playerName, amount);
        }

        @Override
        public boolean has(OfflinePlayer player, String worldName, double amount) {
            return has(player, amount);
        }
    /*
    * Withdraw methods
     */
        @Override
        public EconomyResponse withdrawPlayer(String playerName, double amount) {
            return Account.VaultUtils.transactionForName(playerName, -amount);
        }
        /**IDK why he's like 'DON'T USE NEGATIVE NUMBERS', it should work fine for mine at least - just redundant*/
        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
            return Account.VaultUtils.transactionForPotentiallyOffline(player, -amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
            return withdrawPlayer(playerName, amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
            return withdrawPlayer(player, amount);
        }
    /*
    * Deposit methods
     */
        @Override
        public EconomyResponse depositPlayer(String playerName, double amount) {
            return Account.VaultUtils.transactionForName(playerName, amount);
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
            return Account.VaultUtils.transactionForPotentiallyOffline(player, amount);
        }

        @Override
        public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
            return depositPlayer(playerName, amount);
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
            return depositPlayer(player, amount);
        }
    /*Bank stuff, not needed for now
    * //////////////////////////////
    * ******************************
    * \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    * ******************************
    * //////////////////////////////
    */
    @Override
    public EconomyResponse createBank(String name, String player) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        throw Account.VaultUtils.BANKS_NOT_SUPPORTED;
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }
    //Like before, account creation is automatic on account getting so just always tell them the account was created
    @Override
    public boolean createPlayerAccount(String playerName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return true;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return true;
    }
}
