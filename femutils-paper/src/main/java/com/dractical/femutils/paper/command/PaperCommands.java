package com.dractical.femutils.paper.command;

import com.dractical.femutils.core.check.Checks;
import com.dractical.femutils.core.result.Result;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.TabCompleter;

import java.util.function.Consumer;

/**
 * Entry point for building commands.
 */
@SuppressWarnings("unused")
public final class PaperCommands {

    private PaperCommands() {
        throw new AssertionError("No " + PaperCommands.class.getName() + " instances");
    }

    public static CommandBuilder command(String name) {
        return new CommandBuilder(name);
    }

    public static SubcommandBuilder subcommand(String name) {
        return new SubcommandBuilder(name);
    }

    public static PaperTabCompleter.Builder tabBuilder() {
        return new PaperTabCompleter.Builder();
    }

    public static TabCompleter tab(Consumer<PaperTabCompleter.Builder> dsl) {
        Checks.notNull(dsl, "dsl");
        PaperTabCompleter.Builder builder = tabBuilder();
        dsl.accept(builder);
        return builder.build();
    }

    public static Result<Void> success() {
        return Result.ok(null);
    }

    public static Result<Void> fail(String message, TagResolver... resolvers) {
        return Result.error(CommandException.mm(message, resolvers));
    }
}
