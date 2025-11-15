package com.dractical.femutils.paper.command;

import org.bukkit.command.TabCompleter;

import java.util.List;
import java.util.Locale;

/**
 * Subcommand definition.
 */
@SuppressWarnings("unused")
final class Subcommand {

    final String name;
    final List<String> aliases;
    final CommandAction executor;
    final String permission;
    final boolean playerOnly;
    final boolean consoleOnly;
    final String description;
    final String usage;
    final String invalidUsageMessage;
    final TabCompleter tabCompleter;

    Subcommand(SubcommandBuilder builder) {
        this.name = builder.name;
        this.aliases = List.copyOf(builder.aliases);
        this.executor = builder.executor;
        this.permission = builder.permission;
        this.playerOnly = builder.playerOnly;
        this.consoleOnly = builder.consoleOnly;
        this.description = builder.description;
        this.usage = builder.usage;
        this.invalidUsageMessage = builder.invalidUsageMessage;
        this.tabCompleter = builder.tabCompleter;
    }

    boolean matches(String input) {
        if (input == null) {
            return false;
        }
        String normalized = input.toLowerCase(Locale.ROOT);
        if (name.equalsIgnoreCase(normalized)) {
            return true;
        }
        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(normalized)) {
                return true;
            }
        }
        return false;
    }
}
