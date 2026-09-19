package me.lumen.comprehensiveEconomy.commands.homeCommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.homes.Home;
import me.lumen.comprehensiveEconomy.homes.arg.HomeArg;
import me.lumen.comprehensiveEconomy.menu.HomeMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Collection;

public class DelHomeCommand {
    public static final LiteralCommandNode<CommandSourceStack> delHomeCommandBuild = Commands.literal("delhome")
            .then(Commands.argument("home", new HomeArg())
                    .executes(DelHomeCommand::deleteHome)
            )
            .executes(context -> {
                if (!(context.getSource().getExecutor() instanceof Player player)){
                    context.getSource().getSender().sendMessage(Component.text("This is a player only command").color(NamedTextColor.RED));
                    return 0;
                }
                Collection<Home> homes = Home.getHomes(player);
                if (homes.isEmpty()){
                    player.sendMessage(Component.text("You have no homes to delete!").color(NamedTextColor.RED));
                    return 0;
                }
                player.showDialog(HomeMenu.getDeleteHomeMenu(homes));
                return Command.SINGLE_SUCCESS;
            })
            .build();
    private static int deleteHome(@NonNull CommandContext<CommandSourceStack> ctx){
        Home home = ctx.getArgument("home", Home.class);
        home.delete();
        ctx.getSource().getSender().sendMessage(Component.text("Deleted home " + home).color(NamedTextColor.YELLOW));
        return Command.SINGLE_SUCCESS;
    }
}
