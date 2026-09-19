package me.lumen.comprehensiveEconomy.commands.homeCommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.homes.Home;
import me.lumen.comprehensiveEconomy.homes.arg.HomeArg;
import me.lumen.comprehensiveEconomy.menu.HomeMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;


public class HomeCommand {
    public static final LiteralCommandNode<CommandSourceStack> homeCommandBuild = Commands.literal("home")
            .then(Commands.argument("home", new HomeArg())
                    .executes(HomeCommand::teleportHome)
            )
            .executes(context -> {
                if (!(context.getSource().getExecutor() instanceof Player player)){
                    context.getSource().getSender().sendMessage(Component.text("Only players can use this command!").color(NamedTextColor.RED));
                    return 0;
                }
                HomeMenu.showHomesMenu(player);
                return Command.SINGLE_SUCCESS;
            })
            .build();
    private static int teleportHome(@NonNull CommandContext<CommandSourceStack> ctx){
        Entity executor = ctx.getSource().getExecutor();
        Home home = ctx.getArgument("home", Home.class);
        if (executor instanceof Player player){
            //check has enough
            double price = Home.calculateTeleportCost(player);
            if (!Account.getAccount(player).has(price)){
                player.sendMessage(Component.text("You do not have enough " + CurrencyUtils.getCurrencyNamePlural() + " to use your homes!").color(NamedTextColor.RED));
                player.sendMessage(Component.text("You need " + CurrencyUtils.format(price) + " to teleport, consider lowering the amount of homes you have to reduce the price").color(NamedTextColor.RED));
                return 0;
            }
            home.countdownTeleport(10);
            return Command.SINGLE_SUCCESS;
        }else{
            return 0;
        }
    }
}
