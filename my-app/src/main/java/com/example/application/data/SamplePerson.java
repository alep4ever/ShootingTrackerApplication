package com.example.application.data;

import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
public class SamplePerson extends AbstractEntity {

    private String firstName;
    private String lastName;

    // Removed: email, phone, occupation
    // Added physical attributes for basketball tracking
    private Double height;      // Height in centimeters
    private Double wingspan;    // Wingspan in centimeters
    private Double weight;      // Weight in kilograms

    private LocalDate dateOfBirth;
    private String role;        // Position: Guard, Forward, Center, etc.
    private boolean important;  // Flag for star players or team captains

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // New getter/setter for height
    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    // New getter/setter for wingspan
    public Double getWingspan() {
        return wingspan;
    }

    public void setWingspan(Double wingspan) {
        this.wingspan = wingspan;
    }

    // New getter/setter for weight
    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isImportant() {
        return important;
    }

    public void setImportant(boolean important) {
        this.important = important;
    }

    // Convenience method to get full name for display in dropdowns
    public String getFullName() {
        return firstName + " " + lastName;
    }
}