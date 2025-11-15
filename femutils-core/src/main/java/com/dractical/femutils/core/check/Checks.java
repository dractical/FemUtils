package com.dractical.femutils.core.check;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Precondition and validation utilities.
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public final class Checks {

    private Checks() {
        throw new AssertionError("No " + Checks.class.getName() + " instances");
    }

    /**
     * Makes sure that the given value is not null.
     */
    public static <T> T notNull(T value, String name) {
        if (value == null) {
            throw new NullPointerException(name + " must not be null");
        }
        return value;
    }

    /**
     * Makes sure that the given value is not null.
     */
    public static <T> T notNull(T value) {
        if (value == null) {
            throw new NullPointerException("value must not be null");
        }
        return value;
    }

    /**
     * Makes sure that the given value is null.
     */
    public static void isNull(Object value, String name) {
        if (value != null) {
            throw new IllegalArgumentException(name + " must be null");
        }
    }

    /**
     * Makes sure that the given condition is true for an argument.
     */
    public static void argument(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Makes sure that the given condition is true for an argument.
     */
    public static void argument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("argument condition failed");
        }
    }

    /**
     * Makes sure that the given condition is true for the object state.
     */
    public static void state(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Makes sure that the given condition is true for the object state.
     */
    public static void state(boolean condition) {
        if (!condition) {
            throw new IllegalStateException("state condition failed");
        }
    }

    /**
     * Makes sure value >= 0.
     */
    public static int nonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + " must be >= 0 but was " + value);
        }
        return value;
    }

    public static long nonNegative(long value, String name) {
        if (value < 0L) {
            throw new IllegalArgumentException(name + " must be >= 0 but was " + value);
        }
        return value;
    }

    /**
     * Makes sure value > 0.
     */
    public static int positive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be > 0 but was " + value);
        }
        return value;
    }

    public static long positive(long value, String name) {
        if (value <= 0L) {
            throw new IllegalArgumentException(name + " must be > 0 but was " + value);
        }
        return value;
    }

    /**
     * Makes sure value is within [min, max] inclusive.
     */
    public static int inRange(int value, int min, int max, String name) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    name + " must be in range [" + min + ", " + max + "] but was " + value
            );
        }
        return value;
    }

    public static long inRange(long value, long min, long max, String name) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    name + " must be in range [" + min + ", " + max + "] but was " + value
            );
        }
        return value;
    }

    /**
     * Makes sure a valid index in [0, length).
     */
    public static int index(int index, int length, String name) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException(
                    name + " index " + index + " out of bounds for length " + length
            );
        }
        return index;
    }

    /**
     * Makes sure a valid range [fromIndex, toIndex) within [0, length].
     */
    public static void range(int fromIndex, int toIndex, int length, String name) {
        if (fromIndex < 0 || toIndex < fromIndex || toIndex > length) {
            throw new IndexOutOfBoundsException(
                    name + " range [" + fromIndex + ", " + toIndex + ") out of bounds for length " + length
            );
        }
    }

    /**
     * Makes sure a CharSequence is not null and not empty.
     */
    public static <T extends CharSequence> T notEmpty(T value, String name) {
        notNull(value, name);
        if (value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return value;
    }

    /**
     * Makes sure a CharSequence is not null, not empty, and not only whitespace.
     */
    public static <T extends CharSequence> T notBlank(T value, String name) {
        notNull(value, name);
        int len = value.length();
        boolean hasNonWhitespace = false;
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(value.charAt(i))) {
                hasNonWhitespace = true;
                break;
            }
        }
        if (!hasNonWhitespace) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    /**
     * Makes sure a collection is not null and not empty.
     */
    public static <T extends Collection<?>> T notEmpty(T collection, String name) {
        notNull(collection, name);
        if (collection.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return collection;
    }

    /**
     * Makes sure a map is not null and not empty.
     */
    public static <T extends Map<?, ?>> T notEmpty(T map, String name) {
        notNull(map, name);
        if (map.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
        return map;
    }

    /**
     * Makes sure that two objects are equal.
     */
    public static void equals(Object expected, Object actual, String name) {
        if (!Objects.equals(expected, actual)) {
            throw new IllegalArgumentException(name + " must be equal to " + expected + " but was " + actual);
        }
    }

    /**
     * Makes sure that two objects are not equal.
     */
    public static void notEquals(Object first, Object second, String name) {
        if (Objects.equals(first, second)) {
            throw new IllegalArgumentException(name + " must not be equal to " + second);
        }
    }

    /**
     * Marks a code path as unreachable.
     */
    public static RuntimeException unreachable() {
        return new IllegalStateException("Unreachable code reached");
    }

    /**
     * Marks a code path as unreachable with a custom message.
     */
    public static RuntimeException unreachable(String message) {
        return new IllegalStateException("Unreachable code: " + message);
    }
}
