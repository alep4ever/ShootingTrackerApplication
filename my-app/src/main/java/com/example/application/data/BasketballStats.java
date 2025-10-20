package com.example.application.data;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

/**
 * Entity representing a single basketball shooting statistics entry.
 *
 * Each record captures one shooting drill attempt, storing:
 * - Which player performed the drill
 * - How long it took them (in seconds)
 * - How many shots they attempted
 * - How many shots they made successfully
 * - When this drill was performed
 *
 * The relationship with SamplePerson is Many-to-One, meaning:
 * - One player can have many statistics entries (multiple practice sessions)
 * - Each statistics entry belongs to exactly one player
 *
 * By storing these in the database rather than memory, the statistics
 * persist across application restarts and can be queried, analyzed, and
 * used to track player improvement over time.
 */
@Entity
public class BasketballStats extends AbstractEntity {

    /**
     * The player who performed this shooting drill.
     * Many statistics entries can belong to one player.
     *
     * The @ManyToOne annotation creates a foreign key relationship,
     * storing the player's ID in this table.
     */
    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private SamplePerson player;

    /**
     * Time taken to complete the drill, measured in seconds.
     * For example, 45.5 means 45.5 seconds.
     */
    private Double timeInSeconds;

    /**
     * Total number of shot attempts during this drill.
     * This is the denominator when calculating shooting percentage.
     */
    private Integer shotsAttempted;

    /**
     * Number of successful shots (baskets made).
     * This is the numerator when calculating shooting percentage.
     * Should always be less than or equal to shotsAttempted.
     */
    private Integer shotsMade;

    /**
     * Timestamp recording when this drill was performed.
     * Automatically set when the record is created.
     * Allows tracking progress over time and sorting entries chronologically.
     */
    private LocalDateTime recordedAt;

    // Constructors

    public BasketballStats() {
        // Default constructor required by JPA
        // When created, automatically set the timestamp to now
        this.recordedAt = LocalDateTime.now();
    }

    public BasketballStats(SamplePerson player, Double timeInSeconds, Integer shotsAttempted, Integer shotsMade) {
        this.player = player;
        this.timeInSeconds = timeInSeconds;
        this.shotsAttempted = shotsAttempted;
        this.shotsMade = shotsMade;
        this.recordedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public SamplePerson getPlayer() {
        return player;
    }

    public void setPlayer(SamplePerson player) {
        this.player = player;
    }

    public Double getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(Double timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    public Integer getShotsAttempted() {
        return shotsAttempted;
    }

    public void setShotsAttempted(Integer shotsAttempted) {
        this.shotsAttempted = shotsAttempted;
    }

    public Integer getShotsMade() {
        return shotsMade;
    }

    public void setShotsMade(Integer shotsMade) {
        this.shotsMade = shotsMade;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    /**
     * Calculates the shooting percentage for this drill.
     * Returns a value between 0.0 and 100.0
     *
     * @return percentage of successful shots, or 0.0 if no shots were attempted
     */
    public double getShootingPercentage() {
        if (shotsAttempted == null || shotsAttempted == 0) {
            return 0.0;
        }
        return (shotsMade.doubleValue() / shotsAttempted.doubleValue()) * 100.0;
    }
}