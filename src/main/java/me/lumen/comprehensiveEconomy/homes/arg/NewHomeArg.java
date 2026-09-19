package me.lumen.comprehensiveEconomy.homes.arg;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import me.lumen.comprehensiveEconomy.homes.Home;
import me.lumen.comprehensiveEconomy.utils.CommandErrors;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class NewHomeArg implements CustomArgumentType<Home, String> {
    private static final DynamicCommandExceptionType HOME_EXISTS = new DynamicCommandExceptionType(o -> MessageComponentSerializer.message().serialize(Component.text("Home " + o + " already exists!")));
    @Override
    public @NonNull Home parse(@NonNull StringReader reader) {
        throw new UnsupportedOperationException("Should never be called");
    }

    @Override
    public @NonNull ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public <S> @NonNull Home parse(final @NonNull StringReader reader, final @NonNull S source) throws CommandSyntaxException {
        //check valid source
        if (source instanceof CommandSourceStack commandSourceStack){
            //check player
            if (commandSourceStack.getExecutor() instanceof Player player){
                String name = getNativeType().parse(reader);
                List<String> homes = Home.getHomeNames(player);
                if (homes.contains(name)){
                    throw HOME_EXISTS.create(name);
                }
                return new Home(player, commandSourceStack.getLocation(), name);
            } else {
                throw CommandErrors.NOT_PLAYER.create();
            }
        }else {
            throw new IllegalArgumentException("Bad command source!");
        }
    }
}
