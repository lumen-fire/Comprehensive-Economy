package me.lumen.comprehensiveEconomy.commands.econ;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.menu.SellGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class SellCommand {
    public static LiteralCommandNode<CommandSourceStack> COMMAND = Commands.literal("sell")
            .then(Commands.literal("hand")
                    .executes(context -> {
                        if (context.getSource().getExecutor() instanceof Player player) {
                            ItemStack item = player.getInventory().getItemInMainHand();
                            Material mat = item.getType();
                            if (CurrencyUtils.isUnSellable(mat)) {
                                player.sendMessage(Component.text("That item cannot be sold!").color(NamedTextColor.RED));
                                sendSellableItems(player);
                                return 0;
                            }
                            Account account = Account.getAccount(player);
                            account.sell(item);
                            account.saveChanges();
                            String name = CurrencyUtils.getName(mat);
                            if (name == null) name = mat.getKey().asString();
                            player.sendMessage(Component.text("Sold one " + name.toLowerCase() + " for " + CurrencyUtils.format(CurrencyUtils.getSellPrice(mat).orElse(0D))).color(NamedTextColor.GREEN));
                            return Command.SINGLE_SUCCESS;
                        }
                        context.getSource().getSender().sendMessage(Component.text("This is a player only command!").color(NamedTextColor.RED));
                        return 0;
                    })
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes(context -> {
                                if (context.getSource().getExecutor() instanceof Player player) {
                                    ItemStack item = player.getInventory().getItemInMainHand();
                                    Material mat = item.getType();
                                    if (CurrencyUtils.isUnSellable(mat)) {
                                        player.sendMessage(Component.text("That item cannot be sold!").color(NamedTextColor.RED));
                                        sendSellableItems(player);
                                        return 0;
                                    }
                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                    if (item.getAmount() < amount){
                                        player.sendMessage(Component.text("You do not have enough of that item!").color(NamedTextColor.RED));
                                        return 0;
                                    }
                                    Account account = Account.getAccount(player);
                                    account.sell(item, amount);
                                    account.saveChanges();
                                    String name = CurrencyUtils.getName(mat);
                                    if (name == null) name = mat.getKey().asString();
                                    player.sendMessage(Component.text("Sold " + amount + " " + name.toLowerCase() + " for " + CurrencyUtils.format(CurrencyUtils.getSellPrice(mat, amount).orElse(0D))).color(NamedTextColor.GREEN));
                                    return Command.SINGLE_SUCCESS;
                                }
                                context.getSource().getSender().sendMessage(Component.text("This is a player only command!").color(NamedTextColor.RED));
                                return 0;
                            })
                    )
            )
            .executes(context -> {
                if (context.getSource().getExecutor() instanceof Player player) {
                    new SellGUI().show(player);
                    return Command.SINGLE_SUCCESS;
                }
                context.getSource().getSender().sendMessage(Component.text("This is a player only command!").color(NamedTextColor.RED));
                return 0;
            })
            .build();

    private static void sendSellableItems(@NonNull Player player){
        player.sendMessage(Component.text("Sellable items:").color(NamedTextColor.GREEN));
        Map<String, Double> prices = CurrencyUtils.getSellPrices();
        for (String material : prices.keySet()) {
            player.sendMessage(Component.text(CurrencyUtils.getName(material) + ": " + prices.get(material)).color(NamedTextColor.GREEN));
        }
    }
}
