package com.dractical.femutils.core.text;

import com.dractical.femutils.core.check.Checks;

import java.util.Arrays;
import java.util.Iterator;

/**
 * String helpers.
 */
@SuppressWarnings("unused")
public final class StringUtils {
    private static final String ELLIPSIS = "…";

    private StringUtils() {
        throw new AssertionError("No " + StringUtils.class.getName() + " instances");
    }

    /**
     * Joins String parts with a delimiter.
     * Null elements become "null".
     */
    public static String join(CharSequence delimiter, String... parts) {
        Checks.notNull(delimiter, "delimiter");
        Checks.notNull(parts, "parts");
        final int n = parts.length;
        if (n == 0) return "";
        if (n == 1) return String.valueOf(parts[0]);

        final int delimLen = delimiter.length();
        int total = (n - 1) * delimLen;
        for (String p : parts) {
            total += (p == null ? 4 : p.length());
        }

        StringBuilder sb = new StringBuilder(total);
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    /**
     * Joins arbitrary objects.
     */
    public static String join(CharSequence delimiter, Iterable<?> parts) {
        Checks.notNull(delimiter, "delimiter");
        Checks.notNull(parts, "parts");
        Iterator<?> it = parts.iterator();
        if (!it.hasNext()) return "";
        StringBuilder sb = new StringBuilder(32);
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(delimiter).append(it.next());
        }
        return sb.toString();
    }

    /**
     * Char delimiter for String arrays.
     */
    public static String join(char delimiter, String... parts) {
        Checks.notNull(parts, "parts");
        final int n = parts.length;
        if (n == 0) return "";
        if (n == 1) return String.valueOf(parts[0]);

        int total = (n - 1);
        for (String p : parts) {
            total += (p == null ? 4 : p.length());
        }

        StringBuilder sb = new StringBuilder(total);
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    /**
     * Result container for splitOnce
     */
    public static final class Split {
        public final String left;
        public final String right;
        public final boolean found;

        private Split(String left, String right, boolean found) {
            this.left = left;
            this.right = right;
            this.found = found;
        }
    }

    /**
     * Splits on the first occurrence of the delimiter char.
     */
    public static Split splitOnce(String s, char delimiter) {
        Checks.notNull(s, "s");
        int i = s.indexOf(delimiter);
        if (i < 0) return new Split(s, null, false);
        return new Split(s.substring(0, i), s.substring(i + 1), true);
    }

    /**
     * Splits on the first occurrence of a non-empty delimiter string.
     */
    public static Split splitOnce(String s, String delimiter) {
        Checks.notNull(s, "s");
        Checks.notBlank(delimiter, "delimiter");
        int i = s.indexOf(delimiter);
        if (i < 0) return new Split(s, null, false);
        return new Split(s.substring(0, i), s.substring(i + delimiter.length()), true);
    }

    /**
     * Truncate to max characters. If truncated, append the suffix.
     * If suffix length > max, returns suffix.substring(0, max).
     */
    public static String truncate(String s, int max, String suffix) {
        Checks.notNull(s, "s");
        if (max < 0) throw new IllegalArgumentException("max must be >= 0");
        if (s.length() <= max) return s;

        String suf = (suffix == null) ? "" : suffix;
        int sufLen = suf.length();
        if (sufLen >= max) {
            return sufLen == max ? suf : suf.substring(0, max);
        }
        return s.substring(0, max - sufLen) + suf;
    }

    /**
     * Truncate with a single-character ellipsis (U+2026).
     */
    public static String truncate(String s, int max) {
        return truncate(s, max, ELLIPSIS);
    }

    public static String padLeft(String s, int width, char pad) {
        Checks.notNull(s, "s");
        if (width < 0) throw new IllegalArgumentException("width must be >= 0");
        int len = s.length();
        if (len >= width) return s;

        int pads = width - len;
        char[] out = new char[width];
        for (int i = 0; i < pads; i++) out[i] = pad;
        s.getChars(0, len, out, pads);
        return new String(out);
    }

    public static String padRight(String s, int width, char pad) {
        Checks.notNull(s, "s");
        if (width < 0) throw new IllegalArgumentException("width must be >= 0");
        int len = s.length();
        if (len >= width) return s;

        int pads = width - len;
        char[] out = new char[width];
        s.getChars(0, len, out, 0);
        for (int i = len; i < width; i++) out[i] = pad;
        return new String(out);
    }

    /**
     * snake_case -> lowerCamelCase
     */
    public static String snakeToLowerCamel(String s) {
        Checks.notNull(s, "s");
        int n = s.length();
        if (n == 0) return s;

        char[] out = new char[n];
        int o = 0;
        boolean upcaseNext = false;
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c == '_') {
                upcaseNext = (o != 0);
                continue;
            }
            if (upcaseNext) {
                out[o++] = Character.toUpperCase(c);
                upcaseNext = false;
            } else {
                out[o++] = Character.toLowerCase(c);
            }
        }
        return new String(out, 0, o);
    }

    /**
     * snake_case -> UpperCamelCase
     */
    public static String snakeToUpperCamel(String s) {
        String lowerCamel = snakeToLowerCamel(s);
        if (lowerCamel.isEmpty()) return lowerCamel;
        char first = lowerCamel.charAt(0);
        if (Character.isUpperCase(first)) return lowerCamel;
        char[] chars = lowerCamel.toCharArray();
        chars[0] = Character.toUpperCase(first);
        return new String(chars);
    }

    /**
     * camelCase / PascalCase -> snake_case.
     */
    public static String camelToSnake(String s) {
        Checks.notNull(s, "s");
        int n = s.length();
        if (n == 0) return s;

        StringBuilder sb = new StringBuilder(n + n / 2);
        char prev = 0;
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            boolean isUpper = Character.isUpperCase(c);
            if (i > 0) {
                char next = (i + 1 < n) ? s.charAt(i + 1) : 0;
                boolean boundary = (isUpper && Character.isLowerCase(prev)) || (isUpper && Character.isUpperCase(prev) && Character.isLowerCase(next));
                if (boundary && !sb.isEmpty() && sb.charAt(sb.length() - 1) != '_') {
                    sb.append('_');
                }
            }
            sb.append(Character.toLowerCase(c));
            prev = c;
        }
        return sb.toString();
    }

    /**
     * Returns true if null, empty, or only whitespace.
     */
    public static boolean isBlank(CharSequence cs) {
        if (cs == null) return true;
        int n = cs.length();
        for (int i = 0; i < n; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) return false;
        }
        return true;
    }

    /**
     * Non-throwing substring. Clamps to [0, s.length()].
     */
    public static String safeSubstring(String s, int start, int end) {
        Checks.notNull(s, "s");
        int n = s.length();
        int from = Math.max(0, Math.min(start, n));
        int to = Math.max(from, Math.max(0, Math.min(end, n)));
        if (from == 0 && to == n) return s;
        return s.substring(from, to);
    }

    /**
     * Simple char repetition.
     */
    public static String repeat(char c, int count) {
        if (count <= 0) return "";
        char[] out = new char[count];
        Arrays.fill(out, c);
        return new String(out);
    }
}
