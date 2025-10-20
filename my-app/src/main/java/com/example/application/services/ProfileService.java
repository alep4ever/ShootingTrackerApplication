package com.example.application.services;

import com.example.application.data.Profile;
import com.example.application.data.ProfileRepository;
import com.example.application.data.SamplePerson;
import com.example.application.data.Skill;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing Profile entities.
 * Handles all business logic related to training profiles including
 * creating profiles for players, managing skill assignments, and
 * maintaining the relationship between players and their training profiles.
 */
@Service
public class ProfileService {

    private final ProfileRepository repository;

    public ProfileService(ProfileRepository repository) {
        this.repository = repository;
    }

    /**
     * Finds a profile by its unique ID.
     */
    public Optional<Profile> get(Long id) {
        return repository.findById(id);
    }

    /**
     * Saves or updates a profile in the database.
     */
    @Transactional
    public Profile save(Profile profile) {
        return repository.save(profile);
    }

    /**
     * Deletes a profile by its ID.
     */
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Retrieves all profiles from the database.
     * Used by the Skills view to display all player profiles.
     */
    public List<Profile> findAll() {
        return repository.findAllWithSkills();
    }

    /**
     * Counts the total number of profiles in the database.
     * Useful for checking if initial data setup is needed.
     */
    public long count() {
        return repository.count();
    }

    /**
     * NEW METHOD: Finds a profile associated with a specific player.
     *
     * This is critical for the integration between Team Roster and Skills view.
     * When creating a new player in the roster, we need to check if they already
     * have a profile before creating a new one, preventing duplicates.
     *
     * Returns Optional to safely handle cases where a player doesn't have a profile yet.
     * The caller should check if the Optional is empty before trying to access the profile.
     *
     * @param player The SamplePerson (basketball player) to find a profile for
     * @return Optional containing the profile if found, or Optional.empty() if none exists
     */
    public Optional<Profile> findByPlayer(SamplePerson player) {
        return repository.findByPlayer(player);
    }

    /**
     * Adds a skill to a player's profile.
     *
     * This method handles the many-to-many relationship between profiles and skills.
     * When a coach assigns a skill to a player in the Skills view, this method
     * updates the relationship in the database.
     *
     * @param profileId The ID of the profile to add the skill to
     * @param skill The skill to add (e.g., "Crossover Dribble")
     */
    @Transactional
    public void addSkillToProfile(Long profileId, Skill skill) {
        Optional<Profile> profileOpt = repository.findById(profileId);
        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            profile.addSkill(skill);
            repository.save(profile);
        }
    }

    /**
     * Removes a skill from a player's profile.
     *
     * Used when a coach decides a player no longer needs to focus on
     * a particular skill, or when a skill has been mastered.
     *
     * @param profileId The ID of the profile to remove the skill from
     * @param skill The skill to remove
     */
    @Transactional
    public void removeSkillFromProfile(Long profileId, Skill skill) {
        Optional<Profile> profileOpt = repository.findById(profileId);
        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            profile.removeSkill(skill);
            repository.save(profile);
        }
    }
}