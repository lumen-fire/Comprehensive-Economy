package me.lumen.comprehensiveEconomy.tasks;

import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.economy.Account;
import org.bukkit.scheduler.BukkitRunnable;

public class RepeatAsync_1m {
    public void start(){
        new BukkitRunnable(){
            @Override
            public void run(){
                Account.refreshTopBalances();
            }
        }.runTaskTimerAsynchronously(ComprehensiveEconomy.getPlugin(), 300, 1200);
    }
}
