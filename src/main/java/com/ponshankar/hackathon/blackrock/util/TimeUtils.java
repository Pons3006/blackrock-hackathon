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
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Formats epoch seconds back to the canonical timestamp string.
     */
    public static String fromEpochSeconds(long epochSeconds) {
        // TODO: implement
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
