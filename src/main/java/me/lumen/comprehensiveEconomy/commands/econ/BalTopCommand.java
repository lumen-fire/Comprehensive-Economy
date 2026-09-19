package me.lumen.comprehensiveEconomy.commands.econ;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.menu.BalTopGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalTopCommand {
    public static final LiteralCommandNode<CommandSourceStack> COMMAND = Commands.literal("baltop")
            .then(Commands.literal("list")
                    .executes(context -> {
                        CommandSender sender = context.getSource().getSender();
                        sender.sendMessage(Component.text("Top valued players:").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                        for (int i = 0; i <= 10; i++) {
                            Account.TopAccount topAccount = Account.getNumber(i);
                            //send message if found
                            if (topAccount != null) {
                                String rank = Integer.toString(i + 1);
                                sender.sendMessage(
                                        Component.text(rank + ": ")
                                                .append(Component.text(topAccount.getName())
                                                        .hoverEvent(HoverEvent.showText(
                                                                Component.text("Balance: " + CurrencyUtils.format(topAccount.getBalance())).color(NamedTextColor.GOLD)
                                                                        .append(Component.text("\nCan be up to 1 min out of date").decorate(TextDecoration.ITALIC).color(NamedTextColor.RED))
                                                        ))
                                                        .clickEvent(ClickEvent.callback(audience -> {
                                                            audience.sendMessage(Component.text(topAccount.getName()).color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));
                                                            audience.sendMessage(Component.text("Balance: " + CurrencyUtils.format(topAccount.getBalance())).color(NamedTextColor.LIGHT_PURPLE));
                                                            audience.sendMessage(Component.text("Rank: " + rank).color(NamedTextColor.LIGHT_PURPLE));
                                                            audience.sendMessage(Component.text("Online: " + (Bukkit.getPlayer(topAccount.getUuid()) != null)).color(NamedTextColor.LIGHT_PURPLE));
                                                        }, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).build()))
                                                )
                                                .color(NamedTextColor.GOLD)
                                );
                            }
                        }
                        return Command.SINGLE_SUCCESS;
                    })
            )
            .then(Commands.literal("refresh")
                    .requires(commandSourceStack -> commandSourceStack.getSender().hasPermission("comprehensive-economy.baltoprefresh"))
                    .executes(context -> {
                        CommandSender sender = context.getSource().getSender();
                        sender.sendMessage(Component.text("Refreshing top balances...", NamedTextColor.YELLOW));
                        Account.refreshTopBalancesAnd(() -> sender.sendMessage(Component.text("Refreshed top balances", NamedTextColor.GREEN)));
                        return Command.SINGLE_SUCCESS;
                    })
            )
            .executes(context -> {
                if (context.getSource().getExecutor() instanceof Player player){
                    new BalTopGUI().display(player);
                    return Command.SINGLE_SUCCESS;
                } else {
                    context.getSource().getSender().sendMessage(Component.text("This command can only be used by players").color(NamedTextColor.RED));
                    return 0;
                }
            })
            .build();
}
