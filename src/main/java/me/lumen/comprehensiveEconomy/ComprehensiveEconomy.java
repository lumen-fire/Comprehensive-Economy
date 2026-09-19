package me.lumen.comprehensiveEconomy;

import me.lumen.comprehensiveEconomy.bounty.Bounty;
import me.lumen.comprehensiveEconomy.hooks.papi.PAPIExpansion;
import me.lumen.comprehensiveEconomy.hooks.ultimateteams.TeamTeleportListener;
import me.lumen.comprehensiveEconomy.hooks.vault.VaultEconomy;
import me.lumen.comprehensiveEconomy.listeners.*;
import me.lumen.comprehensiveEconomy.tasks.RepeatAsync_1m;
import me.lumen.comprehensiveEconomy.utils.DataStorage;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class ComprehensiveEconomy extends JavaPlugin {
    private static ComprehensiveEconomy plugin;
    public static @NotNull ComprehensiveEconomy getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("This method cannot be called before the plugin has been initialized!");
        }
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        files();
        hooks();
        events();
        tasks();
    }

    private void files(){
        saveDefaultConfig();
        connectDatabase();
        Bounty.loadBounties();
    }

    private void connectDatabase(){
        DataStorage.getInstance().createTables();
        getLogger().info("Database connected successfully");
    }

    private void hooks(){
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            setupEconomy();
            getLogger().info("Hooked into Vault");
        }
        if (Bukkit.getPluginManager().getPlugin("UltimateTeams") != null) {
            Bukkit.getPluginManager().registerEvents(new TeamTeleportListener(), this);
            getLogger().info("Hooked into UltimateTeams");
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPIExpansion().register();
            getLogger().info("Hooked into PlaceholderAPI");
        }
    }

    private void setupEconomy() {
        Bukkit.getServicesManager().register(Economy.class, new VaultEconomy(), this, ServicePriority.High);
    }

    private void events(){
        Bukkit.getPluginManager().registerEvents(new Join(), this);
        Bukkit.getPluginManager().registerEvents(new Leave(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClick(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeath(), this);
        Bukkit.getPluginManager().registerEvents(new Interact(), this);
    }

    private void tasks(){
        new RepeatAsync_1m().start();
    }

    @Override
    public void onDisable() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Bukkit.getServicesManager().unregister(Economy.class);
            getLogger().info("Unhooked Vault");
        }
    }
}
