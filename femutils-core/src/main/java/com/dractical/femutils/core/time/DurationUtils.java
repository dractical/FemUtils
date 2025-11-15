package com.dractical.femutils.core.time;

import com.dractical.femutils.core.check.Checks;

import java.time.Duration;

/**
 * Helpers for working with java.time.Duration.
 */
@SuppressWarnings("unused")
public final class DurationUtils {

    private DurationUtils() {
        throw new AssertionError("No " + DurationUtils.class.getName() + " instances");
    }

    public static Duration ofMillis(long millis) {
        return Duration.ofMillis(millis);
    }

    public static Duration ofSeconds(long seconds) {
        return Duration.ofSeconds(seconds);
    }

    public static long toMillis(Duration d) {
        Checks.notNull(d, "d");
        return d.toMillis();
    }

    public static long toSeconds(Duration d) {
        Checks.notNull(d, "d");
        return d.getSeconds();
    }

    public static boolean isZeroOrNegative(Duration d) {
        Checks.notNull(d, "d");
        return d.isNegative() || d.isZero();
    }

    public static Duration clamp(Duration d, Duration min, Duration max) {
        Checks.notNull(d, "d");
        Checks.notNull(min, "min");
        Checks.notNull(max, "max");
        if (d.compareTo(min) < 0) return min;
        if (d.compareTo(max) > 0) return max;
        return d;
    }

    /**
     * Human-oriented format.
     */
    public static String prettyPrint(Duration duration) {
        Checks.notNull(duration, "duration");

        if (duration.isZero()) {
            return "0s";
        }

        boolean negative = duration.isNegative();
        Duration d = duration.abs();

        long totalMillis = d.toMillis();

        long days = totalMillis / (24L * 60L * 60L * 1000L);
        totalMillis -= days * 24L * 60L * 60L * 1000L;

        long hours = totalMillis / (60L * 60L * 1000L);
        totalMillis -= hours * 60L * 60L * 1000L;

        long minutes = totalMillis / (60L * 1000L);
        totalMillis -= minutes * 60L * 1000L;

        long seconds = totalMillis / 1000L;
        totalMillis -= seconds * 1000L;

        long millis = totalMillis;

        StringBuilder sb = new StringBuilder(24);
        if (negative) {
            sb.append('-');
        }

        boolean started = false;

        if (days != 0) {
            sb.append(days).append('d');
            started = true;
        }
        if (hours != 0) {
            if (started) sb.append(' ');
            sb.append(hours).append('h');
            started = true;
        }
        if (minutes != 0) {
            if (started) sb.append(' ');
            sb.append(minutes).append('m');
            started = true;
        }
        if (seconds != 0) {
            if (started) sb.append(' ');
            sb.append(seconds).append('s');
            started = true;
        }
        if (millis != 0 || !started) {
            if (started) sb.append(' ');
            sb.append(millis).append("ms");
        }

        return sb.toString();
    }

    /**
     * Coarser pretty print.
     */
    public static String prettyApprox(Duration duration) {
        Checks.notNull(duration, "duration");
        if (duration.isZero()) return "0s";

        boolean negative = duration.isNegative();
        Duration d = duration.abs();
        long millis = d.toMillis();

        long days = millis / (24L * 60L * 60L * 1000L);
        if (days > 0) {
            return prefixSign(negative, days + "d");
        }

        long hours = millis / (60L * 60L * 1000L);
        if (hours > 0) {
            return prefixSign(negative, hours + "h");
        }

        long minutes = millis / (60L * 1000L);
        if (minutes > 0) {
            return prefixSign(negative, minutes + "m");
        }

        long seconds = (millis + 500L) / 1000L;
        if (seconds > 0) {
            return prefixSign(negative, seconds + "s");
        }

        return prefixSign(negative, millis + "ms");
    }

    private static String prefixSign(boolean negative, String s) {
        return negative ? "-" + s : s;
    }
}
