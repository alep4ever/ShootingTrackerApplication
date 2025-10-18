package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository interface for Skill entity
 * Spring automatically creates the implementation with all basic CRUD operations
 */
public interface SkillRepository
        extends
        JpaRepository<Skill, Long>,
        JpaSpecificationExecutor<Skill> {
}
