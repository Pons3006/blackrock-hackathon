package com.ponshankar.hackathon.blackrock.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class TimeUtils {

    private static final Logger log = LoggerFactory.getLogger(TimeUtils.class);

    public static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter DATE_ONLY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private TimeUtils() {}

    /**
     * Parses a timestamp string to epoch seconds (UTC).
     * Accepts both "yyyy-MM-dd HH:mm:ss" and "yyyy-MM-dd" (treated as midnight).
     * Returns null if the format is invalid.
     */
    public static Long toEpochSeconds(String timestamp) {
        if (timestamp == null || timestamp.isBlank()) {
            return null;
        }
        String trimmed = timestamp.trim();
        try {
            LocalDateTime ldt = LocalDateTime.parse(trimmed, TIMESTAMP_FORMAT);
            return ldt.toEpochSecond(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            try {
                LocalDate ld = LocalDate.parse(trimmed, DATE_ONLY_FORMAT);
                return ld.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
            } catch (DateTimeParseException e2) {
                log.warn("Unparseable timestamp: '{}'", trimmed);
                return null;
            }
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
