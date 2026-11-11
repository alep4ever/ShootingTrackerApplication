package com.example.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.application.data.BasketballStats;
import com.example.application.data.BasketballStatsRepository;
import com.example.application.data.SamplePerson;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for BasketballStatsService. The BasketballStatsRepository
 * (database) is fully mocked, so no real DB is contacted.
 */
@ExtendWith(MockitoExtension.class)
class BasketballStatsServiceTest {

    @Mock
    private BasketballStatsRepository repository;

    @InjectMocks
    private BasketballStatsService service;

    private SamplePerson player;
    private BasketballStats stats;

    @BeforeEach
    void setUp() {
        player = mock(SamplePerson.class);
        stats = new BasketballStats(player, 45.5, 10, 7);
    }

    // ---- save ----

    @Test
    void save_delegatesToRepositoryAndReturnsSavedEntity() {
        BasketballStats saved = new BasketballStats(player, 45.5, 10, 7);
        when(repository.save(stats)).thenReturn(saved);

        BasketballStats result = service.save(stats);

        assertSame(saved, result);
        verify(repository).save(stats);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void save_returnsWhatRepositoryReturns_notTheInput() {
        BasketballStats other = new BasketballStats();
        when(repository.save(any(BasketballStats.class))).thenReturn(other);

        BasketballStats result = service.save(stats);

        assertSame(other, result);
        assertNotSame(stats, result);
    }

    @Test
    void save_propagatesRepositoryException() {
        when(repository.save(stats)).thenThrow(new IllegalStateException("db down"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.save(stats));

        assertEquals("db down", ex.getMessage());
    }

    // ---- getStatsForPlayer ----

    @Test
    void getStatsForPlayer_returnsChronologicalListFromRepository() {
        BasketballStats second = new BasketballStats(player, 40.0, 10, 8);
        List<BasketballStats> list = List.of(stats, second);
        when(repository.findByPlayerOrderByRecordedAtAsc(player)).thenReturn(list);

        List<BasketballStats> result = service.getStatsForPlayer(player);

        assertSame(list, result);
        assertEquals(2, result.size());
        assertSame(stats, result.get(0));
        assertSame(second, result.get(1));
        verify(repository).findByPlayerOrderByRecordedAtAsc(player);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getStatsForPlayer_returnsEmptyListWhenPlayerHasNoStats() {
        when(repository.findByPlayerOrderByRecordedAtAsc(player)).thenReturn(List.of());

        List<BasketballStats> result = service.getStatsForPlayer(player);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getStatsForPlayer_usesGivenPlayerOnly() {
        SamplePerson otherPlayer = mock(SamplePerson.class);
        when(repository.findByPlayerOrderByRecordedAtAsc(player)).thenReturn(List.of(stats));

        service.getStatsForPlayer(player);

        verify(repository).findByPlayerOrderByRecordedAtAsc(player);
        verify(repository, never()).findByPlayerOrderByRecordedAtAsc(otherPlayer);
        verify(repository, never()).findAll();
    }

    // ---- delete ----

    @Test
    void delete_callsDeleteByIdWithGivenId() {
        service.delete(3L);

        verify(repository).deleteById(3L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void delete_doesNotTouchOtherRepositoryMethods() {
        service.delete(3L);

        verify(repository, never()).delete(any(BasketballStats.class));
        verify(repository, never()).deleteAll();
        verify(repository, never()).findById(anyLong());
    }

    @Test
    void delete_propagatesRepositoryException() {
        doThrow(new IllegalArgumentException("bad id")).when(repository).deleteById(5L);

        assertThrows(IllegalArgumentException.class, () -> service.delete(5L));
    }

    // ---- count ----

    @Test
    void count_returnsRepositoryCount() {
        when(repository.count()).thenReturn(12L);

        assertEquals(12L, service.count());
        verify(repository).count();
        verifyNoMoreInteractions(repository);
    }

    @Test
    void count_returnsZeroWhenEmpty() {
        when(repository.count()).thenReturn(0L);

        assertEquals(0L, service.count());
    }

    @Test
    void count_supportsValuesBeyondIntRange() {
        long big = Integer.MAX_VALUE + 10L;
        when(repository.count()).thenReturn(big);

        assertEquals(big, service.count());
    }

    @Test
    void count_doesNotLoadEntities() {
        when(repository.count()).thenReturn(1L);

        service.count();

        verify(repository, never()).findAll();
    }
}
