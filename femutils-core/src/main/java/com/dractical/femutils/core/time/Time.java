package com.dractical.femutils.core.time;

import com.dractical.femutils.core.check.Checks;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Small helpers around java.time.
 */
@SuppressWarnings("unused")
public final class Time {
    public static final ZoneId UTC = ZoneOffset.UTC;
    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;
    private static final DateTimeFormatter ISO_OFFSET_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private Time() {
        throw new AssertionError("No " + Time.class.getName() + " instances");
    }

    /**
     * Current Instant in UTC.
     */
    public static Instant nowUtc() {
        return Instant.now();
    }

    /**
     * Current epoch millis in UTC.
     */
    public static long nowUtcMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Current time in the system default zone.
     */
    public static ZonedDateTime nowSystem() {
        return ZonedDateTime.now();
    }

    /**
     * Current time in a specific zone.
     */
    public static ZonedDateTime now(ZoneId zone) {
        Checks.notNull(zone, "zone");
        return ZonedDateTime.now(zone);
    }

    /**
     * Format Instant as ISO-8601 UTC string (like 2025-11-14T12:34:56Z, for example).
     */
    public static String formatIsoInstant(Instant instant) {
        Checks.notNull(instant, "instant");
        return ISO_INSTANT.format(instant);
    }

    /**
     * Parse ISO-8601 instant string to Instant.
     */
    public static Instant parseIsoInstant(String text) {
        Checks.notBlank(text, "text");
        return Instant.parse(text);
    }

    /**
     * Format OffsetDateTime as ISO-8601 with offset (such as 2025-11-14T12:34:56+01:00).
     */
    public static String formatIsoOffset(OffsetDateTime odt) {
        Checks.notNull(odt, "odt");
        return ISO_OFFSET_DATE_TIME.format(odt);
    }

    /**
     * Parse ISO-8601 offset date-time string to OffsetDateTime.
     */
    public static OffsetDateTime parseIsoOffset(String text) {
        Checks.notBlank(text, "text");
        return OffsetDateTime.parse(text, ISO_OFFSET_DATE_TIME);
    }

    /**
     * Format LocalDateTime as ISO-8601 (no zone).
     */
    public static String formatIsoLocal(LocalDateTime ldt) {
        Checks.notNull(ldt, "ldt");
        return ISO_LOCAL_DATE_TIME.format(ldt);
    }

    /**
     * Parse ISO-8601 local date-time (no zone) to LocalDateTime.
     */
    public static LocalDateTime parseIsoLocal(String text) {
        Checks.notBlank(text, "text");
        return LocalDateTime.parse(text, ISO_LOCAL_DATE_TIME);
    }

    /**
     * Convert Instant to epoch millis.
     */
    public static long toEpochMillis(Instant instant) {
        Checks.notNull(instant, "instant");
        return instant.toEpochMilli();
    }

    /**
     * Convert ZonedDateTime to epoch millis.
     */
    public static long toEpochMillis(ZonedDateTime zdt) {
        Checks.notNull(zdt, "zdt");
        return zdt.toInstant().toEpochMilli();
    }

    /**
     * Convert OffsetDateTime to epoch millis.
     */
    public static long toEpochMillis(OffsetDateTime odt) {
        Checks.notNull(odt, "odt");
        return odt.toInstant().toEpochMilli();
    }

    /**
     * Convert epoch millis to Instant.
     */
    public static Instant fromEpochMillis(long millis) {
        return Instant.ofEpochMilli(millis);
    }

    /**
     * Convert epoch millis to ZonedDateTime in the given zone.
     */
    public static ZonedDateTime fromEpochMillis(long millis, ZoneId zone) {
        Checks.notNull(zone, "zone");
        return Instant.ofEpochMilli(millis).atZone(zone);
    }

    /**
     * Duration between two Instants.
     */
    public static Duration between(Instant start, Instant end) {
        Checks.notNull(start, "start");
        Checks.notNull(end, "end");
        return Duration.between(start, end);
    }

    /**
     * Duration since the given Instant until nowUtc().
     */
    public static Duration since(Instant past) {
        return between(Checks.notNull(past, "past"), nowUtc());
    }

    /**
     * Duration from nowUtc() until the given Instant (can be negative).
     */
    public static Duration until(Instant future) {
        return between(nowUtc(), Checks.notNull(future, "future"));
    }

    /**
     * Truncate an Instant to whole seconds (no nanos).
     */
    public static Instant truncateToSeconds(Instant instant) {
        Checks.notNull(instant, "instant");
        long seconds = instant.getEpochSecond();
        return Instant.ofEpochSecond(seconds);
    }

    /**
     * Truncate an Instant to whole minutes (zero seconds/nanos).
     */
    public static Instant truncateToMinutes(Instant instant) {
        Checks.notNull(instant, "instant");
        long seconds = instant.getEpochSecond();
        long minutes = seconds / 60L;
        return Instant.ofEpochSecond(minutes * 60L);
    }
}
