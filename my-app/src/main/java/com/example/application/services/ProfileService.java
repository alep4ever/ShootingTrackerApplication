package com.example.application.services;

import com.example.application.data.Profile;
import com.example.application.data.ProfileRepository;
import com.example.application.data.Skill;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service layer for Profile operations
 * Handles profile management and skill assignments
 */
@Service
public class ProfileService {

    private final ProfileRepository repository;

    public ProfileService(ProfileRepository repository) {
        this.repository = repository;
    }

    public Optional<Profile> get(Long id) {
        return repository.findById(id);
    }

    public Profile save(Profile entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public List<Profile> findAll() {
        return repository.findAll();
    }

    public int count() {
        return (int) repository.count();
    }

    /**
     * Adds a skill to a profile
     * @Transactional ensures the database operation completes fully or rolls back
     */
    @Transactional
    public Profile addSkillToProfile(Long profileId, Skill skill) {
        Optional<Profile> profileOpt = repository.findById(profileId);
        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            profile.addSkill(skill);
            return repository.save(profile);
        }
        return null;
    }

    /**
     * Removes a skill from a profile
     */
    @Transactional
    public Profile removeSkillFromProfile(Long profileId, Skill skill) {
        Optional<Profile> profileOpt = repository.findById(profileId);
        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            profile.removeSkill(skill);
            return repository.save(profile);
        }
        return null;
    }
}