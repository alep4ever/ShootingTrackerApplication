package com.example.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.application.data.Skill;
import com.example.application.data.SkillRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for SkillService. The SkillRepository (database) is fully mocked,
 * so no real DB is contacted.
 */
@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository repository;

    @InjectMocks
    private SkillService service;

    private Skill skill;

    @BeforeEach
    void setUp() {
        skill = new Skill();
        skill.setName("Crossover");
        skill.setDescription("Ball handling move");
        skill.setVideoFileName("crossover.mp4");
    }

    // ---- get ----

    @Test
    void get_returnsSkillWhenRepositoryFindsIt() {
        when(repository.findById(1L)).thenReturn(Optional.of(skill));

        Optional<Skill> result = service.get(1L);

        assertTrue(result.isPresent());
        assertSame(skill, result.get());
        verify(repository).findById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void get_returnsEmptyWhenRepositoryFindsNothing() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<Skill> result = service.get(99L);

        assertTrue(result.isEmpty());
        verify(repository).findById(99L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void get_passesIdUnchangedToRepository() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        service.get(42L);

        verify(repository).findById(42L);
        verify(repository, never()).findById(1L);
    }

    // ---- save ----

    @Test
    void save_delegatesToRepositoryAndReturnsSavedEntity() {
        Skill saved = new Skill();
        saved.setName("Crossover");
        when(repository.save(skill)).thenReturn(saved);

        Skill result = service.save(skill);

        assertSame(saved, result);
        verify(repository).save(skill);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void save_returnsWhatRepositoryReturns_notTheInput() {
        Skill other = new Skill();
        when(repository.save(any(Skill.class))).thenReturn(other);

        Skill result = service.save(skill);

        assertSame(other, result);
        assertNotSame(skill, result);
    }

    @Test
    void save_propagatesRepositoryException() {
        when(repository.save(skill)).thenThrow(new IllegalStateException("db down"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.save(skill));

        assertEquals("db down", ex.getMessage());
        verify(repository).save(skill);
    }

    // ---- delete ----

    @Test
    void delete_callsDeleteByIdWithGivenId() {
        service.delete(7L);

        verify(repository).deleteById(7L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void delete_doesNotTouchOtherRepositoryMethods() {
        service.delete(7L);

        verify(repository, never()).delete(any(Skill.class));
        verify(repository, never()).deleteAll();
        verify(repository, never()).findById(anyLong());
    }

    @Test
    void delete_propagatesRepositoryException() {
        doThrow(new IllegalArgumentException("bad id")).when(repository).deleteById(5L);

        assertThrows(IllegalArgumentException.class, () -> service.delete(5L));
    }

    // ---- findAll ----

    @Test
    void findAll_returnsAllSkillsFromRepository() {
        Skill second = new Skill();
        second.setName("Layup");
        List<Skill> skills = List.of(skill, second);
        when(repository.findAll()).thenReturn(skills);

        List<Skill> result = service.findAll();

        assertEquals(2, result.size());
        assertSame(skills, result);
        verify(repository).findAll();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findAll_returnsEmptyListWhenNoSkills() {
        when(repository.findAll()).thenReturn(List.of());

        List<Skill> result = service.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---- count ----

    @Test
    void count_returnsRepositoryCountAsInt() {
        when(repository.count()).thenReturn(5L);

        assertEquals(5, service.count());
        verify(repository).count();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void count_returnsZeroWhenEmpty() {
        when(repository.count()).thenReturn(0L);

        assertEquals(0, service.count());
    }

    @Test
    void count_doesNotLoadEntities() {
        when(repository.count()).thenReturn(3L);

        service.count();

        verify(repository, never()).findAll();
    }
}
