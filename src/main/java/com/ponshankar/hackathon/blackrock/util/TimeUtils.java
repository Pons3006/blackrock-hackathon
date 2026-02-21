package com.ponshankar.hackathon.blackrock.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class TimeUtils {

    public static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimeUtils() {}

    /**
     * Parses a timestamp string to epoch seconds (UTC).
     * Returns null if the format is invalid.
     */
    public static Long toEpochSeconds(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(timestamp.trim(), TIMESTAMP_FORMAT);
            return ldt.toEpochSecond(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Formats epoch seconds back to the canonical timestamp string.
     */
    public static String fromEpochSeconds(long epochSeconds) {
        return LocalDateTime.ofEpochSecond(epochSeconds, 0, ZoneOffset.UTC)
                .format(TIMESTAMP_FORMAT);
    }
}
