package com.example.application.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByPlayer(SamplePerson player);

    // Custom query that explicitly loads skills with profiles
    @Query("SELECT DISTINCT p FROM Profile p LEFT JOIN FETCH p.skills")
    List<Profile> findAllWithSkills();
}