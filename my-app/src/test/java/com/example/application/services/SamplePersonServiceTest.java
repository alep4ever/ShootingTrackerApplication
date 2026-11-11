package com.example.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.application.data.SamplePerson;
import com.example.application.data.SamplePersonRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Unit-Tests für {@link SamplePersonService}.
 *
 * Das Repository wird gemockt. Es braucht also keine MySQL-Datenbank:
 * Mockito gibt vor, was das Repository zurückgibt, und prüft danach,
 * ob der Service das Repository richtig aufgerufen hat.
 *
 * Im Unhappy Path lässt Mockito das Repository Fehler werfen (thenThrow / doThrow),
 * so wie es bei einer echten Datenbank passieren kann.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("DataFlowIssue") // im Unhappy Path wird absichtlich null übergeben
@DisplayName("SamplePersonService")
class SamplePersonServiceTest {

    @Mock
    private SamplePersonRepository repository;

    @Mock
    private Specification<SamplePerson> filter;

    @InjectMocks
    private SamplePersonService service;

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("get() gibt den Spieler zurück, wenn die ID existiert")
        void getGefunden() {
            SamplePerson max = spieler(1L, "Max", "Muster");
            when(repository.findById(1L)).thenReturn(Optional.of(max));

            Optional<SamplePerson> resultat = service.get(1L);

            assertThat(resultat).containsSame(max);
            verify(repository).findById(1L);
        }

        @Test
        @DisplayName("save() speichert den Spieler über das Repository")
        void save() {
            SamplePerson neu = spieler(null, "Max", "Muster");
            SamplePerson gespeichert = spieler(1000L, "Max", "Muster");
            when(repository.save(neu)).thenReturn(gespeichert);

            SamplePerson resultat = service.save(neu);

            assertThat(resultat).isSameAs(gespeichert);
            assertThat(resultat.getId()).isEqualTo(1000L);
            verify(repository).save(neu);
        }

        @Test
        @DisplayName("delete() löscht den Spieler mit der richtigen ID")
        void delete() {
            service.delete(7L);

            verify(repository).deleteById(7L);
        }

        @Test
        @DisplayName("list(pageable) gibt eine Seite mit Spielern zurück")
        void listMitPageable() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SamplePerson> seite = new PageImpl<>(List.of(
                    spieler(1L, "Max", "Muster"),
                    spieler(2L, "Lea", "Keller")));
            when(repository.findAll(pageable)).thenReturn(seite);

            Page<SamplePerson> resultat = service.list(pageable);

            assertThat(resultat.getContent())
                    .extracting(SamplePerson::getFullName)
                    .containsExactly("Max Muster", "Lea Keller");
            verify(repository).findAll(pageable);
        }

        @Test
        @DisplayName("list(pageable, filter) gibt den Filter an das Repository weiter")
        void listMitFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SamplePerson> seite = new PageImpl<>(List.of(spieler(1L, "Max", "Muster")));
            when(repository.findAll(filter, pageable)).thenReturn(seite);

            Page<SamplePerson> resultat = service.list(pageable, filter);

            assertThat(resultat.getContent()).hasSize(1);
            verify(repository).findAll(filter, pageable);
        }

        @Test
        @DisplayName("count() gibt die Anzahl Spieler als int zurück")
        void count() {
            when(repository.count()).thenReturn(12L);

            int anzahl = service.count();

            assertThat(anzahl).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("get() gibt ein leeres Optional zurück, wenn die ID nicht existiert")
        void getNichtGefunden() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            Optional<SamplePerson> resultat = service.get(99L);

            assertThat(resultat).isEmpty();
        }

        @Test
        @DisplayName("get(null) gibt die IllegalArgumentException vom Repository weiter")
        void getMitNull() {
            when(repository.findById(null))
                    .thenThrow(new IllegalArgumentException("The given id must not be null"));

            assertThatThrownBy(() -> service.get(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must not be null");
        }

        @Test
        @DisplayName("save() gibt den Fehler weiter, wenn ein anderer Benutzer den Spieler gleichzeitig geändert hat")
        void saveOptimisticLocking() {
            SamplePerson max = spieler(1L, "Max", "Muster");
            when(repository.save(max))
                    .thenThrow(new ObjectOptimisticLockingFailureException(SamplePerson.class, 1L));

            // Die View (MasterDetailView) fängt genau diesen Fehler ab
            assertThatThrownBy(() -> service.save(max))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        }

        @Test
        @DisplayName("save() gibt den Fehler weiter, wenn die Datenbank nicht erreichbar ist")
        void saveDatenbankNichtErreichbar() {
            SamplePerson max = spieler(null, "Max", "Muster");
            when(repository.save(max))
                    .thenThrow(new DataAccessResourceFailureException("Datenbank nicht erreichbar"));

            assertThatThrownBy(() -> service.save(max))
                    .isInstanceOf(DataAccessResourceFailureException.class)
                    .hasMessage("Datenbank nicht erreichbar");
        }

        @Test
        @DisplayName("delete() gibt den Fehler weiter, wenn der Spieler noch ein Profil hat (Fremdschlüssel)")
        void deleteMitProfil() {
            doThrow(new DataIntegrityViolationException("FK profile.player_id"))
                    .when(repository).deleteById(1L);

            assertThatThrownBy(() -> service.delete(1L))
                    .isInstanceOf(DataIntegrityViolationException.class);
            verify(repository).deleteById(1L);
        }

        @Test
        @DisplayName("list(pageable) gibt eine leere Seite zurück, wenn es keine Spieler gibt")
        void listLeer() {
            Pageable pageable = PageRequest.of(0, 10);
            when(repository.findAll(pageable)).thenReturn(Page.empty());

            Page<SamplePerson> resultat = service.list(pageable);

            assertThat(resultat.getContent()).isEmpty();
            assertThat(resultat.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("list(pageable, filter) gibt eine leere Seite zurück, wenn der Filter nichts findet")
        void listFilterOhneTreffer() {
            Pageable pageable = PageRequest.of(0, 10);
            when(repository.findAll(filter, pageable)).thenReturn(Page.empty());

            Page<SamplePerson> resultat = service.list(pageable, filter);

            assertThat(resultat.getContent()).isEmpty();
        }

        @Test
        @DisplayName("count() gibt 0 zurück, wenn es keine Spieler gibt")
        void countNull() {
            when(repository.count()).thenReturn(0L);

            assertThat(service.count()).isZero();
        }

        @Test
        @DisplayName("Schwachstelle: count() läuft über, wenn es mehr als Integer.MAX_VALUE Spieler gibt")
        void countUeberlauf() {
            when(repository.count()).thenReturn(Integer.MAX_VALUE + 1L);

            int anzahl = service.count();

            // (int) schneidet den long-Wert ab, darum wird die Zahl negativ
            assertThat(anzahl).isNegative();
        }
    }

    private SamplePerson spieler(Long id, String vorname, String nachname) {
        SamplePerson p = new SamplePerson();
        p.setId(id);
        p.setFirstName(vorname);
        p.setLastName(nachname);
        return p;
    }
}
