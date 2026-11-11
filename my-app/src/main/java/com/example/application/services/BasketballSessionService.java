package com.example.application.services;

import com.example.application.data.BasketballSession;
import com.example.application.data.BasketballSessionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BasketballSessionService {

    private final BasketballSessionRepository repository;

    public BasketballSessionService(BasketballSessionRepository repository) {
        this.repository = repository;
    }

    public Optional<BasketballSession> get(Long id) {
        return repository.findById(id);
    }

    public BasketballSession save(BasketballSession entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<BasketballSession> findAll() {
        return repository.findAll();
    }

    public int count() {
        return (int) repository.count();
    }

    /**
     * Calculates the hit rate of all sessions in percent.
     * Hit rate = all hits / all shots * 100, rounded to 2 decimals.
     *
     * Example: 10 shots / 5 hits and 10 shots / 8 hits = 13 / 20 = 65.0
     */
    public double calculateHitRate() {
        List<BasketballSession> sessions = repository.findAll();

        int totalShots = 0;
        int totalHits = 0;

        for (BasketballSession session : sessions) {
            Integer shots = session.getShots();
            Integer hits = session.getHits();

            // Skip a session without values
            if (shots == null || hits == null) {
                continue;
            }

            // Invalid data: throw an error
            if (shots < 0 || hits < 0) {
                throw new IllegalArgumentException("Shots and hits cannot be negative");
            }
            if (hits > shots) {
                throw new IllegalArgumentException("Hits cannot be more than shots");
            }

            totalShots = totalShots + shots;
            totalHits = totalHits + hits;
        }

        // No shots = hit rate 0 (otherwise division by 0)
        if (totalShots == 0) {
            return 0.0;
        }

        double hitRate = (double) totalHits / totalShots * 100;

        // Round to 2 decimals, e.g. 33.333 -> 33.33
        return Math.round(hitRate * 100) / 100.0;
    }
}
