package me.lumen.comprehensiveEconomy.commands.econ;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class BalanceCommand {
    public static final LiteralCommandNode<CommandSourceStack> COMMAND = Commands.literal("balance")
            .executes(context -> {
                CommandSourceStack source = context.getSource();
                if (source.getExecutor() instanceof Player player){
                    Account account = Account.getAccount(player);
                    player.sendMessage(Component.text("Your balance is: " + account.getBalance() + " " + CurrencyUtils.getCurrencyNamePlural()).color(NamedTextColor.GREEN));
                    return (int) account.getBalance();
                }
                source.getSender().sendMessage(Component.text("This command can only be executed by players!").color(NamedTextColor.RED));
                return 0;
            })
            .build();
}
