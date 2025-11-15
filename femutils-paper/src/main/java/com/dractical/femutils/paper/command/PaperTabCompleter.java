package com.dractical.femutils.paper.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * DSL driven {@link TabCompleter} implementation.
 */
@SuppressWarnings("unused")
public final class PaperTabCompleter implements TabCompleter {

    private final List<SuggestionRule> rules;

    private PaperTabCompleter(List<SuggestionRule> rules) {
        this.rules = rules;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        TabContext context = new TabContext(sender, command, alias, args);
        if (rules.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> suggestions = new LinkedHashSet<>();
        for (SuggestionRule rule : rules) {
            if (rule.matches(context)) {
                suggestions.addAll(rule.provide(context));
            }
        }
        if (suggestions.isEmpty()) {
            return Collections.emptyList();
        }
        String token = context.currentToken();
        if (token.isEmpty()) {
            return new ArrayList<>(suggestions);
        }

        List<String> filtered = new ArrayList<>();
        String lower = token.toLowerCase(Locale.ROOT);
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase(Locale.ROOT).startsWith(lower)) {
                filtered.add(suggestion);
            }
        }
        return filtered;
    }

    private record SuggestionRule(int argIndex, Function<TabContext, Collection<String>> provider) {
        boolean matches(TabContext context) {
            int current = Math.max(context.argsLength() - 1, 0);
            return argIndex == current || argIndex == -1;
        }

        Collection<String> provide(TabContext context) {
            Collection<String> result = provider.apply(context);
            return result != null ? result : Collections.emptyList();
        }
    }

    public static final class Builder {

        private final List<SuggestionRule> rules = new ArrayList<>();

        public Builder arg(int index, Function<TabContext, Collection<String>> provider) {
            Objects.requireNonNull(provider, "provider");
            this.rules.add(new SuggestionRule(index, provider));
            return this;
        }

        public Builder literal(int index, String... literals) {
            return arg(index, ctx -> {
                List<String> values = new ArrayList<>();
                if (literals != null) {
                    Collections.addAll(values, literals);
                }
                return values;
            });
        }

        public Builder players(int index) {
            return arg(index, ctx -> Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }

        public Builder enums(int index, Class<? extends Enum<?>> type) {
            Objects.requireNonNull(type, "type");
            return arg(index, ctx -> {
                Enum<?>[] constants = type.getEnumConstants();
                List<String> names = new ArrayList<>(constants.length);
                for (Enum<?> constant : constants) {
                    names.add(constant.name().toLowerCase(Locale.ROOT));
                }
                return names;
            });
        }

        public Builder worlds(int index) {
            return arg(index, ctx -> {
                List<String> names = new ArrayList<>();
                for (World world : Bukkit.getWorlds()) {
                    names.add(world.getName());
                }
                return names;
            });
        }

        public Builder any(Function<TabContext, Collection<String>> provider) {
            return arg(-1, provider);
        }

        public PaperTabCompleter build() {
            return new PaperTabCompleter(new ArrayList<>(rules));
        }
    }
}
