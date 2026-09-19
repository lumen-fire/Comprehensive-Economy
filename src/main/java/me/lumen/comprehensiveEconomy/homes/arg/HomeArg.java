package me.lumen.comprehensiveEconomy.homes.arg;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.lumen.comprehensiveEconomy.homes.Home;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class HomeArg implements CustomArgumentType.Converted<Home, String> {
    private static final DynamicCommandExceptionType HOME_NOT_FOUND = new DynamicCommandExceptionType(o -> MessageComponentSerializer.message().serialize(Component.text("Home " + o + " not found!")));
    private static final SimpleCommandExceptionType NOT_PLAYER = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.text("Only players can use this command!")));
    @Override
    public @NonNull Home convert(@NonNull String nativeType) {
        throw new UnsupportedOperationException("This should never be called");
    }

    @Override
    public <S> @NonNull Home convert(final @NonNull String nativeType, final @NonNull S source) throws CommandSyntaxException {
        //check valid source
        if (source instanceof CommandSourceStack commandSource) {
            //check player
            if (commandSource.getExecutor() instanceof Player player){
                Home home = Home.getHome(player, nativeType);
                //err if home not found
                if (home == null){
                    throw HOME_NOT_FOUND.create(nativeType);
                }
                return home;
            } else {
                throw NOT_PLAYER.create();
            }
        } else {
            throw new IllegalArgumentException("Invalid source - must be a CommandSourceStack");
        }
    }

    @Override
    public @NonNull ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public <S> @NonNull CompletableFuture<Suggestions> listSuggestions(@NonNull CommandContext<S> context, @NonNull SuggestionsBuilder builder) {
        if (context.getSource() instanceof CommandSourceStack commandSource && commandSource.getExecutor() instanceof Player player) {
            for (String home : Home.getHomeNames(player)){
                builder.suggest(home);
            }
        }
        return builder.buildFuture();
    }
}
