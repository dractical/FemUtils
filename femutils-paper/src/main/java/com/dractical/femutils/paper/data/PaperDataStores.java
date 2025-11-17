package com.dractical.femutils.paper.data;

import com.dractical.femutils.core.config.TypeRegistry;
import com.dractical.femutils.core.data.DataHandle;
import com.dractical.femutils.core.data.DataRef;
import com.dractical.femutils.core.data.MongoDataEngine;
import com.dractical.femutils.core.data.MySqlDataEngine;
import com.dractical.femutils.core.data.YamlDataEngine;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class PaperDataStores implements Closeable {
    private final JavaPlugin plugin;
    private final TypeRegistry registry = new TypeRegistry();
    private final YamlDataEngine yamlEngine;

    public PaperDataStores(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        PaperDataSerializers.registerAll(registry);
        this.yamlEngine = new YamlDataEngine(registry);
    }

    public TypeRegistry registry() {
        return registry;
    }

    public <T> DataHandle<T> yaml(String fileName, Class<T> type, Supplier<T> defaults) throws IOException {
        Path path = plugin.getDataFolder().toPath().resolve(fileName);
        ensureDataFolder();
        return new DataHandle<>(DataRef.path(path), type, defaults, yamlEngine);
    }

    public <T> DataHandle<T> mysql(Object key, Class<T> type, Supplier<T> defaults, MySqlDataEngine engine) throws IOException {
        Objects.requireNonNull(engine, "engine");
        return new DataHandle<>(DataRef.key(key), type, defaults, engine);
    }

    public <T> DataHandle<T> mongo(Object key, Class<T> type, Supplier<T> defaults, MongoDataEngine engine) throws IOException {
        Objects.requireNonNull(engine, "engine");
        return new DataHandle<>(DataRef.key(key), type, defaults, engine);
    }

    private void ensureDataFolder() throws IOException {
        if (!plugin.getDataFolder().exists()) {
            if (!plugin.getDataFolder().mkdirs()) {
                throw new IOException("Unable to create data folder for " + plugin.getName());
            }
        }
    }

    @Override
    public void close() {
        yamlEngine.close();
    }
}
