package com.example.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.application.data.BasketballSession;
import com.example.application.data.BasketballSessionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// TDD Tests fuer die Trefferquote von allen BasketballSessions
// Trefferquote = alle Treffer / alle Wuerfe * 100 (2 Stellen gerundet)
// Die Datenbank wird gemockt, also wird keine echte DB benutzt
class BasketballSessionServiceTest {

    private BasketballSessionRepository repository;
    private BasketballSessionService service;

    // Wird vor jedem Test neu gemacht, damit jeder Test frisch startet
    @BeforeEach
    void setUp() {
        repository = mock(BasketballSessionRepository.class);
        service = new BasketballSessionService(repository);
    }

    // Hilfsmethode: macht eine Session mit Wuerfen und Treffern
    private BasketballSession neueSession(Integer wuerfe, Integer treffer) {
        BasketballSession session = new BasketballSession();
        session.setShots(wuerfe);
        session.setHits(treffer);
        return session;
    }

    // ---------- Normalfaelle ----------

    @Test
    void zweiSessions_ergibt65Prozent() {
        // Arrange: 10 Wuerfe / 5 Treffer und 10 Wuerfe / 8 Treffer
        when(repository.findAll()).thenReturn(List.of(neueSession(10, 5), neueSession(10, 8)));

        // Act
        double ergebnis = service.calculateHitRate();

        // Assert: 13 / 20 * 100 = 65.0
        assertEquals(65.0, ergebnis);
    }

    @Test
    void eineSession_ergibt75Prozent() {
        // Arrange: 4 Wuerfe / 3 Treffer
        when(repository.findAll()).thenReturn(List.of(neueSession(4, 3)));

        // Act
        double ergebnis = service.calculateHitRate();

        // Assert: 3 / 4 * 100 = 75.0
        assertEquals(75.0, ergebnis);
    }

    @Test
    void ergebnisWirdAufZweiStellenGerundet() {
        // Arrange: 3 Wuerfe / 1 Treffer
        when(repository.findAll()).thenReturn(List.of(neueSession(3, 1)));

        // Act
        double ergebnis = service.calculateHitRate();

        // Assert: 1 / 3 * 100 = 33.333... wird 33.33
        assertEquals(33.33, ergebnis);
    }

    // ---------- Grenzwerte ----------

    @Test
    void alleWuerfeGetroffen_ergibt100Prozent() {
        // Arrange: Treffer gleich Wuerfe ist noch erlaubt
        when(repository.findAll()).thenReturn(List.of(neueSession(8, 8)));

        // Act
        double ergebnis = service.calculateHitRate();

        // Assert
        assertEquals(100.0, ergebnis);
    }

    @Test
    void keinWurfGetroffen_ergibt0Prozent() {
        // Arrange: 8 Wuerfe / 0 Treffer
        when(repository.findAll()).thenReturn(List.of(neueSession(8, 0)));

        // Act
        double ergebnis = service.calculateHitRate();

        // Assert
        assertEquals(0.0, ergebnis);
    }

    @Test
    void einTrefferZuViel_wirftFehler() {
        // Arrange: Grenzwert, 5 Wuerfe aber 6 Treffer (nur 1 zu viel)
        when(repository.findAll()).thenReturn(List.of(neueSession(5, 6)));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.calculateHitRate());
    }

    // ---------- Sonderfaelle ----------

    @Test
    void keineSessions_ergibt0Prozent() {
        // Arrange: Datenbank ist leer
        when(repository.findAll()).thenReturn(List.of());

        // Act
        double ergebnis = service.calculateHitRate();

        // Assert: kein Absturz wegen Division durch 0
        assertEquals(0.0, ergebnis);
    }

    @Test
    void nurSessionMitNullWuerfen_ergibt0Prozent() {
        // Arrange: 0 Wuerfe / 0 Treffer
        when(repository.findAll()).thenReturn(List.of(neueSession(0, 0)));

        // Act
        double ergebnis = service.calculateHitRate();

        // Assert: 0.0 und nicht NaN
        assertEquals(0.0, ergebnis);
    }

    @Test
    void sessionMitNullWuerfen_aendertErgebnisNicht() {
        // Arrange: eine normale Session und eine leere Session
        when(repository.findAll()).thenReturn(List.of(neueSession(10, 5), neueSession(0, 0)));

        // Act
        double ergebnis = service.calculateHitRate();

        // Assert: nur 5 / 10 zaehlt
        assertEquals(50.0, ergebnis);
    }

    @Test
    void sessionOhneWerte_wirdUebersprungen() {
        // Arrange: eine Session ohne Werte (null) und eine normale Session
        when(repository.findAll()).thenReturn(List.of(neueSession(null, null), neueSession(4, 2)));

        // Act
        double ergebnis = service.calculateHitRate();

        // Assert: nur 2 / 4 zaehlt
        assertEquals(50.0, ergebnis);
    }

    @Test
    void sessionOhneTreffer_wirdUebersprungen() {
        // Arrange: Wuerfe sind da, aber Treffer fehlen (null)
        when(repository.findAll()).thenReturn(List.of(neueSession(5, null), neueSession(4, 2)));

        // Act
        double ergebnis = service.calculateHitRate();

        // Assert: nur 2 / 4 zaehlt
        assertEquals(50.0, ergebnis);
    }

    // ---------- Ungueltige Daten ----------

    @Test
    void mehrTrefferAlsWuerfe_wirftFehler() {
        // Arrange: 5 Wuerfe aber 7 Treffer ist unmoeglich
        when(repository.findAll()).thenReturn(List.of(neueSession(5, 7)));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.calculateHitRate());
    }

    @Test
    void negativeWuerfe_wirftFehler() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of(neueSession(-3, 0)));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.calculateHitRate());
    }

    @Test
    void negativeTreffer_wirftFehler() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of(neueSession(5, -1)));

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> service.calculateHitRate());
    }

    // ---------- Datenbank ----------

    @Test
    void datenbankWirdGenauEinmalGelesen() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of(neueSession(10, 5)));

        // Act
        service.calculateHitRate();

        // Assert: findAll() wurde genau 1 Mal aufgerufen
        verify(repository, times(1)).findAll();
    }

    // ---------- Standard-Methoden (get, save, delete, findAll, count) ----------

    @Test
    void get_gibtSessionZurueck() {
        // Arrange
        BasketballSession session = neueSession(10, 5);
        when(repository.findById(1L)).thenReturn(Optional.of(session));

        // Act
        Optional<BasketballSession> ergebnis = service.get(1L);

        // Assert
        assertTrue(ergebnis.isPresent());
        assertSame(session, ergebnis.get());
    }

    @Test
    void get_gibtLeerZurueckWennIdNichtExistiert() {
        // Arrange
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<BasketballSession> ergebnis = service.get(99L);

        // Assert
        assertTrue(ergebnis.isEmpty());
    }

    @Test
    void save_speichertSessionImRepository() {
        // Arrange
        BasketballSession session = neueSession(10, 5);
        when(repository.save(session)).thenReturn(session);

        // Act
        BasketballSession ergebnis = service.save(session);

        // Assert
        assertSame(session, ergebnis);
        verify(repository, times(1)).save(session);
    }

    @Test
    void delete_loeschtSessionMitRichtigerId() {
        // Act
        service.delete(3L);

        // Assert
        verify(repository, times(1)).deleteById(3L);
    }

    @Test
    void findAll_gibtAlleSessionsZurueck() {
        // Arrange
        List<BasketballSession> sessions = List.of(neueSession(10, 5), neueSession(4, 3));
        when(repository.findAll()).thenReturn(sessions);

        // Act
        List<BasketballSession> ergebnis = service.findAll();

        // Assert
        assertEquals(2, ergebnis.size());
        assertSame(sessions, ergebnis);
    }

    @Test
    void count_gibtAnzahlSessionsZurueck() {
        // Arrange
        when(repository.count()).thenReturn(7L);

        // Act
        int ergebnis = service.count();

        // Assert
        assertEquals(7, ergebnis);
    }
}
