package me.lumen.comprehensiveEconomy.commands.econ;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.utils.CommandErrors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class WithdrawCommand {
    public static final LiteralCommandNode<CommandSourceStack> COMMAND = Commands.literal("withdraw")
            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                    .executes(context -> {
                        if (!(context.getSource().getExecutor() instanceof Player player)) {
                            throw CommandErrors.NOT_PLAYER.create();
                        }
                        double amount = DoubleArgumentType.getDouble(context, "amount");
                        Account account = Account.getAccount(player);

                        if (!account.withdrawAndSave(amount)){
                            player.sendMessage(Component.text("You need " + CurrencyUtils.format(amount)).color(NamedTextColor.RED));
                            return 0;
                        }

                        player.give(account.withdrawToCheque(amount));
                        player.sendMessage(Component.text("Withdrew a cheque of " + CurrencyUtils.format(amount)).color(NamedTextColor.GREEN));
                        return Command.SINGLE_SUCCESS;
                    })
            )
            .build();
}
