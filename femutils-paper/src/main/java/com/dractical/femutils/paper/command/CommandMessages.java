package com.dractical.femutils.paper.command;

import com.dractical.femutils.paper.lang.Lang;
import org.bukkit.command.CommandSender;

/**
 * Command messages.
 */
public final class CommandMessages {
    public static final String DEFAULT_NO_PERMISSION = "<red>You do not have permission to use this command.</red>";
    public static final String DEFAULT_PLAYERS_ONLY  = "<red>This command can only be used by players.</red>";
    public static final String DEFAULT_CONSOLE_ONLY  = "<red>This command can only be used from console.</red>";
    public static final String DEFAULT_INVALID_USAGE = "<red>Invalid usage.</red>";

    private CommandMessages() {
        throw new AssertionError("No " + CommandMessages.class.getName() + " instances");
    }

    public static void sendNoPermission(CommandSender sender, String message) {
        Lang.send(sender, message != null ? message : DEFAULT_NO_PERMISSION);
    }

    public static void sendPlayersOnly(CommandSender sender, String message) {
        Lang.send(sender, message != null ? message : DEFAULT_PLAYERS_ONLY);
    }

    public static void sendConsoleOnly(CommandSender sender, String message) {
        Lang.send(sender, message != null ? message : DEFAULT_CONSOLE_ONLY);
    }

    public static void sendInvalidUsage(CommandSender sender, String baseMessage, String usage, String label) {
        String msg = baseMessage != null ? baseMessage : DEFAULT_INVALID_USAGE;
        if (usage != null && !usage.isEmpty()) {
            if (label != null && !label.isEmpty()) {
                msg = msg + " <gray>Usage:</gray> <white>/" + label + " " + usage + "</white>";
            } else {
                msg = msg + " <gray>Usage:</gray> <white>" + usage + "</white>";
            }
        }
        Lang.send(sender, msg);
    }
}
