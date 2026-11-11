package com.example.application.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Tests fuer die Entitaet BasketballSession
// Prueft den Konstruktor und alle Getter und Setter
class BasketballSessionTest {

    private BasketballSession session;

    // Wird vor jedem Test neu gemacht, damit jeder Test frisch startet
    @BeforeEach
    void setUp() {
        session = new BasketballSession();
    }

    // ---------- Konstruktor ----------

    @Test
    void neueSession_hatErstellungszeitVonJetzt() {
        // Arrange: Zeit kurz vor und nach dem Erstellen merken
        LocalDateTime vorher = LocalDateTime.now();

        // Act
        BasketballSession neueSession = new BasketballSession();
        LocalDateTime nachher = LocalDateTime.now();

        // Assert: createdAt liegt zwischen vorher und nachher
        assertNotNull(neueSession.getCreatedAt());
        assertFalse(neueSession.getCreatedAt().isBefore(vorher));
        assertFalse(neueSession.getCreatedAt().isAfter(nachher));
    }

    @Test
    void neueSession_hatNochKeineWerte() {
        // Assert: Wuerfe, Treffer und Zeit sind am Anfang leer
        assertNull(session.getShots());
        assertNull(session.getHits());
        assertNull(session.getTimeMinutes());
    }

    // ---------- Getter und Setter ----------

    @Test
    void setShots_speichertWuerfe() {
        // Act
        session.setShots(10);

        // Assert
        assertEquals(10, session.getShots());
    }

    @Test
    void setHits_speichertTreffer() {
        // Act
        session.setHits(7);

        // Assert
        assertEquals(7, session.getHits());
    }

    @Test
    void setTimeMinutes_speichertZeit() {
        // Act
        session.setTimeMinutes(12.5);

        // Assert
        assertEquals(12.5, session.getTimeMinutes());
    }

    @Test
    void setCreatedAt_speichertDatum() {
        // Arrange
        LocalDateTime datum = LocalDateTime.of(2026, 11, 11, 14, 30);

        // Act
        session.setCreatedAt(datum);

        // Assert
        assertEquals(datum, session.getCreatedAt());
    }

    @Test
    void neueSession_istEineEntitaet() {
        // Assert: BasketballSession erbt von AbstractEntity (ID und Version)
        assertTrue(session instanceof AbstractEntity);
        assertNull(session.getId());
    }
}
