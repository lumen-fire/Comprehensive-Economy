package me.lumen.comprehensiveEconomy.utils;

import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import net.kyori.adventure.text.Component;

public class CommandErrors {
    public static final SimpleCommandExceptionType NOT_PLAYER = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.text("This command can only be used by players!")));
}
