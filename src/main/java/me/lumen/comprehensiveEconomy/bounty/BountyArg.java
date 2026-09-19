package me.lumen.comprehensiveEconomy.bounty;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

/**
 * The completable future this type returns in commands may be completed exceptionally with a command syntax exception if the player was not found
 */
public class BountyArg implements CustomArgumentType.Converted<BountyArgumentResolver, String> {
    private final boolean createIfNotFound;
    private final boolean playerMustHaveSetBounty;
    private final boolean includesThemself;

    /**
     * The completable future this type returns in commands may be completed exceptionally with a command syntax exception if the player was not found.
     * Defaults to not requiring the player to have set a bounty on the player themselves and the specified player not being them self
     * @param createIfNotFound whether to return a new bounty if none was found
     */
    public BountyArg(boolean createIfNotFound) {
        this(createIfNotFound, false);
    }

    /**
     *
     * @param createIfNotFound whether to create if no bounty is found
     * @param playerMustHaveSetBounty whether the player who ran the command must have a bounty set on that player - irrelevant if createIfNotFound is true
     * Defaults to not allowing them to enter themselves
     */
    public BountyArg(boolean createIfNotFound, boolean playerMustHaveSetBounty) {
        this(createIfNotFound, playerMustHaveSetBounty, false);
    }

    /**
     * @param createIfNotFound whether to create if no bounty is found
     * @param playerMustHaveSetBounty whether the player who ran the command must have a bounty set on that player - irrelevant if createIfNotFound is true
     * @param includesThemself whether to allow the player to enter themselves
     */
    public BountyArg(boolean createIfNotFound, boolean playerMustHaveSetBounty, boolean includesThemself) {
        this.createIfNotFound = createIfNotFound;
        this.includesThemself = includesThemself;
        this.playerMustHaveSetBounty = playerMustHaveSetBounty;
    }

    /**
     * Creates this arg type, defaulting to creating a new bounty if no bounty was found, not allowing them to enter themselves and not requiring the player to have a bounty set on that player.
     * The completable future this type returns in commands may be completed exceptionally with a command syntax exception if the player was not found
     */
    public BountyArg(){
        this(true);
    }

    @Override
    public @NonNull BountyArgumentResolver convert(@NonNull String nativeType) {
        throw new UnsupportedOperationException("Not supported!");
    }

    @Override
    public <S> @NonNull BountyArgumentResolver convert(final @NonNull String nativeType, final @NonNull S source) {
        if (!(source instanceof CommandSourceStack stack)) throw new IllegalArgumentException("Source is not a CommandSourceStack!");
        return new BountyArgumentResolver(nativeType, stack, new BountyArgumentResolver.ResolverSettings(createIfNotFound,  playerMustHaveSetBounty, includesThemself));
    }

    @Override
    public @NonNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }

    @Override
    public <S> @NonNull CompletableFuture<Suggestions> listSuggestions(final @NonNull CommandContext<S> context, final @NonNull SuggestionsBuilder builder) {
        if (!(context.getSource() instanceof CommandSourceStack stack)) throw new IllegalArgumentException("Source is not a CommandSourceStack!");
        if (!(stack.getExecutor() instanceof Player sender)) return builder.buildFuture();
        if (createIfNotFound){
            for (Player player : Bukkit.getOnlinePlayers()){
                // if sender is player skip
                if (!includesThemself && sender == player) {
                    continue;
                }
                builder.suggest(player.getName());
            }
        } else if (playerMustHaveSetBounty) {
            //suggest all bounties they have set
            for (Bounty bounty: Bounty.getBounties().values()){
                if (!includesThemself && bounty.uuid().equals(sender.getUniqueId())) {
                    continue;
                }
                if (!bounty.hasSetBounty(sender)) continue;
                String name = bounty.createName();
                builder.suggest(name);
            }
        } else {
            //suggest all bounties
            for (Bounty bounty: Bounty.getBounties().values()){
                if (!includesThemself && bounty.uuid().equals(sender.getUniqueId())) {
                    continue;
                }
                String name = bounty.createName();
                builder.suggest(name);
            }
        }

        return builder.buildFuture();
    }
}
