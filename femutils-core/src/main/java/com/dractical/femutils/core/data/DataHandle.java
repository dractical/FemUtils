package com.dractical.femutils.core.data;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Mutable, reloadable view over a persisted payload. Mirrored after
 * {@link com.dractical.femutils.core.config.ConfigHandle} for familiarity.
 */
@SuppressWarnings("unused")
public final class DataHandle<T> implements Closeable {
    private final DataRef ref;
    private final Class<T> type;
    private final Supplier<T> defaults;
    private final Engine engine;
    private final List<Consumer<T>> listeners = new CopyOnWriteArrayList<>();
    private volatile T value;

    public DataHandle(DataRef ref, Class<T> type, Supplier<T> defaults, Engine engine) throws IOException {
        this.ref = Objects.requireNonNull(ref, "ref");
        this.type = Objects.requireNonNull(type, "type");
        this.defaults = Objects.requireNonNull(defaults, "defaults");
        this.engine = Objects.requireNonNull(engine, "engine");
        this.value = engine.load(ref, type, defaults);
    }

    public DataRef ref() {
        return ref;
    }

    public T get() {
        return value;
    }

    public T reload() throws IOException {
        T newVal = engine.load(ref, type, defaults);
        this.value = newVal;
        for (Consumer<T> l : listeners) l.accept(newVal);
        return newVal;
    }

    public boolean exists() throws IOException {
        return engine.exists(ref);
    }

    public void save() throws IOException {
        engine.save(ref, value);
    }

    public void setAndSave(T newValue) throws IOException {
        this.value = newValue;
        save();
    }

    public void delete() throws IOException {
        engine.delete(ref);
        this.value = defaults.get();
    }

    public void onReload(Consumer<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void close() throws IOException {
        engine.close();
    }

    public interface Engine extends Closeable {
        <T> T load(DataRef ref, Class<T> type, Supplier<T> defaults) throws IOException;

        void save(DataRef ref, Object value) throws IOException;

        boolean exists(DataRef ref) throws IOException;

        void delete(DataRef ref) throws IOException;
    }
}
