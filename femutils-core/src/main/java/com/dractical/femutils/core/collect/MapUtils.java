package com.dractical.femutils.core.collect;

import com.dractical.femutils.core.check.Checks;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Map helpers.
 */
@SuppressWarnings("unused")
public final class MapUtils {

    private MapUtils() {
        throw new AssertionError("No " + MapUtils.class.getName() + " instances");
    }

    /**
     * Null-safe variant of Map.getOrDefault.
     */
    public static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        if (map == null) {
            return defaultValue;
        }
        if (!map.containsKey(key)) {
            return defaultValue;
        }
        return map.get(key);
    }

    /**
     * Lazy default computation.
     */
    public static <K, V> V getOrDefault(Map<K, V> map, K key, Supplier<? extends V> defaultSupplier) {
        Checks.notNull(defaultSupplier, "defaultSupplier");
        if (map == null || !map.containsKey(key)) {
            return defaultSupplier.get();
        }
        return map.get(key);
    }

    /**
     * Like Map.computeIfAbsent but null-safe for the map.
     */
    public static <K, V> V getOrDefaultCompute(Map<K, V> map, K key, Function<? super K, ? extends V> computer) {
        Checks.notNull(computer, "computer");

        if (map == null) {
            return computer.apply(key);
        }

        V existing = map.get(key);
        if (existing != null) {
            return existing;
        }

        V computed = computer.apply(key);
        if (computed != null) {
            map.put(key, computed);
        }
        return computed;
    }

    /**
     * Variant that *does* respect null values in the map:
     */
    public static <K, V> V getOrComputeIfAbsent(Map<K, V> map, K key, Function<? super K, ? extends V> computer) {
        Checks.notNull(computer, "computer");
        if (map == null) {
            return computer.apply(key);
        }

        V existing = map.get(key);
        if (existing != null || map.containsKey(key)) {
            return existing;
        }

        V computed = computer.apply(key);
        if (computed != null) {
            map.put(key, computed);
        }
        return computed;
    }

    /**
     * Null-safe putIfAbsent.
     */
    public static <K, V> V putIfAbsent(Map<K, V> map, K key, V value) {
        if (map == null) {
            return null;
        }
        return map.putIfAbsent(key, value);
    }

    /**
     * Null-safe "ensure" helper.
     * Throws if map is null. Returns the same map, for fluent chaining.
     */
    public static <M extends Map<?, ?>> M requireNonNull(M map, String name) {
        return Checks.notNull(map, name);
    }
}
