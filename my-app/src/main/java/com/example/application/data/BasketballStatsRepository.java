package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository for accessing basketball statistics from the database.
 *
 * Spring Data JPA automatically implements these methods based on their names.
 * No need to write the actual SQL - Spring figures it out from the method signature.
 */
public interface BasketballStatsRepository extends JpaRepository<BasketballStats, Long> {

    /**
     * Finds all statistics entries for a specific player, ordered by when they were recorded.
     *
     * This method name tells Spring Data to:
     * - Find all BasketballStats WHERE player = ?
     * - ORDER BY recordedAt ASC (oldest first)
     *
     * @param player The player whose statistics we want to retrieve
     * @return List of all shooting drill entries for this player, in chronological order
     */
    List<BasketballStats> findByPlayerOrderByRecordedAtAsc(SamplePerson player);
}