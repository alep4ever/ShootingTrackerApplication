package com.example.application.data;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class BasketballStatsTest {

    @Test
    void defaultConstructor_setsRecordedAtToNow() {
        LocalDateTime before = LocalDateTime.now();
        BasketballStats stats = new BasketballStats();
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(stats.getRecordedAt());
        assertFalse(stats.getRecordedAt().isBefore(before));
        assertFalse(stats.getRecordedAt().isAfter(after));
        assertNull(stats.getPlayer());
        assertNull(stats.getTimeInSeconds());
        assertNull(stats.getShotsAttempted());
        assertNull(stats.getShotsMade());
    }

    @Test
    void parameterizedConstructor_setsAllFields() {
        SamplePerson player = new SamplePerson();
        LocalDateTime before = LocalDateTime.now();

        BasketballStats stats = new BasketballStats(player, 45.5, 10, 7);

        assertSame(player, stats.getPlayer());
        assertEquals(45.5, stats.getTimeInSeconds());
        assertEquals(10, stats.getShotsAttempted());
        assertEquals(7, stats.getShotsMade());
        assertNotNull(stats.getRecordedAt());
        assertFalse(stats.getRecordedAt().isBefore(before));
    }

    @Test
    void gettersAndSetters() {
        BasketballStats stats = new BasketballStats();
        SamplePerson player = new SamplePerson();
        LocalDateTime time = LocalDateTime.of(2026, 1, 2, 3, 4);

        stats.setPlayer(player);
        stats.setTimeInSeconds(30.0);
        stats.setShotsAttempted(20);
        stats.setShotsMade(15);
        stats.setRecordedAt(time);

        assertSame(player, stats.getPlayer());
        assertEquals(30.0, stats.getTimeInSeconds());
        assertEquals(20, stats.getShotsAttempted());
        assertEquals(15, stats.getShotsMade());
        assertEquals(time, stats.getRecordedAt());
    }

    // ---- getShootingPercentage ----

    @Test
    void shootingPercentage_normalCase() {
        assertEquals(70.0, new BasketballStats(null, 1.0, 10, 7).getShootingPercentage(), 0.0001);
    }

    @Test
    void shootingPercentage_allMade() {
        assertEquals(100.0, new BasketballStats(null, 1.0, 5, 5).getShootingPercentage(), 0.0001);
    }

    @Test
    void shootingPercentage_noneMade() {
        assertEquals(0.0, new BasketballStats(null, 1.0, 5, 0).getShootingPercentage(), 0.0001);
    }

    @Test
    void shootingPercentage_fractionalResult() {
        assertEquals(100.0 / 3.0, new BasketballStats(null, 1.0, 3, 1).getShootingPercentage(), 0.0001);
    }

    @Test
    void shootingPercentage_zeroAttemptsReturnsZero() {
        assertEquals(0.0, new BasketballStats(null, 1.0, 0, 0).getShootingPercentage());
    }

    @Test
    void shootingPercentage_nullAttemptsReturnsZero() {
        assertEquals(0.0, new BasketballStats(null, 1.0, null, null).getShootingPercentage());
    }
}
