package me.lumen.comprehensiveEconomy.commands.homeCommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.homes.Home;
import me.lumen.comprehensiveEconomy.homes.arg.NewHomeArg;
import me.lumen.comprehensiveEconomy.menu.HomeMenu;
import me.lumen.comprehensiveEconomy.menu.PurchaseConfirmation;
import me.lumen.comprehensiveEconomy.utils.DatabaseDebounce;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class SetHomeCommand {
    public static final LiteralCommandNode<CommandSourceStack> setHomeCommandBuild = Commands.literal("sethome")
            .then(Commands.argument("home", new NewHomeArg())
                    .executes(SetHomeCommand::setHome)
            )
            .executes(context -> {
                if (!(context.getSource().getExecutor() instanceof Player player)){
                    context.getSource().getSender().sendMessage(Component.text("Only players can execute this command!").color(NamedTextColor.RED));
                    return 0;
                }
                if (Home.getHomesCount(player) >= 5){
                    player.sendMessage(Component.text("You have reached the maximum amount of homes!").color(NamedTextColor.RED));
                    return 0;
                }
                player.showDialog(HomeMenu.getSetHomeMenu(Home.getHomesCount(player) + 1));
                return Command.SINGLE_SUCCESS;
            })
            .build();
    //set the home
    private static int setHome(@NonNull CommandContext<CommandSourceStack> ctx) {
        Home home = ctx.getArgument("home", Home.class);
        Entity executor = ctx.getSource().getExecutor();
        if (executor instanceof Player player){
            //max homes
            int homes = Home.getHomesCount(player);
            if (homes >= 5){
                player.sendMessage(Component.text("You have reached the maximum number of homes!").color(NamedTextColor.RED));
                return 0;
            }
            //get cost
            double cost = Home.calculateSetCost(player);
            //err message for not enough money
            if (!Account.getAccount(player).has(cost)){
                player.sendMessage(Component.text("You do not have enough funds for this, you need " + CurrencyUtils.format(cost)).color(NamedTextColor.RED));
                return 0;
            }
            //show purchase confirmation
            PurchaseConfirmation.show(player, cost, "Home #" + (homes + 1), presser -> {
                //spam proofing
                if (DatabaseDebounce.blocked(presser)){
                    presser.playSound(presser.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f,1f);
                    presser.sendActionBar(DatabaseDebounce.getTooFastMessage(presser));
                    return;
                }
                DatabaseDebounce.update(presser, 1000);
                home.save();
            });
            return Command.SINGLE_SUCCESS;
        }else{
            //this would never happen anyway as the NewHomeArg requires to be sent by a player
            return 0;
        }
    }
}
