package me.lumen.comprehensiveEconomy.commands.bounty;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.bounty.Bounty;
import me.lumen.comprehensiveEconomy.bounty.BountyArg;
import me.lumen.comprehensiveEconomy.utils.CommandErrors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class BountyClaimItemCommand {
    public static final LiteralCommandNode<CommandSourceStack> COMMAND = Commands.literal("bounty-claim-item-spawn")
            .requires(Commands.restricted(commandSourceStack -> commandSourceStack.getSender().hasPermission("comprehensive-economy.bounty-item")))
            .then(Commands.argument("player", new BountyArg(false))
                    .executes(context -> {
                        @SuppressWarnings("unchecked") CompletableFuture<Bounty> future = context.getArgument("player", CompletableFuture.class);
                        Location location = context.getSource().getLocation();
                        CommandSender sender = context.getSource().getSender();
                        if (!(context.getSource().getExecutor() instanceof Player player)) throw CommandErrors.NOT_PLAYER.create();
                        future.thenAccept(bounty -> {
                            bounty.spawnClaimItem(location, player);
                            String name = Bukkit.getOfflinePlayer(bounty.uuid()).getName();
                            sender.sendMessage(Component.text("Bounty claim item spawned for " + name).color(NamedTextColor.GREEN));
                        });
                        return Command.SINGLE_SUCCESS;
                    })
            )
            .build();
}
