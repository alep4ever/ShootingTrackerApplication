package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository interface for Profile entity
 * Provides all standard database operations for profiles
 */
public interface ProfileRepository
        extends
        JpaRepository<Profile, Long>,
        JpaSpecificationExecutor<Profile> {
}