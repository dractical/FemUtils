package com.dractical.femutils.paper.command;

import com.dractical.femutils.core.check.Checks;
import com.dractical.femutils.core.result.Result;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for a single subcommand.
 */
@SuppressWarnings("unused")
public final class SubcommandBuilder {

    final String name;
    final List<String> aliases = new ArrayList<>();
    CommandAction executor;
    String permission;
    boolean playerOnly;
    boolean consoleOnly;
    String description;
    String usage;
    String invalidUsageMessage;
    TabCompleter tabCompleter;

    SubcommandBuilder(String name) {
        this.name = Checks.notBlank(name, "name");
    }

    public SubcommandBuilder aliases(String... aliases) {
        if (aliases != null) {
            Collections.addAll(this.aliases, aliases);
        }
        return this;
    }

    public SubcommandBuilder permission(String permission) {
        this.permission = permission;
        return this;
    }

    public SubcommandBuilder playerOnly() {
        this.playerOnly = true;
        this.consoleOnly = false;
        return this;
    }

    public SubcommandBuilder consoleOnly() {
        this.consoleOnly = true;
        this.playerOnly = false;
        return this;
    }

    public SubcommandBuilder description(String description) {
        this.description = description;
        return this;
    }

    public SubcommandBuilder usage(String usage) {
        this.usage = usage;
        return this;
    }

    public SubcommandBuilder invalidUsageMessage(String message) {
        this.invalidUsageMessage = message;
        return this;
    }

    public SubcommandBuilder exec(Consumer<CommandContext> executor) {
        Checks.notNull(executor, "executor");
        this.executor = ctx -> {
            executor.accept(ctx);
            return Result.ok(null);
        };
        return this;
    }

    public SubcommandBuilder execResult(CommandAction executor) {
        this.executor = Checks.notNull(executor, "executor");
        return this;
    }

    public SubcommandBuilder tab(TabCompleter completer) {
        this.tabCompleter = completer;
        return this;
    }

    public SubcommandBuilder tabDsl(Consumer<PaperTabCompleter.Builder> dsl) {
        Checks.notNull(dsl, "dsl");
        PaperTabCompleter.Builder builder = new PaperTabCompleter.Builder();
        dsl.accept(builder);
        this.tabCompleter = builder.build();
        return this;
    }

    Subcommand build() {
        Checks.notNull(executor, "executor");
        return new Subcommand(this);
    }
}
