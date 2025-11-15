package com.dractical.femutils.core.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class ConfigHandle<T> {
    private final Path path;
    private final Class<T> type;
    private final Supplier<T> defaults;
    private final Engine engine;
    private final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();
    private volatile T value;

    public ConfigHandle(Path path, Class<T> type, Supplier<T> defaults, Engine engine) throws IOException {
        this.path = path;
        this.type = type;
        this.defaults = defaults;
        this.engine = engine;
        this.value = engine.load(path, type, defaults);
    }

    public T get() {
        return value;
    }

    public void reload() throws IOException {
        T newVal = engine.load(path, type, defaults);
        this.value = newVal;
        for (Consumer<T> l : listeners) l.accept(newVal);
    }

    public void save() throws IOException {
        engine.save(path, value);
    }

    public void setAndSave(T newValue) throws IOException {
        this.value = newValue;
        save();
    }

    public void onReload(Consumer<T> listener) {
        listeners.add(listener);
    }

    public Path path() {
        return path;
    }

    public interface Engine {
        <T> T load(Path path, Class<T> type, Supplier<T> defaults) throws IOException;

        void save(Path path, Object value) throws IOException;
    }
}
