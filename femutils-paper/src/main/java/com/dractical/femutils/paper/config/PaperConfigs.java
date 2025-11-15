package com.dractical.femutils.paper.config;

import com.dractical.femutils.core.config.ConfigHandle;
import com.dractical.femutils.core.config.TypeRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class PaperConfigs {
    private final JavaPlugin plugin;
    private final TypeRegistry registry = new TypeRegistry();
    private final SimpleYamlEngine engine;

    public PaperConfigs(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        PaperSerializers.registerAll(registry);
        this.engine = new SimpleYamlEngine(registry);
    }

    public TypeRegistry registry() {
        return registry;
    }

    public <T> ConfigHandle<T> create(String fileName, Class<T> type, Supplier<T> defaults) throws IOException {
        Path path = plugin.getDataFolder().toPath().resolve(fileName);
        if (Files.notExists(path)) {
            //noinspection ResultOfMethodCallIgnored
            plugin.getDataFolder().mkdirs();
            try (InputStream in = plugin.getResource(fileName)) {
                if (in != null) Files.copy(in, path);
            }
        }
        return new ConfigHandle<>(path, type, defaults, engine);
    }
}
