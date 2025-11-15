package com.dractical.femutils.core.config;

@SuppressWarnings("unused")
public interface TypeSerializer<T> {

    T deserialize(Object raw, ReflectMapper ctx, Class<T> type);

    Object serialize(T value, ReflectMapper ctx);
}
