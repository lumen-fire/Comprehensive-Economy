package me.lumen.comprehensiveEconomy;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.lumen.comprehensiveEconomy.commands.ComprehensiveEconomyReload;
import me.lumen.comprehensiveEconomy.commands.bounty.BountyClaimItemCommand;
import me.lumen.comprehensiveEconomy.commands.bounty.BountyCommand;
import me.lumen.comprehensiveEconomy.commands.econ.*;
import me.lumen.comprehensiveEconomy.commands.econ.admin.SetBalance;
import me.lumen.comprehensiveEconomy.commands.homeCommands.DelHomeCommand;
import me.lumen.comprehensiveEconomy.commands.homeCommands.HomeCommand;
import me.lumen.comprehensiveEconomy.commands.homeCommands.SetHomeCommand;
import org.jspecify.annotations.NonNull;

import java.util.List;

@SuppressWarnings({"unused", "UnstableApiUsage"})
class ComprehensiveEconomyBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(final @NonNull BootstrapContext context) {
        commands(context);
    }

    private void commands(final @NonNull BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            Commands registrar = commands.registrar();
            registrar.register(HomeCommand.homeCommandBuild, "Teleport to a home");
            registrar.register(SetHomeCommand.setHomeCommandBuild, "Set a home");
            registrar.register(DelHomeCommand.delHomeCommandBuild, "Delete a home");
            registrar.register(PayCommand.COMMAND, "Pay another player");
            registrar.register(BalanceCommand.COMMAND, "Get your balance", List.of("bal"));
            registrar.register(SetBalance.COMMAND,"Set a players balance - intended for debugging and testing mainly");
            registrar.register(BalTopCommand.COMMAND, "View the richest player");
            registrar.register(ComprehensiveEconomyReload.COMMAND, "Reload the plugins config");
            registrar.register(SellCommand.COMMAND, "Sell your items to the server");
            registrar.register(BountyCommand.COMMAND, "Manage your bounties on players");
            registrar.register(BountyClaimItemCommand.COMMAND, "Spawn a bounty claim item for a bounty - mainly intended for debugging");
            registrar.register(WithdrawCommand.COMMAND, "Withdraw currency into a cheque");
        });
    }
}
