package com.example.application.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit-Tests für die Entität {@link SamplePerson} (Spieler im Team Roster).
 *
 * Getestet wird nur die Klasse selbst, ohne Datenbank und ohne Spring.
 * Aufbau jedes Tests: Arrange (vorbereiten), Act (ausführen), Assert (prüfen).
 *
 * Happy Path:   normale, gültige Eingaben.
 * Unhappy Path: fehlende, ungültige oder unerwartete Eingaben.
 *
 * Tests mit "Schwachstelle" im Namen zeigen, wie sich der Code im Moment
 * bei einem Problem verhält. Wird der Code verbessert, muss der Test angepasst werden.
 */
@SuppressWarnings("DataFlowIssue") // im Unhappy Path wird absichtlich null übergeben
@DisplayName("SamplePerson (Entität)")
class SamplePersonTest {

    private SamplePerson person;

    @BeforeEach
    void setUp() {
        person = new SamplePerson();
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("neue Person hat noch keine ID und Version 0")
        void neuePersonHatKeineId() {
            assertThat(person.getId()).isNull();
            assertThat(person.getVersion()).isZero();
        }

        @Test
        @DisplayName("neue Person ist standardmässig kein Star-Spieler")
        void importantIstStandardmaessigFalse() {
            assertThat(person.isImportant()).isFalse();
        }

        @Test
        @DisplayName("neue Person hat alle Felder auf null")
        void alleFelderSindNull() {
            assertThat(person.getFirstName()).isNull();
            assertThat(person.getLastName()).isNull();
            assertThat(person.getHeight()).isNull();
            assertThat(person.getWingspan()).isNull();
            assertThat(person.getWeight()).isNull();
            assertThat(person.getDateOfBirth()).isNull();
            assertThat(person.getRole()).isNull();
        }

        @Test
        @DisplayName("Vorname und Nachname werden gespeichert")
        void vornameUndNachname() {
            person.setFirstName("Max");
            person.setLastName("Muster");

            assertThat(person.getFirstName()).isEqualTo("Max");
            assertThat(person.getLastName()).isEqualTo("Muster");
        }

        @Test
        @DisplayName("Körpermasse (Grösse, Spannweite, Gewicht) werden gespeichert")
        void koerpermasse() {
            person.setHeight(198.5);
            person.setWingspan(205.0);
            person.setWeight(95.2);

            assertThat(person.getHeight()).isEqualTo(198.5);
            assertThat(person.getWingspan()).isEqualTo(205.0);
            assertThat(person.getWeight()).isEqualTo(95.2);
        }

        @Test
        @DisplayName("Geburtsdatum wird gespeichert")
        void geburtsdatum() {
            LocalDate geburtstag = LocalDate.of(2005, 3, 14);

            person.setDateOfBirth(geburtstag);

            assertThat(person.getDateOfBirth()).isEqualTo(geburtstag);
        }

        @Test
        @DisplayName("Position (Rolle) wird gespeichert")
        void position() {
            person.setRole("Point Guard");

            assertThat(person.getRole()).isEqualTo("Point Guard");
        }

        @Test
        @DisplayName("Star-Spieler kann gesetzt und wieder entfernt werden")
        void starSpieler() {
            person.setImportant(true);
            assertThat(person.isImportant()).isTrue();

            person.setImportant(false);
            assertThat(person.isImportant()).isFalse();
        }

        @Test
        @DisplayName("ID kann gesetzt werden")
        void id() {
            person.setId(42L);

            assertThat(person.getId()).isEqualTo(42L);
        }

        @Test
        @DisplayName("getFullName() setzt Vor- und Nachname mit Leerzeichen zusammen")
        void vollerName() {
            person.setFirstName("Max");
            person.setLastName("Muster");

            assertThat(person.getFullName()).isEqualTo("Max Muster");
        }

        @Test
        @DisplayName("getFullName() passt sich an, wenn der Name geändert wird")
        void nameAendern() {
            person.setFirstName("Max");
            person.setLastName("Muster");

            person.setLastName("Meier");

            assertThat(person.getFullName()).isEqualTo("Max Meier");
        }

        @Test
        @DisplayName("equals(): zwei Personen mit gleicher ID sind gleich")
        void gleicheIdIstGleich() {
            SamplePerson a = personMitId(1L);
            SamplePerson b = personMitId(1L);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("equals(): zwei Personen mit verschiedener ID sind nicht gleich")
        void andereIdIstNichtGleich() {
            SamplePerson a = personMitId(1L);
            SamplePerson b = personMitId(2L);

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("Schwachstelle: getFullName() ohne Vor- und Nachname gibt \"null null\" zurück")
        void fullNameOhneNamen() {
            String name = person.getFullName();

            assertThat(name).isEqualTo("null null");
        }

        @Test
        @DisplayName("Schwachstelle: getFullName() ohne Vorname gibt \"null Muster\" zurück")
        void fullNameOhneVorname() {
            person.setLastName("Muster");

            assertThat(person.getFullName()).isEqualTo("null Muster");
        }

        @Test
        @DisplayName("Schwachstelle: negative Körpermasse werden ohne Prüfung gespeichert")
        void negativeKoerpermasse() {
            person.setHeight(-180.0);
            person.setWingspan(-1.0);
            person.setWeight(0.0);

            // Die Entität hat keine Validierung (z.B. @Positive), darum wird alles übernommen
            assertThat(person.getHeight()).isEqualTo(-180.0);
            assertThat(person.getWingspan()).isEqualTo(-1.0);
            assertThat(person.getWeight()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("Schwachstelle: Geburtsdatum in der Zukunft wird akzeptiert")
        void geburtsdatumInZukunft() {
            LocalDate zukunft = LocalDate.now().plusYears(10);

            person.setDateOfBirth(zukunft);

            assertThat(person.getDateOfBirth()).isEqualTo(zukunft);
        }

        @Test
        @DisplayName("Felder können mit null überschrieben werden")
        void felderMitNullUeberschreiben() {
            person.setFirstName("Max");
            person.setHeight(198.5);
            person.setRole("Center");

            person.setFirstName(null);
            person.setHeight(null);
            person.setRole(null);

            assertThat(person.getFirstName()).isNull();
            assertThat(person.getHeight()).isNull();
            assertThat(person.getRole()).isNull();
        }

        @Test
        @DisplayName("equals(): ohne ID ist eine Person nur mit sich selbst gleich")
        void ohneIdNurSichSelbstGleich() {
            SamplePerson a = new SamplePerson();
            SamplePerson b = new SamplePerson();

            assertThat(a).isEqualTo(a);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("equals(): Person mit ID ist nicht gleich Person ohne ID")
        void mitIdUndOhneId() {
            SamplePerson mitId = personMitId(1L);
            SamplePerson ohneId = new SamplePerson();

            assertThat(mitId).isNotEqualTo(ohneId);
            assertThat(ohneId).isNotEqualTo(mitId);
        }

        @Test
        @DisplayName("equals(): ist nicht gleich null oder einem anderen Typ")
        void nichtGleichNullOderAnderemTyp() {
            SamplePerson a = personMitId(1L);

            assertThat(a).isNotEqualTo(null);
            assertThat(a).isNotEqualTo("Max Muster");
        }

        @Test
        @DisplayName("Schwachstelle: SamplePerson und Profile mit gleicher ID gelten als gleich")
        void andereEntitaetMitGleicherId() {
            SamplePerson spieler = personMitId(1L);
            Profile profil = new Profile();
            profil.setId(1L);

            // AbstractEntity.equals() prüft nur die ID, nicht die Klasse
            assertThat(spieler).isEqualTo(profil);
        }
    }

    private SamplePerson personMitId(Long id) {
        SamplePerson p = new SamplePerson();
        p.setId(id);
        return p;
    }
}
