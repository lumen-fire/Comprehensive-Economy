package me.lumen.comprehensiveEconomy.commands.econ;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.utils.DatabaseDebounce;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class PayCommand {
    public static final LiteralCommandNode<CommandSourceStack> COMMAND = Commands.literal("pay")
            .then(Commands.argument("player", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            builder.suggest(player.getName());
                        }
                        return builder.buildFuture();
                    })
                    .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                            .executes(context -> {
                                CommandSourceStack source = context.getSource();
                                if (source.getExecutor() instanceof Player player) {
                                    if (DatabaseDebounce.blocked(player)){
                                        player.sendActionBar(DatabaseDebounce.getTooFastMessage(player));
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
                                        return 0;
                                    }
                                    DatabaseDebounce.update(player, 50);

                                    String name = StringArgumentType.getString(context, "player");
                                    double amount = CurrencyUtils.round(DoubleArgumentType.getDouble(context, "amount"));
                                    Account playersAccount = Account.getAccount(player);
                                    if (!playersAccount.has(amount)){
                                        player.sendMessage(Component.text("You do not have enough money to do this!").color(NamedTextColor.RED));
                                        return 0;
                                    }

                                   Account.executeOnName(name, account -> {
                                       //if they are online
                                       Player onlineReceiver = Bukkit.getPlayer(account.uuid());
                                       if (onlineReceiver != null){
                                           player.sendMessage(Component.text("Payed " + onlineReceiver.getName() + " " + CurrencyUtils.format(amount)).color(NamedTextColor.GREEN));
                                           onlineReceiver.sendMessage(Component.text(player.getName() + " payed you " + CurrencyUtils.format(amount)).color(NamedTextColor.GREEN));
                                           playersAccount.transferAndSave(amount, account);
                                           return;
                                       }
                                       playersAccount.transferAndSave(amount, account);
                                       OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(account.uuid());
                                       player.sendMessage(Component.text("Payed " + offlinePlayer.getName() + " " + CurrencyUtils.format(amount)).color(NamedTextColor.GREEN));
                                   }, s -> player.sendMessage(Component.text(s).color(NamedTextColor.RED)));
                                    return Command.SINGLE_SUCCESS;
                                } else {
                                    return 0;
                                }
                            }))).build();
}
