package com.example.application.data;

import jakarta.persistence.Entity;

/**
 * Represents a basketball skill (e.g., crossover, shooting)
 * Each skill has a name, description, and a video file that demonstrates it
 */
@Entity
public class Skill extends AbstractEntity {

    private String name;
    private String description;
    // Stores just the filename, e.g. "crossover.mp4"
    // The actual file should be in src/main/resources/static/videos/
    private String videoFileName;

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

    public String getVideoFileName() {
        return videoFileName;
    }

    public void setVideoFileName(String videoFileName) {
        this.videoFileName = videoFileName;
    }
}