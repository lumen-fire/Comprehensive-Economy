package me.lumen.comprehensiveEconomy.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ComprehensiveEconomyReload {
    public static final LiteralCommandNode<CommandSourceStack> COMMAND = Commands.literal("comprehensive-economy-reload")
            .executes(context -> {
                ComprehensiveEconomy.getPlugin().reloadConfig();
                context.getSource().getSender().sendMessage(Component.text("Config reloaded!").color(NamedTextColor.GREEN));
                return Command.SINGLE_SUCCESS;
            })
            .requires(Commands.restricted(commandSourceStack -> commandSourceStack.getSender().hasPermission("comprehensive-economy.reload")))
            .build();
}
