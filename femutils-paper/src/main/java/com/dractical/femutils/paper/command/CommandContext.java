package com.dractical.femutils.paper.command;

import com.dractical.femutils.core.check.Checks;
import com.dractical.femutils.core.text.StringUtils;
import com.dractical.femutils.core.time.DurationUtils;
import com.dractical.femutils.paper.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.Locale;

/**
 * Gives access to sender, args, and common argument parsers.
 */
@SuppressWarnings("unused")
public final class CommandContext {

    private final Plugin plugin;
    private final CommandSender sender;
    private final Command command;
    private final String label;
    private final String[] args;
    private final String invalidUsageMessage;
    private final String usage;

    CommandContext(Plugin plugin, CommandSender sender, Command command, String label, String[] args, String invalidUsageMessage, String usage) {
        this.plugin = Checks.notNull(plugin, "plugin");
        this.sender = Checks.notNull(sender, "sender");
        this.command = Checks.notNull(command, "command");
        this.label = label;
        this.args = (args != null ? args : new String[0]);
        this.invalidUsageMessage = invalidUsageMessage;
        this.usage = usage;
    }

    public Plugin plugin() {
        return plugin;
    }

    public CommandSender sender() {
        return sender;
    }

    public Command command() {
        return command;
    }

    public String label() {
        return label;
    }

    public String[] args() {
        return args;
    }

    public int argsLength() {
        return args.length;
    }

    public String arg(int index) {
        return (index >= 0 && index < args.length) ? args[index] : null;
    }

    public String joinArgs(int fromIndex) {
        if (fromIndex < 0 || fromIndex >= args.length) {
            return "";
        }
        String[] slice = new String[args.length - fromIndex];
        System.arraycopy(args, fromIndex, slice, 0, slice.length);
        return StringUtils.join(' ', slice);
    }

    public boolean isPlayer() {
        return sender instanceof Player;
    }

    public Player asPlayer() {
        return (sender instanceof Player) ? (Player) sender : null;
    }

    /**
     * Requires player sender.
     */
    public Player requirePlayerSender() {
        if (sender instanceof Player) {
            return (Player) sender;
        }
        CommandMessages.sendPlayersOnly(sender, CommandMessages.DEFAULT_PLAYERS_ONLY);
        return null;
    }

    /**
     * Sends invalid usage text and this command's label.
     */
    public void invalidUsage() {
        CommandMessages.sendInvalidUsage(sender, invalidUsageMessage, usage, label);
    }

    /**
     * Sends a simple error message to sender.
     */
    public void error(String message) {
        if (message != null && !message.isEmpty()) {
            Lang.send(sender, message);
        }
    }

    /**
     * Online player by name. Returns null if missing.
     */
    public Player argOnlinePlayer(int index) {
        String raw = arg(index);
        if (raw == null || raw.isEmpty()) return null;
        return Bukkit.getPlayerExact(raw) != null ? Bukkit.getPlayerExact(raw) : Bukkit.getPlayer(raw);
    }

    /**
     * Offline (or online) player by name. Returns null if missing.
     */
    public OfflinePlayer argOfflinePlayer(int index) {
        String raw = arg(index);
        if (raw == null || raw.isEmpty()) return null;
        return Bukkit.getOfflinePlayer(raw);
    }

    /**
     * Returns target player for commands that accept optional [player].
     */
    public Player argPlayerOrSelf(int index) {
        String raw = arg(index);
        if (raw == null || raw.isEmpty()) {
            return asPlayer();
        }
        return argOnlinePlayer(index);
    }

    /**
     * Tries to parse an enum value from args[index], case-insensitive.
     */
    public <E extends Enum<E>> E argEnum(int index, Class<E> enumClass) {
        String raw = arg(index);
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.isEmpty()) return null;

        String upper = raw.toUpperCase(Locale.ROOT);
        for (E constant : enumClass.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(upper)) {
                return constant;
            }
        }
        return null;
    }

    /**
     * Parses duration expressions like "10s", "5m", "2h", "1d", "1h30m", "500ms"
     */
    public Duration argDuration(int index) {
        String raw = arg(index);
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        return parseDuration(raw);
    }

    /**
     * Internal lightweight duration parser.
     */
    private Duration parseDuration(String text) {
        String s = text.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return null;

        long totalMillis = 0L;
        int len = s.length();
        int pos = 0;

        while (pos < len) {
            int startNum = pos;
            while (pos < len && Character.isDigit(s.charAt(pos))) {
                pos++;
            }
            if (startNum == pos) {
                return null;
            }
            long value;
            try {
                value = Long.parseLong(s.substring(startNum, pos));
            } catch (NumberFormatException ex) {
                return null;
            }

            int startUnit = pos;
            while (pos < len && Character.isLetter(s.charAt(pos))) {
                pos++;
            }
            if (startUnit == pos) {
                return null;
            }
            String unit = s.substring(startUnit, pos);

            long factor;
            switch (unit) {
                case "ms" -> factor = 1L;
                case "s", "sec", "secs" -> factor = 1000L;
                case "m", "min", "mins" -> factor = 60_000L;
                case "h", "hr", "hrs" -> factor = 3_600_000L;
                case "d", "day", "days" -> factor = 86_400_000L;
                default -> {
                    return null;
                }
            }

            totalMillis += value * factor;
        }

        return Duration.ofMillis(totalMillis);
    }

    /**
     * Duration pretty printer.
     */
    public String prettyDuration(Duration d) {
        return DurationUtils.prettyPrint(d);
    }

    /**
     * Parses a location from args.
     */
    public Location argLocation(int startIndex) {
        if (args.length <= startIndex + 2) {
            return null;
        }
        String sx = arg(startIndex);
        String sy = arg(startIndex + 1);
        String sz = arg(startIndex + 2);
        if (sx == null || sy == null || sz == null) {
            return null;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(sx);
            y = Double.parseDouble(sy);
            z = Double.parseDouble(sz);
        } catch (NumberFormatException ex) {
            return null;
        }

        World world;
        String sworld = arg(startIndex + 3);
        if (sworld != null && !sworld.isEmpty()) {
            world = Bukkit.getWorld(sworld);
        } else if (sender instanceof Player) {
            world = ((Player) sender).getWorld();
        } else {
            world = null;
        }

        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z);
    }
}
