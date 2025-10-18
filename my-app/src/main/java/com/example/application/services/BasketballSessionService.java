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
}