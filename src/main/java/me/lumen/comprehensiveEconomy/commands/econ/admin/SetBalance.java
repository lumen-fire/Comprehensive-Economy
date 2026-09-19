package me.lumen.comprehensiveEconomy.commands.econ.admin;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class SetBalance {
    public static final LiteralCommandNode<CommandSourceStack> COMMAND = Commands.literal("setbalance")
            .then(Commands.argument("player", ArgumentTypes.player())
                    .then(Commands.argument("balance",DoubleArgumentType.doubleArg(0))
                            .executes(context -> {
                                Player player = context.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(context.getSource()).getFirst();
                                double amount = CurrencyUtils.round(DoubleArgumentType.getDouble(context, "balance"));
                                Account.getAccount(player).setBalanceAndSave(amount);
                                context.getSource().getSender().sendMessage(Component.text("Set the balance of " + player.getName() + " to " + CurrencyUtils.format(amount)).color(NamedTextColor.GREEN));
                                return (int) amount;
                            })
                    )
            )
            .requires(Commands.restricted(commandSourceStack -> commandSourceStack.getSender().hasPermission("comprehensive-economy.setbalance")))
            .build();
}
