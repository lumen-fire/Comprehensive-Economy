package me.lumen.comprehensiveEconomy.commands.bounty;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import me.lumen.comprehensiveEconomy.bounty.BountyArg;
import me.lumen.comprehensiveEconomy.bounty.BountyArgumentResolver;
import me.lumen.comprehensiveEconomy.economy.Account;
import me.lumen.comprehensiveEconomy.economy.CurrencyUtils;
import me.lumen.comprehensiveEconomy.menu.bounty.BountyDialogMenu;
import me.lumen.comprehensiveEconomy.menu.bounty.BountyInventoryGUI;
import me.lumen.comprehensiveEconomy.utils.CommandErrors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public class BountyCommand {
    public static final LiteralCommandNode<CommandSourceStack> COMMAND = Commands.literal("bounty")
            .executes(context -> {
                if (!(context.getSource().getExecutor() instanceof Player player)) throw CommandErrors.NOT_PLAYER.create();
                new BountyInventoryGUI().show(player);
                return Command.SINGLE_SUCCESS;
            })
            .then(Commands.literal("set")
                    .then(Commands.argument("player", new BountyArg())
                            .executes(context -> {
                                if (!(context.getSource().getExecutor() instanceof Player player)) throw CommandErrors.NOT_PLAYER.create();
                                BountyArgumentResolver resolver = context.getArgument("player", BountyArgumentResolver.class);
                                resolver.runAsync(bounty -> new BountyDialogMenu(player, bounty).showSettingMenu(Audience::closeDialog));
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                    .executes(context -> {
                                        BountyArgumentResolver resolver = context.getArgument("player", BountyArgumentResolver.class);
                                        double amount = CurrencyUtils.round(DoubleArgumentType.getDouble(context, "amount"));
                                        Entity executor = context.getSource().getExecutor();
                                        if (!(executor instanceof Player player)){
                                            throw CommandErrors.NOT_PLAYER.create();
                                        }

                                        resolver.runAsync(bounty -> {
                                            double oldBounty = bounty.getSetBounty(player);
                                            double price = amount - oldBounty;
                                            Account account = Account.getAccount(player);

                                            if (!account.withdrawAndSave(price)){
                                                player.sendMessage(Component.text("You do not have " + CurrencyUtils.format(price)).color(NamedTextColor.RED));
                                                return;
                                            }
                                            String name = Bukkit.getOfflinePlayer(bounty.uuid()).getName();
                                            bounty.setBounty(player, amount);
                                            player.sendMessage(Component.text("Set your bounty on " + name + " to " + CurrencyUtils.format(amount)).color(NamedTextColor.GREEN));
                                        });
                                        return Command.SINGLE_SUCCESS;
                                    })
                                    .suggests((context, builder) -> {
                                        CompletableFuture<Suggestions> future = new CompletableFuture<>();
                                        BountyArgumentResolver resolver = context.getArgument("player", BountyArgumentResolver.class);
                                        if (!(context.getSource().getExecutor() instanceof Player player)){
                                            return future;
                                        }
                                        resolver.runAsync(bounty -> {
                                            if (bounty.hasSetBounty(player)){
                                                builder.suggest(BigDecimal.valueOf(bounty.getSetBounty(player)).toPlainString());
                                                future.complete(builder.build());
                                            }
                                        }, true);
                                        return future;
                                    })
                            )
                    )
            )
            .then(Commands.literal("add")
                    .then(Commands.argument("player", new BountyArg())
                            .executes(context -> {
                                if (!(context.getSource().getExecutor() instanceof Player player)) throw CommandErrors.NOT_PLAYER.create();
                                BountyArgumentResolver resolver  = context.getArgument("player", BountyArgumentResolver.class);
                                resolver.runAsync(bounty -> new BountyDialogMenu(player, bounty).showAddingMenu(Audience::closeDialog));
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                    .executes(context -> {
                                        BountyArgumentResolver resolver = context.getArgument("player", BountyArgumentResolver.class);
                                        double amount = CurrencyUtils.round(DoubleArgumentType.getDouble(context, "amount"));
                                        Entity executor = context.getSource().getExecutor();
                                        if (!(executor instanceof Player player)){
                                            throw CommandErrors.NOT_PLAYER.create();
                                        }

                                        resolver.runAsync(bounty -> {
                                            Account account = Account.getAccount(player);
                                            if (!account.withdrawAndSave(amount)){
                                                player.sendMessage(Component.text("You do not have " + CurrencyUtils.format(amount)).color(NamedTextColor.RED));
                                                return;
                                            }
                                            String name = Bukkit.getOfflinePlayer(bounty.uuid()).getName();
                                            bounty.addToBounty(player, amount);
                                            player.sendMessage(Component.text("Added " + CurrencyUtils.format(amount) + " to your bounty on " + name).color(NamedTextColor.GREEN));
                                        });

                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            )
            .then(Commands.literal("remove")
                    .then(Commands.argument("player", new BountyArg(false, true))
                            .executes(context -> {
                                BountyArgumentResolver resolver = context.getArgument("player", BountyArgumentResolver.class);
                                Entity executor = context.getSource().getExecutor();
                                if (!(executor instanceof Player player)){
                                    throw CommandErrors.NOT_PLAYER.create();
                                }
                                resolver.runAsync(bounty -> {
                                    Account account = Account.getAccount(player);
                                    double old = bounty.getSetBounty(player);
                                    String name = Bukkit.getOfflinePlayer(bounty.uuid()).getName();
                                    account.depositAndSave(old);
                                    bounty.removeBounty(player);
                                    player.sendMessage(Component.text("Removed your bounty on " + name).color(NamedTextColor.YELLOW));
                                });
                                return Command.SINGLE_SUCCESS;
                            })
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                    .executes(context -> {
                                        BountyArgumentResolver resolver = context.getArgument("player", BountyArgumentResolver.class);
                                        double amount = CurrencyUtils.round(DoubleArgumentType.getDouble(context, "amount"));
                                        Entity executor = context.getSource().getExecutor();
                                        if (!(executor instanceof Player player)){
                                            throw CommandErrors.NOT_PLAYER.create();
                                        }
                                        resolver.runAsync(bounty -> {
                                            Account account = Account.getAccount(player);
                                            if (!bounty.subtractFromBounty(player, amount)){
                                                player.sendMessage(Component.text("You cannot remove an amount higher than the bounty you set!").color(NamedTextColor.RED));
                                                return;
                                            }
                                            account.depositAndSave(amount);
                                            String name = Bukkit.getOfflinePlayer(bounty.uuid()).getName();
                                            player.sendMessage(Component.text("Removed " + CurrencyUtils.format(amount) + " from your bounty on " + name).color(NamedTextColor.YELLOW));
                                        });
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            )
            .then(Commands.literal("get")
                    .then(Commands.argument("player", new BountyArg(false, false, true))
                            .executes(context -> {
                                BountyArgumentResolver resolver = context.getArgument("player", BountyArgumentResolver.class);
                                Entity executor = context.getSource().getExecutor();
                                if (!(executor instanceof Player player)){
                                    throw CommandErrors.NOT_PLAYER.create();
                                }
                                resolver.runAsync(bounty -> {
                                    String name = Bukkit.getOfflinePlayer(bounty.uuid()).getName();
                                    player.sendMessage(Component.text("Bounty info: " + name).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
                                    player.sendMessage(Component.text("Total: " + CurrencyUtils.format(bounty.getTotal())).color(NamedTextColor.GOLD));
                                    player.sendMessage(Component.text("Your bounty set: " + CurrencyUtils.format(bounty.getSetBounty(player))).color(NamedTextColor.GOLD));
                                });
                                return Command.SINGLE_SUCCESS;
                            })
                    )
            )
            .build();
}
