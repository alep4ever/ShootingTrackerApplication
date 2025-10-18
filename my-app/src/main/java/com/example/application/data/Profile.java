package com.example.application.data;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a player profile or training focus
 * Each profile can have multiple skills associated with it
 */
@Entity
public class Profile extends AbstractEntity {

    private String name;
    private String description;

    // Many-to-Many relationship: One profile can have many skills,
    // and one skill can belong to many profiles
    // EAGER fetch ensures skills are loaded immediately with the profile
    // This prevents LazyInitializationException when accessing skills in the view
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "profile_skill", // Creates a junction table to link profiles and skills
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

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

    public Set<Skill> getSkills() {
        return skills;
    }

    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }

    // Helper method to add a skill to this profile
    public void addSkill(Skill skill) {
        this.skills.add(skill);
    }

    // Helper method to remove a skill from this profile
    public void removeSkill(Skill skill) {
        this.skills.remove(skill);
    }
}