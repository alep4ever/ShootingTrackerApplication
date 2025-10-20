package com.example.application.services;

import com.example.application.data.BasketballStats;
import com.example.application.data.BasketballStatsRepository;
import com.example.application.data.SamplePerson;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer for managing basketball shooting statistics.
 *
 * This service handles all business logic related to recording and retrieving
 * shooting drill performance data. By using a service layer, we keep the UI code
 * clean and separate the data access logic from the presentation logic.
 */
@Service
public class BasketballStatsService {

    private final BasketballStatsRepository repository;

    public BasketballStatsService(BasketballStatsRepository repository) {
        this.repository = repository;
    }

    /**
     * Saves a new basketball statistics entry to the database.
     *
     * @param stats The statistics entry to save
     * @return The saved entry (with ID assigned by the database)
     */
    @Transactional
    public BasketballStats save(BasketballStats stats) {
        return repository.save(stats);
    }

    /**
     * Retrieves all statistics entries for a specific player.
     * Results are ordered chronologically (oldest to newest) so you can
     * see the player's improvement trajectory over time.
     *
     * @param player The player whose statistics to retrieve
     * @return List of all shooting drill entries for this player
     */
    public List<BasketballStats> getStatsForPlayer(SamplePerson player) {
        return repository.findByPlayerOrderByRecordedAtAsc(player);
    }

    /**
     * Deletes a statistics entry from the database.
     * Useful if a drill was recorded incorrectly and needs to be removed.
     *
     * @param id The ID of the statistics entry to delete
     */
    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Counts the total number of statistics entries in the database.
     * Useful for dashboard displays or performance monitoring.
     *
     * @return Total count of all recorded shooting drills across all players
     */
    public long count() {
        return repository.count();
    }
}