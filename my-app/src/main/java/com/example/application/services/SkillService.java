package com.example.application.services;

import com.example.application.data.Skill;
import com.example.application.data.SkillRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Service layer for Skill operations
 * This sits between the UI and the database, handling business logic
 */
@Service
public class SkillService {

    private final SkillRepository repository;

    public SkillService(SkillRepository repository) {
        this.repository = repository;
    }

    public Optional<Skill> get(Long id) {
        return repository.findById(id);
    }

    public Skill save(Skill entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Skill> findAll() {
        return repository.findAll();
    }

    public int count() {
        return (int) repository.count();
    }
}