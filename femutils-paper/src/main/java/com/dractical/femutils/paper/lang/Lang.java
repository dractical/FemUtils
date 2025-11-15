package com.dractical.femutils.paper.lang;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.Objects;

@SuppressWarnings("unused")
public final class Lang {
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private Lang() {
    }

    public static Component mm(String template, TagResolver... resolvers) {
        Objects.requireNonNull(template, "template");
        return MM.deserialize(template, resolvers);
    }

    public static void send(Audience audience, Component component) {
        Objects.requireNonNull(audience, "audience");
        if (component != null) {
            audience.sendMessage(component);
        }
    }

    public static void send(CommandSender sender, Component component) {
        Objects.requireNonNull(sender, "sender");
        if (component != null) {
            sender.sendMessage(component);
        }
    }

    public static void send(Audience audience, String template, TagResolver... resolvers) {
        Objects.requireNonNull(audience, "audience");
        audience.sendMessage(mm(template, resolvers));
    }

    public static void send(CommandSender sender, String template, TagResolver... resolvers) {
        Objects.requireNonNull(sender, "sender");
        sender.sendMessage(mm(template, resolvers));
    }

    public static TagResolver placeholder(String key, Component value) {
        //noinspection PatternValidation
        return Placeholder.component(key, value);
    }

    public static TagResolver placeholder(String key, String value) {
        //noinspection PatternValidation
        return Placeholder.parsed(key, value);
    }

    public static MiniMessage mini() {
        return MM;
    }
}
