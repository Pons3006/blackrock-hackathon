/**
 * Test type: Unit test
 * Validation: TimeUtils — epoch second conversion, round-trip parsing, null/blank/malformed input handling
 * Command: mvn test -Dtest=TimeUtilsTest
 */
package com.ponshankar.hackathon.blackrock.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimeUtilsTest {

    @Test
    void toEpochSeconds_validTimestamp() {
        Long epoch = TimeUtils.toEpochSeconds("2024-01-15 10:30:00");
        assertNotNull(epoch);
        assertEquals("2024-01-15 10:30:00", TimeUtils.fromEpochSeconds(epoch));
    }

    @Test
    void toEpochSeconds_unixEpoch() {
        Long epoch = TimeUtils.toEpochSeconds("1970-01-01 00:00:00");
        assertNotNull(epoch);
        assertEquals(0L, epoch);
    }

    @Test
    void toEpochSeconds_nullInput() {
        assertNull(TimeUtils.toEpochSeconds(null));
    }

    @Test
    void toEpochSeconds_blankInput() {
        assertNull(TimeUtils.toEpochSeconds(""));
        assertNull(TimeUtils.toEpochSeconds("   "));
    }

    @Test
    void toEpochSeconds_malformedInput() {
        assertNull(TimeUtils.toEpochSeconds("not-a-date"));
        assertNull(TimeUtils.toEpochSeconds("2024-13-01 00:00:00"));
        assertNull(TimeUtils.toEpochSeconds("2024/01/15 10:30:00"));
    }

    @Test
    void toEpochSeconds_dateOnly() {
        Long epoch = TimeUtils.toEpochSeconds("2024-01-15");
        assertNotNull(epoch);
        assertEquals("2024-01-15 00:00:00", TimeUtils.fromEpochSeconds(epoch));
    }

    @Test
    void toEpochSeconds_dateOnly_unixEpoch() {
        Long epoch = TimeUtils.toEpochSeconds("1970-01-01");
        assertNotNull(epoch);
        assertEquals(0L, epoch);
    }

    @Test
    void fromEpochSeconds_knownValue() {
        // 2024-01-01 00:00:00 UTC = 1704067200
        assertEquals("2024-01-01 00:00:00", TimeUtils.fromEpochSeconds(1704067200L));
    }

    @Test
    void roundTrip_multipleTimestamps() {
        String[] timestamps = {
                "2023-06-15 08:45:30",
                "2000-12-31 23:59:59",
                "2025-03-01 00:00:01"
        };
        for (String ts : timestamps) {
            Long epoch = TimeUtils.toEpochSeconds(ts);
            assertNotNull(epoch, "Failed to parse: " + ts);
            assertEquals(ts, TimeUtils.fromEpochSeconds(epoch), "Round-trip failed for: " + ts);
        }
    }

    @Test
    void toEpochSeconds_trims_whitespace() {
        Long epoch = TimeUtils.toEpochSeconds("  2024-01-15 10:30:00  ");
        assertNotNull(epoch);
        assertEquals("2024-01-15 10:30:00", TimeUtils.fromEpochSeconds(epoch));
    }
}
