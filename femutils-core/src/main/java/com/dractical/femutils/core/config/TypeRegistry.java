package com.dractical.femutils.core.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TypeRegistry {
    private final Map<Class<?>, TypeSerializer<?>> exact = new LinkedHashMap<>();
    private final Map<Class<?>, TypeSerializer<?>> cache = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, TypeSerializer<T> ser) {
        exact.put(type, ser);
        cache.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> TypeSerializer<T> find(Class<T> type) {
        TypeSerializer<?> hit = cache.get(type);
        if (hit != null) return (TypeSerializer<T>) hit;

        TypeSerializer<?> ser = exact.get(type);
        if (ser != null) {
            cache.put(type, ser);
            return (TypeSerializer<T>) ser;
        }
        for (Map.Entry<Class<?>, TypeSerializer<?>> e : exact.entrySet()) {
            if (e.getKey().isAssignableFrom(type)) {
                cache.put(type, e.getValue());
                return (TypeSerializer<T>) e.getValue();
            }
        }
        return null;
    }
}
