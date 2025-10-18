package com.example.application.views.skill;

import com.example.application.data.Profile;
import com.example.application.data.Skill;
import com.example.application.services.ProfileService;
import com.example.application.services.SkillService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main view for the Skills management system
 * Shows profiles side by side, each with their assigned skills
 * Allows adding new skills and assigning them to profiles
 */
@PageTitle("Skills")
@Route(value = "skills", layout = MainLayout.class)
@Menu(order = 2, icon = LineAwesomeIconUrl.BASKETBALL_BALL_SOLID)
public class SkillView extends VerticalLayout {

    private final SkillService skillService;
    private final ProfileService profileService;

    private final HorizontalLayout profilesLayout = new HorizontalLayout();
    private final VerticalLayout adminSection = new VerticalLayout();

    public SkillView(SkillService skillService, ProfileService profileService) {
        this.skillService = skillService;
        this.profileService = profileService;

        addClassName("skill-view");
        setSizeFull();

        // Create admin section at top for adding new skills
        createAdminSection();

        // Create the main profiles display
        createProfilesSection();

        add(adminSection, profilesLayout);

        // Initialize with some default data if database is empty
        initializeDefaultData();

        // Load and display existing profiles
        refreshProfiles();
    }

    /**
     * Creates the admin section where new skills and profiles can be added
     */
    private void createAdminSection() {
        adminSection.addClassName("admin-section");
        adminSection.getStyle()
                .set("background-color", "#f5f5f5")
                .set("padding", "20px")
                .set("border-radius", "8px")
                .set("margin-bottom", "20px");

        H3 adminTitle = new H3("Admin: Add New Skills & Profiles");

        // Skill creation form
        HorizontalLayout skillForm = new HorizontalLayout();
        TextField skillName = new TextField("Skill Name");
        skillName.setPlaceholder("e.g., Crossover");

        TextArea skillDescription = new TextArea("Description");
        skillDescription.setPlaceholder("Brief description of the skill");
        skillDescription.setWidth("300px");

        // File upload for video
        MemoryBuffer buffer = new MemoryBuffer();
        Upload videoUpload = new Upload(buffer);
        videoUpload.setAcceptedFileTypes("video/mp4", ".mp4");
        videoUpload.setMaxFiles(1);

        Button saveSkillBtn = new Button("Save Skill");
        saveSkillBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        videoUpload.addSucceededListener(event -> {
            try {
                // Create videos directory if it doesn't exist
                Path videosDir = Paths.get("src/main/resources/static/videos");
                if (!Files.exists(videosDir)) {
                    Files.createDirectories(videosDir);
                }

                // Save the uploaded file
                String fileName = event.getFileName();
                Path filePath = videosDir.resolve(fileName);

                try (InputStream inputStream = buffer.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                    inputStream.transferTo(outputStream);
                }

                // Create and save the skill
                Skill skill = new Skill();
                skill.setName(skillName.getValue());
                skill.setDescription(skillDescription.getValue());
                skill.setVideoFileName(fileName);
                skillService.save(skill);

                Notification.show("Skill '" + skillName.getValue() + "' added successfully!")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Clear form
                skillName.clear();
                skillDescription.clear();

            } catch (Exception e) {
                Notification.show("Error saving video: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        skillForm.add(skillName, skillDescription, videoUpload, saveSkillBtn);
        skillForm.setAlignItems(Alignment.END);

        // Profile creation form
        HorizontalLayout profileForm = new HorizontalLayout();
        TextField profileName = new TextField("Profile Name");
        profileName.setPlaceholder("e.g., Player 1");

        TextField profileDesc = new TextField("Description");
        profileDesc.setPlaceholder("e.g., Guard position");

        Button saveProfileBtn = new Button("Create Profile");
        saveProfileBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveProfileBtn.addClickListener(e -> {
            if (!profileName.isEmpty()) {
                Profile profile = new Profile();
                profile.setName(profileName.getValue());
                profile.setDescription(profileDesc.getValue());
                profileService.save(profile);

                Notification.show("Profile created!")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                profileName.clear();
                profileDesc.clear();
                refreshProfiles();
            }
        });

        profileForm.add(profileName, profileDesc, saveProfileBtn);
        profileForm.setAlignItems(Alignment.END);

        adminSection.add(adminTitle, new H4("Add New Skill:"), skillForm,
                new H4("Create New Profile:"), profileForm);
    }

    /**
     * Creates the section that displays all profiles horizontally
     */
    private void createProfilesSection() {
        profilesLayout.addClassName("profiles-layout");
        profilesLayout.setWidthFull();
        profilesLayout.getStyle()
                .set("overflow-x", "auto")
                .set("gap", "20px");
    }

    /**
     * Refreshes the display of all profiles and their skills
     */
    private void refreshProfiles() {
        profilesLayout.removeAll();

        List<Profile> profiles = profileService.findAll();

        for (Profile profile : profiles) {
            profilesLayout.add(createProfileCard(profile));
        }
    }

    /**
     * Creates a card component for a single profile
     * Shows profile name, description, and all assigned skills
     */
    private VerticalLayout createProfileCard(Profile profile) {
        VerticalLayout card = new VerticalLayout();
        card.addClassName("profile-card");
        card.getStyle()
                .set("border", "2px solid #e0e0e0")
                .set("border-radius", "8px")
                .set("padding", "15px")
                .set("background-color", "white")
                .set("min-width", "300px")
                .set("max-width", "300px");

        H3 profileName = new H3(profile.getName());
        Paragraph profileDesc = new Paragraph(profile.getDescription());
        profileDesc.getStyle().set("color", "#666");

        // Skills container
        VerticalLayout skillsContainer = new VerticalLayout();
        skillsContainer.addClassName("skills-container");
        skillsContainer.getStyle()
                .set("background-color", "#f9f9f9")
                .set("border-radius", "4px")
                .set("padding", "10px")
                .set("margin-top", "10px");

        Span skillsLabel = new Span("Skills:");
        skillsLabel.getStyle().set("font-weight", "bold");
        skillsContainer.add(skillsLabel);

        // Display each skill
        for (Skill skill : profile.getSkills()) {
            skillsContainer.add(createSkillBadge(skill, profile));
        }

        // Add skill button
        Button addSkillBtn = new Button("+ Add Skill");
        addSkillBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addSkillBtn.addClickListener(e -> openSkillSelectionDialog(profile));

        card.add(profileName, profileDesc, skillsContainer, addSkillBtn);
        return card;
    }

    /**
     * Creates a badge/chip for a skill with video preview on click
     */
    private HorizontalLayout createSkillBadge(Skill skill, Profile profile) {
        HorizontalLayout badge = new HorizontalLayout();
        badge.getStyle()
                .set("background-color", "#2196F3")
                .set("color", "white")
                .set("padding", "5px 10px")
                .set("border-radius", "16px")
                .set("margin", "5px")
                .set("cursor", "pointer");

        Span skillName = new Span(skill.getName());

        // Click to show video
        badge.addClickListener(e -> showSkillVideoDialog(skill));

        // Remove button
        Button removeBtn = new Button("×");
        removeBtn.getStyle()
                .set("background", "transparent")
                .set("color", "white")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("padding", "0 5px");
        removeBtn.addClickListener(e -> {
            profileService.removeSkillFromProfile(profile.getId(), skill);
            refreshProfiles();
            Notification.show("Skill removed from profile");
        });

        badge.add(skillName, removeBtn);
        return badge;
    }

    /**
     * Opens a dialog showing all available skills to add to a profile
     */
    private void openSkillSelectionDialog(Profile profile) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Select Skills to Add");

        VerticalLayout content = new VerticalLayout();
        List<Skill> allSkills = skillService.findAll();

        for (Skill skill : allSkills) {
            // Check if skill is already in profile
            if (!profile.getSkills().contains(skill)) {
                HorizontalLayout skillRow = new HorizontalLayout();
                skillRow.setWidthFull();
                skillRow.setAlignItems(Alignment.CENTER);

                Div skillInfo = new Div();
                skillInfo.add(new Span(skill.getName()));
                if (skill.getDescription() != null) {
                    Paragraph desc = new Paragraph(skill.getDescription());
                    desc.getStyle().set("color", "#666").set("font-size", "0.9em");
                    skillInfo.add(desc);
                }

                Button previewBtn = new Button("Preview Video");
                previewBtn.addClickListener(e -> showSkillVideoDialog(skill));

                Button addBtn = new Button("Add");
                addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                addBtn.addClickListener(e -> {
                    profileService.addSkillToProfile(profile.getId(), skill);
                    refreshProfiles();
                    Notification.show("Skill added to profile!");
                    dialog.close();
                });

                skillRow.add(skillInfo, previewBtn, addBtn);
                skillRow.setFlexGrow(1, skillInfo);
                content.add(skillRow);
            }
        }

        Button closeBtn = new Button("Close", e -> dialog.close());

        dialog.add(content);
        dialog.getFooter().add(closeBtn);
        dialog.open();
    }

    /**
     * Shows a dialog with the skill's demonstration video
     */
    private void showSkillVideoDialog(Skill skill) {
        Dialog videoDialog = new Dialog();
        videoDialog.setHeaderTitle(skill.getName());
        videoDialog.setWidth("600px");

        VerticalLayout content = new VerticalLayout();

        if (skill.getDescription() != null) {
            Paragraph desc = new Paragraph(skill.getDescription());
            content.add(desc);
        }

        // Create HTML5 video element
        if (skill.getVideoFileName() != null) {
            String videoUrl = "/videos/" + skill.getVideoFileName();

            Div videoContainer = new Div();
            videoContainer.getElement().setProperty("innerHTML",
                    "<video width='100%' controls>" +
                            "<source src='" + videoUrl + "' type='video/mp4'>" +
                            "Your browser does not support the video tag." +
                            "</video>");

            content.add(videoContainer);
        } else {
            content.add(new Paragraph("No video available for this skill."));
        }

        Button closeBtn = new Button("Close", e -> videoDialog.close());

        videoDialog.add(content);
        videoDialog.getFooter().add(closeBtn);
        videoDialog.open();
    }

    /**
     * Initializes the database with some default skills and profiles
     * Only runs if the database is empty
     */
    private void initializeDefaultData() {
        if (skillService.count() == 0) {
            // Create default skills (without videos for now)
            Skill crossover = new Skill();
            crossover.setName("Crossover");
            crossover.setDescription("Quick ball handling move to change direction");
            skillService.save(crossover);

            Skill shooting = new Skill();
            shooting.setName("Plain Shooting");
            shooting.setDescription("Basic shooting technique");
            skillService.save(shooting);

            Notification.show("Default skills created. Add videos through the admin section!");
        }

        if (profileService.count() == 0) {
            // Create a default profile
            Profile defaultProfile = new Profile();
            defaultProfile.setName("Player 1");
            defaultProfile.setDescription("Default training profile");
            profileService.save(defaultProfile);
        }
    }
}