package com.example.application.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit-Tests für die Entität {@link Profile} (Trainingsprofil eines Spielers).
 *
 * Der Spieler ({@link SamplePerson}) und die Skills ({@link Skill}) werden mit
 * Mockito gemockt. So wird nur das Verhalten von Profile getestet und nicht
 * das der anderen Klassen.
 *
 * Happy Path:   normale, gültige Eingaben.
 * Unhappy Path: fehlende, doppelte, ungültige oder unerwartete Eingaben.
 *
 * Tests mit "Schwachstelle" im Namen zeigen, wie sich der Code im Moment
 * bei einem Problem verhält. Wird der Code verbessert, muss der Test angepasst werden.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("DataFlowIssue") // im Unhappy Path wird absichtlich null übergeben
@DisplayName("Profile (Entität)")
class ProfileTest {

    @Mock
    private SamplePerson spieler;

    @Mock
    private Skill crossover;

    @Mock
    private Skill layup;

    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = new Profile();
    }

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("neues Profil hat noch keine ID, keinen Namen und keinen Spieler")
        void neuesProfilIstLeer() {
            assertThat(profile.getId()).isNull();
            assertThat(profile.getName()).isNull();
            assertThat(profile.getDescription()).isNull();
            assertThat(profile.getPlayer()).isNull();
        }

        @Test
        @DisplayName("neues Profil hat eine leere Skill-Liste (nicht null)")
        void skillListeIstLeerAberNichtNull() {
            assertThat(profile.getSkills()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Name und Beschreibung werden gespeichert")
        void nameUndBeschreibung() {
            profile.setName("Max Muster");
            profile.setDescription("Training profile for Point Guard");

            assertThat(profile.getName()).isEqualTo("Max Muster");
            assertThat(profile.getDescription()).isEqualTo("Training profile for Point Guard");
        }

        @Test
        @DisplayName("Spieler (SamplePerson) wird verknüpft")
        void spielerVerknuepfen() {
            when(spieler.getFullName()).thenReturn("Max Muster");

            profile.setPlayer(spieler);

            assertThat(profile.getPlayer()).isSameAs(spieler);
            assertThat(profile.getPlayer().getFullName()).isEqualTo("Max Muster");
        }

        @Test
        @DisplayName("setSkills() ersetzt die ganze Skill-Liste")
        void setSkillsErsetztListe() {
            profile.addSkill(crossover);
            Set<Skill> neueSkills = new HashSet<>(Set.of(layup));

            profile.setSkills(neueSkills);

            assertThat(profile.getSkills()).containsExactly(layup);
        }

        @Test
        @DisplayName("addSkill() fügt einen Skill hinzu")
        void skillHinzufuegen() {
            profile.addSkill(crossover);

            assertThat(profile.getSkills()).containsExactly(crossover);
        }

        @Test
        @DisplayName("addSkill() kann mehrere verschiedene Skills hinzufügen")
        void mehrereSkills() {
            profile.addSkill(crossover);
            profile.addSkill(layup);

            assertThat(profile.getSkills()).containsExactlyInAnyOrder(crossover, layup);
        }

        @Test
        @DisplayName("removeSkill() entfernt einen vorhandenen Skill")
        void skillEntfernen() {
            profile.addSkill(crossover);
            profile.addSkill(layup);

            profile.removeSkill(crossover);

            assertThat(profile.getSkills()).containsExactly(layup);
        }

        @Test
        @DisplayName("equals(): zwei Profile mit gleicher ID sind gleich")
        void gleicheIdIstGleich() {
            Profile a = profilMitId(1L);
            Profile b = profilMitId(1L);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("equals(): zwei Profile mit verschiedener ID sind nicht gleich")
        void andereIdIstNichtGleich() {
            Profile a = profilMitId(1L);
            Profile b = profilMitId(2L);

            assertThat(a).isNotEqualTo(b);
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("addSkill() fügt denselben Skill nicht doppelt hinzu")
        void keinDuplikat() {
            profile.addSkill(crossover);
            profile.addSkill(crossover);

            assertThat(profile.getSkills()).hasSize(1);
        }

        @Test
        @DisplayName("addSkill(): zwei Skill-Objekte mit gleicher ID zählen als ein Skill")
        void gleicheIdKeinDuplikat() {
            // Hier echte Skills statt Mocks, weil equals() über die ID geht
            Skill skillAusDb1 = skillMitId(5L);
            Skill skillAusDb2 = skillMitId(5L);

            profile.addSkill(skillAusDb1);
            profile.addSkill(skillAusDb2);

            assertThat(profile.getSkills()).hasSize(1);
        }

        @Test
        @DisplayName("removeSkill() wirft keinen Fehler, wenn der Skill nicht vorhanden ist")
        void nichtVorhandenenSkillEntfernen() {
            profile.addSkill(layup);

            assertThatCode(() -> profile.removeSkill(crossover)).doesNotThrowAnyException();
            assertThat(profile.getSkills()).containsExactly(layup);
        }

        @Test
        @DisplayName("removeSkill() auf leerer Skill-Liste wirft keinen Fehler")
        void entfernenAusLeererListe() {
            assertThatCode(() -> profile.removeSkill(crossover)).doesNotThrowAnyException();
            assertThat(profile.getSkills()).isEmpty();
        }

        @Test
        @DisplayName("Spieler kann mit null wieder entfernt werden")
        void spielerEntfernen() {
            profile.setPlayer(spieler);

            profile.setPlayer(null);

            assertThat(profile.getPlayer()).isNull();
        }

        @Test
        @DisplayName("Schwachstelle: addSkill(null) wird ohne Prüfung in die Liste aufgenommen")
        void nullSkillHinzufuegen() {
            profile.addSkill(null);

            assertThat(profile.getSkills()).hasSize(1).containsNull();
        }

        @Test
        @DisplayName("nach setSkills(null) werfen addSkill() und removeSkill() eine NullPointerException")
        void skillListeNull() {
            profile.setSkills(null);

            assertThat(profile.getSkills()).isNull();
            assertThatThrownBy(() -> profile.addSkill(crossover))
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> profile.removeSkill(crossover))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Schwachstelle: Skill ohne ID hinzufügen, später ID setzen → removeSkill() findet ihn nicht mehr")
        void hashCodeAendertSichNachSpeichern() {
            Skill neuerSkill = new Skill();      // noch nicht gespeichert, ID = null
            profile.addSkill(neuerSkill);

            neuerSkill.setId(5L);                // so wie nach dem Speichern in der DB
            profile.removeSkill(neuerSkill);

            // hashCode() hängt von der ID ab. Das HashSet sucht deshalb am falschen Ort.
            assertThat(profile.getSkills()).hasSize(1);
        }

        @Test
        @DisplayName("equals(): ohne ID ist ein Profil nur mit sich selbst gleich")
        void ohneIdNurSichSelbstGleich() {
            Profile a = new Profile();
            Profile b = new Profile();

            assertThat(a).isEqualTo(a);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("equals(): ist nicht gleich null oder einem anderen Typ")
        void nichtGleichNullOderAnderemTyp() {
            Profile a = profilMitId(1L);

            assertThat(a).isNotEqualTo(null);
            assertThat(a).isNotEqualTo("Max Muster");
        }
    }

    private Profile profilMitId(Long id) {
        Profile p = new Profile();
        p.setId(id);
        return p;
    }

    private Skill skillMitId(Long id) {
        Skill s = new Skill();
        s.setId(id);
        return s;
    }
}
