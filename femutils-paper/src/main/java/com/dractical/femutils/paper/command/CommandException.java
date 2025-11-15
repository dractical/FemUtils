package com.dractical.femutils.paper.command;

import com.dractical.femutils.paper.lang.Lang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Arrays;

/**
 * Runtime exception that carries a MiniMessage formatted component.
 */
@SuppressWarnings("unused")
public final class CommandException extends RuntimeException {

    private final Component component;

    private CommandException(Component component) {
        super(component != null ? component.toString() : "CommandException");
        this.component = component;
    }

    public static CommandException mm(String template, TagResolver... resolvers) {
        return new CommandException(Lang.mm(template, resolvers));
    }

    public static CommandException component(Component component) {
        return new CommandException(component);
    }

    public Component component() {
        return component;
    }

    @Override
    public String toString() {
        return "CommandException{" + Arrays.toString(component == null ? new Object[0] : new Object[]{component}) + '}';
    }
}
