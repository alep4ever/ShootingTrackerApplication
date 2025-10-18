package com.example.application.data;

import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class BasketballSession extends AbstractEntity {

    private Double timeMinutes;
    private Integer shots;
    private Integer hits;
    private LocalDateTime createdAt;

    public BasketballSession() {
        this.createdAt = LocalDateTime.now();
    }

    public Double getTimeMinutes() {
        return timeMinutes;
    }

    public void setTimeMinutes(Double timeMinutes) {
        this.timeMinutes = timeMinutes;
    }

    public Integer getShots() {
        return shots;
    }

    public void setShots(Integer shots) {
        this.shots = shots;
    }

    public Integer getHits() {
        return hits;
    }

    public void setHits(Integer hits) {
        this.hits = hits;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}