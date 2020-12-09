package com.deser.seniorbackup.command;

import lombok.Getter;
import org.bukkit.command.CommandSender;

@Getter
public abstract class CommandArgument {
    private final Boolean isPlayerExclusive;
    private final String argumentName;
    private final String[] argumentAliases;

    public CommandArgument(final Boolean isPlayerExclusive, final String argumentName, final String... argumentAliases) {
        this.isPlayerExclusive = isPlayerExclusive;
        this.argumentName = argumentName;
        this.argumentAliases = argumentAliases;
    }

    public abstract void execute(CommandSender sender, String[] args);
}
