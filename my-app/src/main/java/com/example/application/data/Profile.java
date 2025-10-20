package com.example.application.data;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import java.util.HashSet;
import java.util.Set;

/**
 * Profile entity that represents a training profile for a basketball player.
 * Each profile is now directly linked to a SamplePerson (player) from the roster.
 *
 * The profile maintains a collection of skills that the player needs to work on,
 * with each skill potentially having video demonstrations and descriptions.
 *
 * Key relationship: Profile has a one-to-one relationship with SamplePerson,
 * meaning each player has exactly one profile, and each profile belongs to one player.
 */
@Entity
public class Profile extends AbstractEntity {

    private String name;
    private String description;

    /**
     * One-to-one relationship with SamplePerson (the basketball player).
     * This links the training profile directly to a player in the team roster.
     *
     * When a player is created in the Team Roster view, a profile is automatically
     * created with this relationship established. This ensures every player
     * has a corresponding training profile in the Skills system.
     */
    @OneToOne
    @JoinColumn(name = "player_id", unique = true)
    private SamplePerson player;

    /**
     * Many-to-many relationship with skills.
     * A profile can have multiple skills, and a skill can be assigned to multiple profiles.
     *
     * This is managed through a join table called "profile_skills" which stores
     * the relationships between profiles and skills. This allows coaches to assign
     * the same skill (e.g., "Crossover Dribble") to multiple players while maintaining
     * separate video files and descriptions for each skill.
     *
     * IMPORTANT: Uses FetchType.EAGER to prevent LazyInitializationException.
     * This means whenever a Profile is loaded, its skills are loaded immediately
     * in the same query. This is necessary because the SkillView needs to access
     * the skills collection after the database session has closed.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "profile_skills",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    // Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public SamplePerson getPlayer() {
        return player;
    }

    public void setPlayer(SamplePerson player) {
        this.player = player;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }

    /**
     * Helper method to add a single skill to this profile.
     * Maintains the bidirectional relationship properly.
     */
    public void addSkill(Skill skill) {
        this.skills.add(skill);
    }

    /**
     * Helper method to remove a skill from this profile.
     */
    public void removeSkill(Skill skill) {
        this.skills.remove(skill);
    }
}