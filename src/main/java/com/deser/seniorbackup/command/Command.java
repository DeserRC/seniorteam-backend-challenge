package com.deser.seniorbackup.command;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Getter
public abstract class Command extends BukkitCommand {
    private Set<CommandArgument> arguments;

    @SneakyThrows
    public Command(final String cmd, final String... alias) {
        super(cmd);
        setAliases(Arrays.asList(alias));

        final Field commandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
        commandMap.setAccessible(true);
        final CommandMap cm = (CommandMap) commandMap.get(Bukkit.getServer());
        cm.register(cmd, this);
    }

    public void setArguments(CommandArgument... arguments) {
        this.arguments = Arrays.stream(arguments).collect(toSet());
    }
}
