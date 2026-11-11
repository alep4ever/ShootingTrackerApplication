package com.example.application.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.application.data.Profile;
import com.example.application.data.ProfileRepository;
import com.example.application.data.SamplePerson;
import com.example.application.data.Skill;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Unit-Tests für {@link ProfileService}.
 *
 * Das {@link ProfileRepository} wird gemockt, damit keine Datenbank nötig ist.
 * Mit dem ArgumentCaptor wird geprüft, was der Service genau speichert.
 *
 * Im Unhappy Path lässt Mockito das Repository Fehler werfen (thenThrow / doThrow),
 * so wie es bei einer echten Datenbank passieren kann.
 *
 * Tests mit "Schwachstelle" im Namen zeigen, wie sich der Code im Moment
 * bei einem Problem verhält. Wird der Code verbessert, muss der Test angepasst werden.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("DataFlowIssue") // im Unhappy Path wird absichtlich null übergeben
@DisplayName("ProfileService")
class ProfileServiceTest {

    @Mock
    private ProfileRepository repository;

    @Mock
    private SamplePerson spieler;

    @Captor
    private ArgumentCaptor<Profile> profileCaptor;

    @InjectMocks
    private ProfileService service;

    @Nested
    @DisplayName("Happy Path")
    class HappyPath {

        @Test
        @DisplayName("get() gibt das Profil zurück, wenn die ID existiert")
        void getGefunden() {
            Profile profil = profil(1L, "Max Muster");
            when(repository.findById(1L)).thenReturn(Optional.of(profil));

            assertThat(service.get(1L)).containsSame(profil);
        }

        @Test
        @DisplayName("save() speichert das Profil über das Repository")
        void save() {
            Profile profil = profil(null, "Max Muster");
            Profile gespeichert = profil(1000L, "Max Muster");
            when(repository.save(profil)).thenReturn(gespeichert);

            Profile resultat = service.save(profil);

            assertThat(resultat).isSameAs(gespeichert);
            verify(repository).save(profil);
        }

        @Test
        @DisplayName("delete() löscht das Profil mit der richtigen ID")
        void delete() {
            service.delete(3L);

            verify(repository).deleteById(3L);
        }

        @Test
        @DisplayName("findAll() lädt die Profile zusammen mit ihren Skills")
        void findAllMitSkills() {
            List<Profile> profile = List.of(profil(1L, "Max Muster"), profil(2L, "Lea Keller"));
            when(repository.findAllWithSkills()).thenReturn(profile);

            List<Profile> resultat = service.findAll();

            assertThat(resultat).isEqualTo(profile);
            // Wichtig: die Abfrage mit JOIN FETCH, nicht das normale findAll()
            verify(repository).findAllWithSkills();
            verify(repository, never()).findAll();
        }

        @Test
        @DisplayName("count() gibt die Anzahl Profile zurück")
        void count() {
            when(repository.count()).thenReturn(4L);

            assertThat(service.count()).isEqualTo(4L);
        }

        @Test
        @DisplayName("findByPlayer() gibt das Profil des Spielers zurück")
        void findByPlayerVorhanden() {
            Profile profil = profil(1L, "Max Muster");
            profil.setPlayer(spieler);
            when(repository.findByPlayer(spieler)).thenReturn(Optional.of(profil));

            Optional<Profile> resultat = service.findByPlayer(spieler);

            assertThat(resultat).containsSame(profil);
            assertThat(resultat.get().getPlayer()).isSameAs(spieler);
        }

        @Test
        @DisplayName("addSkillToProfile() fügt den Skill hinzu und speichert das Profil")
        void skillHinzufuegen() {
            Profile profil = profil(1L, "Max Muster");
            Skill crossover = skill(10L, "Crossover Dribble");
            when(repository.findById(1L)).thenReturn(Optional.of(profil));

            service.addSkillToProfile(1L, crossover);

            verify(repository).save(profileCaptor.capture());
            assertThat(profileCaptor.getValue().getSkills()).containsExactly(crossover);
        }

        @Test
        @DisplayName("addSkillToProfile() behält bereits vorhandene Skills")
        void vorhandeneSkillsBleiben() {
            Profile profil = profil(1L, "Max Muster");
            Skill crossover = skill(10L, "Crossover Dribble");
            Skill layup = skill(11L, "Layup Fundamentals");
            profil.addSkill(crossover);
            when(repository.findById(1L)).thenReturn(Optional.of(profil));

            service.addSkillToProfile(1L, layup);

            verify(repository).save(profileCaptor.capture());
            assertThat(profileCaptor.getValue().getSkills()).containsExactlyInAnyOrder(crossover, layup);
        }

        @Test
        @DisplayName("removeSkillFromProfile() entfernt den Skill und speichert das Profil")
        void skillEntfernen() {
            Profile profil = profil(1L, "Max Muster");
            Skill crossover = skill(10L, "Crossover Dribble");
            Skill layup = skill(11L, "Layup Fundamentals");
            profil.addSkill(crossover);
            profil.addSkill(layup);
            when(repository.findById(1L)).thenReturn(Optional.of(profil));

            service.removeSkillFromProfile(1L, crossover);

            verify(repository).save(profileCaptor.capture());
            assertThat(profileCaptor.getValue().getSkills()).containsExactly(layup);
        }
    }

    @Nested
    @DisplayName("Unhappy Path")
    class UnhappyPath {

        @Test
        @DisplayName("get() gibt ein leeres Optional zurück, wenn die ID nicht existiert")
        void getNichtGefunden() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThat(service.get(99L)).isEmpty();
        }

        @Test
        @DisplayName("get(null) gibt die IllegalArgumentException vom Repository weiter")
        void getMitNull() {
            when(repository.findById(null))
                    .thenThrow(new IllegalArgumentException("The given id must not be null"));

            assertThatThrownBy(() -> service.get(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("findByPlayer() gibt ein leeres Optional zurück, wenn der Spieler kein Profil hat")
        void findByPlayerOhneProfil() {
            when(repository.findByPlayer(spieler)).thenReturn(Optional.empty());

            assertThat(service.findByPlayer(spieler)).isEmpty();
        }

        @Test
        @DisplayName("findAll() gibt eine leere Liste zurück, wenn es keine Profile gibt")
        void findAllLeer() {
            when(repository.findAllWithSkills()).thenReturn(List.of());

            assertThat(service.findAll()).isEmpty();
        }

        @Test
        @DisplayName("count() gibt 0 zurück, wenn es keine Profile gibt")
        void countNull() {
            when(repository.count()).thenReturn(0L);

            assertThat(service.count()).isZero();
        }

        @Test
        @DisplayName("save() gibt den Fehler weiter, wenn ein anderer Benutzer das Profil gleichzeitig geändert hat")
        void saveOptimisticLocking() {
            Profile profil = profil(1L, "Max Muster");
            when(repository.save(profil))
                    .thenThrow(new ObjectOptimisticLockingFailureException(Profile.class, 1L));

            assertThatThrownBy(() -> service.save(profil))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        }

        @Test
        @DisplayName("delete() gibt den Datenbankfehler weiter")
        void deleteFehler() {
            doThrow(new DataIntegrityViolationException("FK profile_skills.profile_id"))
                    .when(repository).deleteById(1L);

            assertThatThrownBy(() -> service.delete(1L))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("addSkillToProfile() speichert nichts, wenn das Profil nicht existiert")
        void addSkillProfilExistiertNicht() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            service.addSkillToProfile(99L, skill(10L, "Crossover Dribble"));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("addSkillToProfile() fügt einen bereits zugewiesenen Skill nicht doppelt hinzu")
        void addSkillDoppelt() {
            Profile profil = profil(1L, "Max Muster");
            Skill crossover = skill(10L, "Crossover Dribble");
            profil.addSkill(crossover);
            when(repository.findById(1L)).thenReturn(Optional.of(profil));

            service.addSkillToProfile(1L, skill(10L, "Crossover Dribble"));

            verify(repository).save(profileCaptor.capture());
            assertThat(profileCaptor.getValue().getSkills()).hasSize(1);
        }

        @Test
        @DisplayName("Schwachstelle: addSkillToProfile() mit null-Skill speichert null im Profil")
        void addSkillNull() {
            Profile profil = profil(1L, "Max Muster");
            when(repository.findById(1L)).thenReturn(Optional.of(profil));

            service.addSkillToProfile(1L, null);

            verify(repository).save(profileCaptor.capture());
            assertThat(profileCaptor.getValue().getSkills()).containsNull();
        }

        @Test
        @DisplayName("addSkillToProfile() gibt den Fehler weiter und speichert nichts, wenn das Laden fehlschlägt")
        void addSkillLadenFehler() {
            when(repository.findById(1L))
                    .thenThrow(new DataAccessResourceFailureException("Datenbank nicht erreichbar"));

            assertThatThrownBy(() -> service.addSkillToProfile(1L, skill(10L, "Crossover Dribble")))
                    .isInstanceOf(DataAccessResourceFailureException.class);
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("addSkillToProfile() gibt den Fehler weiter, wenn das Speichern fehlschlägt")
        void addSkillSpeichernFehler() {
            Profile profil = profil(1L, "Max Muster");
            when(repository.findById(1L)).thenReturn(Optional.of(profil));
            when(repository.save(any()))
                    .thenThrow(new DataAccessResourceFailureException("Datenbank nicht erreichbar"));

            assertThatThrownBy(() -> service.addSkillToProfile(1L, skill(10L, "Crossover Dribble")))
                    .isInstanceOf(DataAccessResourceFailureException.class);
        }

        @Test
        @DisplayName("removeSkillFromProfile() speichert nichts, wenn das Profil nicht existiert")
        void removeSkillProfilExistiertNicht() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            service.removeSkillFromProfile(99L, skill(10L, "Crossover Dribble"));

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("removeSkillFromProfile() lässt die Skills unverändert, wenn der Skill nicht zugewiesen ist")
        void removeSkillNichtZugewiesen() {
            Profile profil = profil(1L, "Max Muster");
            Skill layup = skill(11L, "Layup Fundamentals");
            profil.addSkill(layup);
            when(repository.findById(1L)).thenReturn(Optional.of(profil));

            service.removeSkillFromProfile(1L, skill(10L, "Crossover Dribble"));

            verify(repository).save(profileCaptor.capture());
            assertThat(profileCaptor.getValue().getSkills()).containsExactly(layup);
        }
    }

    private Profile profil(Long id, String name) {
        Profile p = new Profile();
        p.setId(id);
        p.setName(name);
        return p;
    }

    private Skill skill(Long id, String name) {
        Skill s = new Skill();
        s.setId(id);
        s.setName(name);
        return s;
    }
}
