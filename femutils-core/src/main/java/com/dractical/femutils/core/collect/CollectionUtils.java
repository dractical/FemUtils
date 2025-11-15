package com.dractical.femutils.core.collect;

import com.dractical.femutils.core.check.Checks;

import java.util.*;

/**
 * Collection helpers.
 */
@SuppressWarnings("unused")
public final class CollectionUtils {

    private CollectionUtils() {
        throw new AssertionError("No " + CollectionUtils.class.getName() + " instances");
    }

    public static boolean isNullOrEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    public static boolean isNullOrEmpty(Map<?, ?> m) {
        return m == null || m.isEmpty();
    }

    /**
     * Returns an immutable list.
     */
    public static <T> List<T> immutableList(List<? extends T> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(source);
    }

    /**
     * Same semantics as {@link #immutableList(List)} but for any Collection.
     */
    public static <T> List<T> immutableList(Collection<? extends T> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(source);
    }

    /**
     * Returns an immutable set.
     */
    public static <T> Set<T> immutableSet(Set<? extends T> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptySet();
        }
        return Set.copyOf(source);
    }

    /**
     * Returns an immutable map.
     */
    public static <K, V> Map<K, V> immutableMap(Map<? extends K, ? extends V> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyMap();
        }
        return Map.copyOf(source);
    }

    /**
     * Defensive copy to a mutable List.
     */
    public static <T> List<T> copyToList(Collection<? extends T> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(source);
    }

    /**
     * Creates a new ArrayList with known capacity, and copies the elements if source is non-null.
     */
    public static <T> List<T> copyToList(Collection<? extends T> source, int expectedSize) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        int size = source.size();
        int capacity = Math.max(size, expectedSize);
        List<T> list = new ArrayList<>(capacity);
        list.addAll(source);
        return list;
    }

    /**
     * Returns the size of a collection or 0 if null.
     */
    public static int sizeOrZero(Collection<?> c) {
        return c == null ? 0 : c.size();
    }

    /**
     * Returns the size of a map or 0 if null.
     */
    public static int sizeOrZero(Map<?, ?> m) {
        return m == null ? 0 : m.size();
    }

    /**
     * Returns the first element of a collection or null if null/empty.
     */
    public static <T> T firstOrNull(Collection<T> c) {
        if (c == null || c.isEmpty()) {
            return null;
        }
        if (c instanceof List<?>) {
            return ((List<T>) c).getFirst();
        }
        return c.iterator().next();
    }

    /**
     * Makes sure the collection is not null.
     * Returns the original if non-null, or an empty and unmodifiable list if null.
     */
    public static <T> Collection<T> nullToEmpty(Collection<T> c) {
        return c == null ? Collections.emptyList() : c;
    }

    /**
     * Makes sure the map is not null.
     * Returns the original map if non-null, or an empty and unmodifiable map if null.
     */
    public static <K, V> Map<K, V> nullToEmpty(Map<K, V> m) {
        return m == null ? Collections.emptyMap() : m;
    }

    /**
     * Throws if collection is null or empty, and returns it otherwise.
     */
    public static <T extends Collection<?>> T requireNonEmpty(T c, String name) {
        Checks.notNull(c, name);
        if (c.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return c;
    }

    /**
     * Throws if map is null or empty, and returns it otherwise.
     */
    public static <T extends Map<?, ?>> T requireNonEmpty(T m, String name) {
        Checks.notNull(m, name);
        if (m.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return m;
    }
}
