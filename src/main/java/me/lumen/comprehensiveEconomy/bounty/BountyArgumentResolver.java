package me.lumen.comprehensiveEconomy.bounty;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import me.lumen.comprehensiveEconomy.ComprehensiveEconomy;
import me.lumen.comprehensiveEconomy.utils.CommandErrors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class BountyArgumentResolver {
    private static final SimpleCommandExceptionType NEVER_JOINED = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.text("That player has never joined!")));
    private static final SimpleCommandExceptionType NO_BOUNTY_SET = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.text("You have not set a bounty on that player!")));
    private static final SimpleCommandExceptionType NO_BOUNTY_FOUND = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.text("There is no bounty on that player!")));
    private static final SimpleCommandExceptionType NOT_YOURSELF = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.text("The specified player cannot be yourself!")));

    private final String name;
    private final CommandSourceStack source;
    private final ResolverSettings settings;
    public BountyArgumentResolver(@NonNull String name, CommandSourceStack source, ResolverSettings settings) {
        this.name = name;
        this.source = source;
        this.settings = settings;
    }

    public void runAsync(Consumer<Bounty> logic) throws CommandSyntaxException {
        runAsync(logic, false);
    }

    public void runAsync(Consumer<Bounty> logic, boolean ignoreErrs) throws CommandSyntaxException {
        if (!(source.getExecutor() instanceof Player player)) throw CommandErrors.NOT_PLAYER.create();
        Bukkit.getScheduler().runTaskAsynchronously(ComprehensiveEconomy.getPlugin(), () -> {
            try {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
                if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                    throw NEVER_JOINED.create();
                }
                if (!settings.includesThemself && player.getUniqueId().equals(offlinePlayer.getUniqueId())) {
                    throw NOT_YOURSELF.create();
                }

                if (settings.createIfNotFound){
                    logic.accept(Bounty.getOrCreateBounty(offlinePlayer));
                } else if (settings.playerMustHaveSetBounty){
                    Bounty bounty = Bounty.getBounty(offlinePlayer);
                    if (bounty == null || !bounty.hasSetBounty(player)){
                        throw NO_BOUNTY_SET.create();
                    }
                    logic.accept(bounty);
                } else {
                    Bounty bounty = Bounty.getBounty(offlinePlayer);
                    if (bounty == null){
                        throw NO_BOUNTY_FOUND.create();
                    }
                    logic.accept(bounty);
                }
            } catch (CommandSyntaxException e) {
                if (ignoreErrs) return;
                player.sendMessage(MessageComponentSerializer.message().deserialize(e.getRawMessage()).color(NamedTextColor.RED));
            }
        });
    }

    public static class ResolverSettings {
        private final boolean createIfNotFound;
        private final boolean playerMustHaveSetBounty;
        private final boolean includesThemself;

        public ResolverSettings(boolean createIfNotFound, boolean playerMustHaveSetBounty, boolean includesThemself) {
            this.createIfNotFound = createIfNotFound;
            this.playerMustHaveSetBounty = playerMustHaveSetBounty;
            this.includesThemself = includesThemself;
        }
    }
}
