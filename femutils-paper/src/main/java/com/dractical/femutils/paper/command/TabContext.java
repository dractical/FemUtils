package com.dractical.femutils.paper.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Context passed to tab completion providers.
 */
@SuppressWarnings("unused")
public final class TabContext {

    private final CommandSender sender;
    private final Command command;
    private final String alias;
    private final String[] args;

    TabContext(CommandSender sender, Command command, String alias, String[] args) {
        this.sender = sender;
        this.command = command;
        this.alias = alias;
        this.args = args != null ? args : new String[0];
    }

    public CommandSender sender() {
        return sender;
    }

    public Command command() {
        return command;
    }

    public String alias() {
        return alias;
    }

    public String[] args() {
        return args;
    }

    public int argsLength() {
        return args.length;
    }

    public String arg(int index) {
        if (index < 0 || index >= args.length) {
            return null;
        }
        return args[index];
    }

    public String currentToken() {
        if (args.length == 0) {
            return "";
        }
        return args[args.length - 1];
    }
}
