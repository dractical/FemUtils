package com.dractical.femutils.paper.command;

import com.dractical.femutils.core.check.Checks;
import com.dractical.femutils.core.result.Result;
import com.dractical.femutils.paper.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * Builder for Paper/Bukkit commands.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class CommandBuilder {

    private final String name;
    private final List<String> aliases = new ArrayList<>();

    private String permission;
    private boolean playerOnly;
    private boolean consoleOnly;

    private String description;
    private String usage;

    private String noPermissionMessage;
    private String playersOnlyMessage;
    private String consoleOnlyMessage;
    private String invalidUsageMessage;

    private CommandAction executor;
    private TabCompleter tabCompleter;
    private final List<Subcommand> subcommands = new ArrayList<>();
    private final Map<String, Subcommand> subcommandLookup = new HashMap<>();

    CommandBuilder(String name) {
        this.name = Checks.notBlank(name, "name");
    }

    public CommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public CommandBuilder playerOnly() {
        this.playerOnly = true;
        this.consoleOnly = false;
        return this;
    }

    public CommandBuilder consoleOnly() {
        this.consoleOnly = true;
        this.playerOnly = false;
        return this;
    }

    public CommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public CommandBuilder usage(String usage) {
        this.usage = usage;
        return this;
    }

    public CommandBuilder aliases(String... aliases) {
        if (aliases != null) {
            Collections.addAll(this.aliases, aliases);
        }
        return this;
    }

    public CommandBuilder noPermissionMessage(String message) {
        this.noPermissionMessage = message;
        return this;
    }

    public CommandBuilder playersOnlyMessage(String message) {
        this.playersOnlyMessage = message;
        return this;
    }

    public CommandBuilder consoleOnlyMessage(String message) {
        this.consoleOnlyMessage = message;
        return this;
    }

    public CommandBuilder invalidUsageMessage(String message) {
        this.invalidUsageMessage = message;
        return this;
    }

    public CommandBuilder exec(Consumer<CommandContext> executor) {
        Checks.notNull(executor, "executor");
        this.executor = ctx -> {
            executor.accept(ctx);
            return Result.ok(null);
        };
        return this;
    }

    public CommandBuilder execResult(CommandAction executor) {
        this.executor = Checks.notNull(executor, "executor");
        return this;
    }

    public CommandBuilder tab(TabCompleter completer) {
        this.tabCompleter = completer;
        return this;
    }

    public CommandBuilder tabDsl(Consumer<PaperTabCompleter.Builder> dsl) {
        Checks.notNull(dsl, "dsl");
        PaperTabCompleter.Builder builder = new PaperTabCompleter.Builder();
        dsl.accept(builder);
        this.tabCompleter = builder.build();
        return this;
    }

    public CommandBuilder subcommand(SubcommandBuilder builder) {
        Checks.notNull(builder, "builder");
        Subcommand subcommand = builder.build();
        registerSubcommand(subcommand);
        return this;
    }

    public CommandBuilder subcommands(SubcommandBuilder... builders) {
        if (builders != null) {
            for (SubcommandBuilder builder : builders) {
                if (builder != null) {
                    subcommand(builder);
                }
            }
        }
        return this;
    }

    /**
     * Registers this command with Bukkit.
     * IMPORTANT: You still have to define the base command in plugin.yml.
     */
    public void register(Plugin plugin) {
        Checks.notNull(plugin, "plugin");
        Checks.state(executor != null || !subcommands.isEmpty(), "No executor or subcommands registered for /" + name);

        PluginCommand cmd = Bukkit.getServer().getPluginCommand(name);
        if (cmd == null) {
            throw new IllegalStateException("PluginCommand '" + name + "' is not defined in plugin.yml");
        }

        if (description != null) {
            cmd.setDescription(description);
        }
        if (usage != null) {
            cmd.setUsage("/" + name + " " + usage);
        }
        if (!aliases.isEmpty()) {
            cmd.setAliases(aliases);
        }

        InternalExecutor internal = new InternalExecutor(plugin, this);
        cmd.setExecutor(internal);
        cmd.setTabCompleter(tabCompleter != null ? tabCompleter : internal);
    }

    private void registerSubcommand(Subcommand subcommand) {
        subcommands.add(subcommand);
        subcommandLookup.put(subcommand.name.toLowerCase(Locale.ROOT), subcommand);
        for (String alias : subcommand.aliases) {
            subcommandLookup.put(alias.toLowerCase(Locale.ROOT), subcommand);
        }
    }

    private static final class InternalExecutor implements CommandExecutor, TabCompleter {

        private final Plugin plugin;
        private final CommandBuilder meta;

        InternalExecutor(Plugin plugin, CommandBuilder meta) {
            this.plugin = plugin;
            this.meta = meta;
        }

        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
            if (meta.permission != null && !meta.permission.isEmpty() && !sender.hasPermission(meta.permission)) {
                CommandMessages.sendNoPermission(sender, meta.noPermissionMessage);
                return true;
            }

            if (meta.playerOnly && !(sender instanceof Player)) {
                CommandMessages.sendPlayersOnly(sender, meta.playersOnlyMessage);
                return true;
            }
            if (meta.consoleOnly && sender instanceof Player) {
                CommandMessages.sendConsoleOnly(sender, meta.consoleOnlyMessage);
                return true;
            }

            CommandContext ctx = new CommandContext(
                    plugin,
                    sender,
                    command,
                    label,
                    args,
                    meta.invalidUsageMessage,
                    meta.usage
            );

            try {
                Result<Void> result = dispatch(ctx);
                handleResult(sender, result);
            } catch (Throwable t) {
                plugin.getLogger().severe("Unhandled exception in command /" + label + ": " + t.getMessage());
                //noinspection CallToPrintStackTrace
                t.printStackTrace();
                Lang.send(sender, "<red>An internal error occurred while executing this command.</red>");
            }
            return true;
        }

        private Result<Void> dispatch(CommandContext ctx) {
            if (!meta.subcommands.isEmpty()) {
                Result<Void> subResult = trySubcommand(ctx);
                if (subResult != null) {
                    return subResult;
                }
                if (meta.executor == null) {
                    ctx.invalidUsage();
                    return Result.ok(null);
                }
            }
            return meta.executor != null ? meta.executor.execute(ctx) : Result.ok(null);
        }

        private Result<Void> trySubcommand(CommandContext ctx) {
            if (ctx.argsLength() == 0) {
                return null;
            }
            Subcommand subcommand = meta.subcommandLookup.get(ctx.arg(0).toLowerCase(Locale.ROOT));
            if (subcommand == null) {
                return null;
            }

            CommandSender sender = ctx.sender();
            if (subcommand.permission != null && !subcommand.permission.isEmpty() && !sender.hasPermission(subcommand.permission)) {
                CommandMessages.sendNoPermission(sender, meta.noPermissionMessage);
                return Result.ok(null);
            }
            if (subcommand.playerOnly && !(sender instanceof Player)) {
                CommandMessages.sendPlayersOnly(sender, meta.playersOnlyMessage);
                return Result.ok(null);
            }
            if (subcommand.consoleOnly && sender instanceof Player) {
                CommandMessages.sendConsoleOnly(sender, meta.consoleOnlyMessage);
                return Result.ok(null);
            }

            String[] newArgs = sliceArgs(ctx.args(), 1);
            CommandContext child = new CommandContext(
                    ctx.plugin(),
                    sender,
                    ctx.command(),
                    ctx.label(),
                    newArgs,
                    subcommand.invalidUsageMessage != null ? subcommand.invalidUsageMessage : meta.invalidUsageMessage,
                    subcommand.usage != null ? subcommand.usage : meta.usage
            );
            return subcommand.executor.execute(child);
        }

        private List<String> subcommandNames(CommandSender sender) {
            if (meta.subcommands.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> names = new ArrayList<>();
            for (Subcommand sub : meta.subcommands) {
                if (sub.permission == null || sub.permission.isEmpty() || sender.hasPermission(sub.permission)) {
                    names.add(sub.name);
                }
            }
            return names;
        }

        private void handleResult(CommandSender sender, Result<Void> result) {
            if (result == null || result.isOk()) {
                return;
            }
            Throwable error = result.errorOrNull();
            if (error instanceof CommandException ce) {
                Lang.send(sender, ce.component());
                return;
            }
            Lang.send(sender, "<red>An unexpected error occurred. Check console for details.</red>");
            if (error != null) {
                plugin.getLogger().severe("Command error: " + error.getMessage());
                //noinspection CallToPrintStackTrace
                error.printStackTrace();
            }
        }

        private String[] sliceArgs(String[] args, int fromIndex) {
            if (args == null || args.length <= fromIndex) {
                return new String[0];
            }
            String[] sliced = new String[args.length - fromIndex];
            System.arraycopy(args, fromIndex, sliced, 0, sliced.length);
            return sliced;
        }

        @Override
        public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
            if (meta.tabCompleter != null) {
                return meta.tabCompleter.onTabComplete(sender, command, alias, args);
            }
            if (!meta.subcommands.isEmpty()) {
                if (args.length <= 1) {
                    return filterPrefix(subcommandNames(sender), args.length == 0 ? "" : args[0]);
                }
                Subcommand sub = meta.subcommandLookup.get(args[0].toLowerCase(Locale.ROOT));
                if (sub != null && sub.tabCompleter != null) {
                    String[] shifted = sliceArgs(args, 1);
                    return sub.tabCompleter.onTabComplete(sender, command, alias, shifted);
                }
            }
            return Collections.emptyList();
        }

        private List<String> filterPrefix(List<String> values, String token) {
            if (values == null || values.isEmpty()) {
                return Collections.emptyList();
            }
            if (token == null || token.isEmpty()) {
                return values;
            }
            String lower = token.toLowerCase(Locale.ROOT);
            List<String> matches = new ArrayList<>();
            for (String value : values) {
                if (value.toLowerCase(Locale.ROOT).startsWith(lower)) {
                    matches.add(value);
                }
            }
            return matches;
        }
    }
}
