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
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Skills management view - now integrated with Team Roster.
 *
 * IMPORTANT CHANGE: This view no longer allows creating profiles directly.
 * Instead, profiles are automatically created when players are added to the Team Roster.
 *
 * When you create a player named "Max" in the Team Roster view, a training profile
 * for Max is automatically created and will appear here. You can then assign
 * basketball skills (with video demonstrations) to each player's profile.
 *
 * The view displays all player profiles side by side, showing:
 * - Player name and position
 * - Skills assigned to that player
 * - Ability to add new skills from the library
 * - Ability to remove skills they've mastered or no longer need
 *
 * Admin section allows coaches to create new skills with video demonstrations
 * that can then be assigned to any player.
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

        // Create admin section at top for adding new skills to the library
        createAdminSection();

        // Create the main profiles display area
        createProfilesSection();

        add(adminSection, profilesLayout);

        // Initialize with default skills if database is empty
        initializeDefaultData();

        // Load and display all player profiles from Team Roster
        refreshProfiles();
    }

    /**
     * Creates the admin section where new skills can be added to the library.
     * Clean, minimal interface with optional video upload and skill editing.
     */
    private void createAdminSection() {
        adminSection.addClassName("admin-section");
        adminSection.getStyle()
                .set("background-color", "#f5f5f5")
                .set("padding", "20px")
                .set("border-radius", "8px")
                .set("margin-bottom", "20px");

        // SKILL CREATION FORM
        HorizontalLayout skillForm = new HorizontalLayout();

        TextField skillName = new TextField("Skill Name");
        skillName.setPlaceholder("e.g., Crossover Dribble");
        skillName.setWidth("200px");

        TextArea skillDescription = new TextArea("Description");
        skillDescription.setPlaceholder("Brief description of the skill");
        skillDescription.setWidth("300px");
        skillDescription.setHeight("100px");

        // File upload for optional video demonstration
        MemoryBuffer videoBuffer = new MemoryBuffer();
        Upload videoUpload = new Upload(videoBuffer);
        videoUpload.setAcceptedFileTypes("video/mp4", ".mp4");
        videoUpload.setMaxFiles(1);
        videoUpload.setMaxFileSize(50 * 1024 * 1024); // 50MB max
        videoUpload.setWidth("200px");

        // Track uploaded video filename
        final String[] uploadedFileName = {null};

        videoUpload.addSucceededListener(event -> {
            try {
                // Create videos directory if it doesn't exist
                Path videosDir = Paths.get("src/main/resources/static/videos");
                if (!Files.exists(videosDir)) {
                    Files.createDirectories(videosDir);
                }

                // Save the uploaded video file
                String fileName = event.getFileName();
                Path filePath = videosDir.resolve(fileName);

                try (InputStream inputStream = videoBuffer.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                    inputStream.transferTo(outputStream);
                }

                uploadedFileName[0] = fileName;
                Notification.show("Video uploaded successfully!");

            } catch (Exception e) {
                Notification.show("Error uploading video: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Save button - works with or without video
        Button saveSkillBtn = new Button("Add Skill");
        saveSkillBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveSkillBtn.addClickListener(e -> {
            if (skillName.isEmpty()) {
                Notification.show("Please enter a skill name", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Create and save the skill (video is optional)
            Skill skill = new Skill();
            skill.setName(skillName.getValue());
            skill.setDescription(skillDescription.getValue());
            skill.setVideoFileName(uploadedFileName[0]); // Can be null
            skillService.save(skill);

            Notification.show("Skill added successfully!")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Clear form for next entry
            skillName.clear();
            skillDescription.clear();
            uploadedFileName[0] = null;

            // Refresh skill list in edit section
            refreshEditSection();
        });

        skillForm.add(skillName, skillDescription, videoUpload, saveSkillBtn);
        skillForm.setAlignItems(Alignment.END);

        // SKILL EDIT SECTION
        VerticalLayout editSection = new VerticalLayout();
        editSection.addClassName("edit-section");
        editSection.getStyle()
                .set("margin-top", "30px")
                .set("padding", "15px")
                .set("background-color", "#fff")
                .set("border-radius", "8px")
                .set("border", "1px solid #ddd");

        Span editTitle = new Span("Edit Skills");
        editTitle.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "1.1em")
                .set("margin-bottom", "10px")
                .set("display", "block");

        VerticalLayout skillListContainer = new VerticalLayout();
        skillListContainer.setId("skill-list-container");
        skillListContainer.setPadding(false);
        skillListContainer.setSpacing(true);

        editSection.add(editTitle, skillListContainer);

        adminSection.add(skillForm, editSection);

        // Initial load of skills for editing
        refreshEditSection();
    }

    /**
     * Refreshes the edit section with current skills from the database.
     */
    private void refreshEditSection() {
        VerticalLayout container = (VerticalLayout) adminSection.getChildren()
                .filter(component -> component.getId().orElse("").equals("skill-list-container"))
                .findFirst()
                .orElse(null);

        if (container == null) {
            return;
        }

        container.removeAll();

        List<Skill> allSkills = skillService.findAll();

        if (allSkills.isEmpty()) {
            Paragraph noSkills = new Paragraph("No skills created yet");
            noSkills.getStyle()
                    .set("color", "#999")
                    .set("font-style", "italic");
            container.add(noSkills);
            return;
        }

        for (Skill skill : allSkills) {
            HorizontalLayout skillRow = new HorizontalLayout();
            skillRow.setWidthFull();
            skillRow.setAlignItems(Alignment.CENTER);
            skillRow.getStyle()
                    .set("padding", "10px")
                    .set("background-color", "#f9f9f9")
                    .set("border-radius", "4px")
                    .set("margin-bottom", "5px");

            Span skillNameSpan = new Span(skill.getName());
            skillNameSpan.getStyle()
                    .set("font-weight", "bold")
                    .set("flex-grow", "1");

            Button editBtn = new Button("Edit");
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e -> openEditSkillDialog(skill));

            Button deleteBtn = new Button("Delete");
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> {
                skillService.delete(skill.getId());
                Notification.show("Skill deleted");
                refreshEditSection();
                refreshProfiles();
            });

            skillRow.add(skillNameSpan, editBtn, deleteBtn);
            container.add(skillRow);
        }
    }

    /**
     * Opens a dialog to edit an existing skill.
     */
    private void openEditSkillDialog(Skill skill) {
        Dialog editDialog = new Dialog();
        editDialog.setHeaderTitle("Edit Skill");
        editDialog.setWidth("600px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);

        TextField nameField = new TextField("Skill Name");
        nameField.setValue(skill.getName());
        nameField.setWidthFull();

        TextArea descField = new TextArea("Description");
        descField.setValue(skill.getDescription() != null ? skill.getDescription() : "");
        descField.setWidthFull();
        descField.setHeight("100px");

        // Show current video status
        Span videoStatus = new Span();
        if (skill.getVideoFileName() != null) {
            videoStatus.setText("Current video: " + skill.getVideoFileName());
            videoStatus.getStyle().set("color", "#2196F3");
        } else {
            videoStatus.setText("No video attached");
            videoStatus.getStyle().set("color", "#999");
        }

        // Upload new video (optional)
        MemoryBuffer editVideoBuffer = new MemoryBuffer();
        Upload newVideoUpload = new Upload(editVideoBuffer);
        newVideoUpload.setAcceptedFileTypes("video/mp4", ".mp4");
        newVideoUpload.setMaxFiles(1);
        newVideoUpload.setMaxFileSize(50 * 1024 * 1024);
        newVideoUpload.setWidthFull();

        final String[] newVideoFileName = {null};

        newVideoUpload.addSucceededListener(event -> {
            try {
                Path videosDir = Paths.get("src/main/resources/static/videos");
                if (!Files.exists(videosDir)) {
                    Files.createDirectories(videosDir);
                }

                String fileName = event.getFileName();
                Path filePath = videosDir.resolve(fileName);

                try (InputStream inputStream = editVideoBuffer.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                    inputStream.transferTo(outputStream);
                }

                newVideoFileName[0] = fileName;
                videoStatus.setText("New video uploaded: " + fileName);
                videoStatus.getStyle().set("color", "#4CAF50");

            } catch (Exception ex) {
                Notification.show("Error uploading video: " + ex.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        content.add(nameField, descField, videoStatus, newVideoUpload);

        Button saveBtn = new Button("Save Changes", e -> {
            skill.setName(nameField.getValue());
            skill.setDescription(descField.getValue());

            // Update video only if a new one was uploaded
            if (newVideoFileName[0] != null) {
                skill.setVideoFileName(newVideoFileName[0]);
            }

            skillService.save(skill);
            Notification.show("Skill updated successfully!")
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshEditSection();
            refreshProfiles();
            editDialog.close();
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> editDialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        editDialog.add(content);
        editDialog.getFooter().add(cancelBtn, saveBtn);
        editDialog.open();
    }

    /**
     * Creates the horizontal scrolling section that displays all player profiles.
     */
    private void createProfilesSection() {
        profilesLayout.addClassName("profiles-layout");
        profilesLayout.setWidthFull();
        profilesLayout.getStyle()
                .set("overflow-x", "auto")
                .set("gap", "20px")
                .set("padding", "10px");
    }

    /**
     * Refreshes the display of all profiles.
     *
     * Loads all profiles from the database (which are now linked to players
     * from the Team Roster) and creates a card for each one.
     */
    private void refreshProfiles() {
        profilesLayout.removeAll();

        List<Profile> profiles = profileService.findAll();

        if (profiles.isEmpty()) {
            // Show helpful message if no profiles exist yet
            VerticalLayout emptyState = new VerticalLayout();
            emptyState.setAlignItems(Alignment.CENTER);
            emptyState.getStyle()
                    .set("padding", "40px")
                    .set("color", "#666");

            H3 emptyTitle = new H3("No Player Profiles Yet");
            Paragraph emptyText = new Paragraph(
                    "Go to Team Roster and add your first player. " +
                            "Their training profile will automatically appear here!"
            );
            emptyState.add(emptyTitle, emptyText);
            profilesLayout.add(emptyState);
        } else {
            // Display each player's profile card
            for (Profile profile : profiles) {
                profilesLayout.add(createProfileCard(profile));
            }
        }
    }

    /**
     * Creates a card component for a single player's profile.
     *
     * The card displays:
     * - Player name (from Team Roster)
     * - Player description/position
     * - All skills currently assigned to this player
     * - Button to add more skills from the library
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
                .set("max-width", "300px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // Player name (automatically set from Team Roster)
        H3 profileName = new H3(profile.getName());
        profileName.getStyle().set("margin-top", "0");

        // Player description (e.g., "Training profile for Point Guard")
        Paragraph profileDesc = new Paragraph(profile.getDescription());
        profileDesc.getStyle()
                .set("color", "#666")
                .set("margin", "5px 0 15px 0");

        // Display player info if linked to a SamplePerson
        if (profile.getPlayer() != null) {
            Div playerInfo = new Div();
            playerInfo.getStyle()
                    .set("background-color", "#f5f5f5")
                    .set("padding", "8px")
                    .set("border-radius", "4px")
                    .set("margin-bottom", "10px")
                    .set("font-size", "0.9em");

            Span positionLabel = new Span("Position: " + profile.getPlayer().getRole());
            positionLabel.getStyle().set("font-weight", "bold");
            playerInfo.add(positionLabel);
            card.add(playerInfo);
        }

        // Skills container
        VerticalLayout skillsContainer = new VerticalLayout();
        skillsContainer.addClassName("skills-container");
        skillsContainer.getStyle()
                .set("background-color", "#f9f9f9")
                .set("border-radius", "4px")
                .set("padding", "10px")
                .set("margin-top", "10px")
                .set("min-height", "100px");

        Span skillsLabel = new Span("Assigned Skills:");
        skillsLabel.getStyle()
                .set("font-weight", "bold")
                .set("color", "#333");
        skillsContainer.add(skillsLabel);

        // Display each skill assigned to this player
        if (profile.getSkills().isEmpty()) {
            Paragraph noSkills = new Paragraph("No skills assigned yet. Click 'Add Skill' below.");
            noSkills.getStyle()
                    .set("color", "#999")
                    .set("font-style", "italic")
                    .set("font-size", "0.9em");
            skillsContainer.add(noSkills);
        } else {
            for (Skill skill : profile.getSkills()) {
                skillsContainer.add(createSkillBadge(skill, profile));
            }
        }

        // Add skill button - opens dialog to choose from skill library
        Button addSkillBtn = new Button("+ Add Skill");
        addSkillBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addSkillBtn.addClickListener(e -> openSkillSelectionDialog(profile));

        card.add(profileName, profileDesc, skillsContainer, addSkillBtn);
        return card;
    }

    /**
     * Creates a badge/chip for a skill.
     *
     * The badge shows the skill name and includes:
     * - Click on skill name to preview video demonstration
     * - Remove button (×) to unassign the skill from this player
     */
    private HorizontalLayout createSkillBadge(Skill skill, Profile profile) {
        HorizontalLayout badge = new HorizontalLayout();
        badge.getStyle()
                .set("background-color", "#2196F3")
                .set("color", "white")
                .set("padding", "8px 12px")
                .set("border-radius", "20px")
                .set("margin", "5px")
                .set("cursor", "pointer")
                .set("align-items", "center");

        Span skillName = new Span(skill.getName());
        skillName.getStyle()
                .set("margin-right", "8px")
                .set("font-size", "0.9em");

        // Click skill name to show video demonstration
        badge.addClickListener(e -> showSkillVideoDialog(skill));

        // Remove button
        Button removeBtn = new Button("×");
        removeBtn.getStyle()
                .set("background", "rgba(255,255,255,0.2)")
                .set("color", "white")
                .set("border", "none")
                .set("cursor", "pointer")
                .set("padding", "2px 8px")
                .set("border-radius", "50%")
                .set("font-weight", "bold")
                .set("font-size", "1.2em")
                .set("line-height", "1");

        removeBtn.addClickListener(e -> {
            profileService.removeSkillFromProfile(profile.getId(), skill);
            refreshProfiles();
            Notification.show("Skill removed from " + profile.getName());
            e.getSource().getUI().ifPresent(ui -> ui.getPage().reload());
        });

        badge.add(skillName, removeBtn);
        return badge;
    }

    /**
     * Opens a dialog showing all available skills from the library.
     *
     * The dialog displays only skills that aren't already assigned to this player,
     * allowing the coach to add new skills from the complete library.
     */
    private void openSkillSelectionDialog(Profile profile) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Select Skills to Assign to " + profile.getName());
        dialog.setWidth("600px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);

        List<Skill> allSkills = skillService.findAll();
        boolean hasAvailableSkills = false;

        for (Skill skill : allSkills) {
            // Only show skills not already assigned to this player
            if (!profile.getSkills().contains(skill)) {
                hasAvailableSkills = true;

                HorizontalLayout skillRow = new HorizontalLayout();
                skillRow.setWidthFull();
                skillRow.setAlignItems(Alignment.CENTER);
                skillRow.getStyle()
                        .set("padding", "10px")
                        .set("border-bottom", "1px solid #eee");

                Div skillInfo = new Div();
                skillInfo.getStyle().set("flex-grow", "1");

                Span skillNameSpan = new Span(skill.getName());
                skillNameSpan.getStyle()
                        .set("font-weight", "bold")
                        .set("display", "block");
                skillInfo.add(skillNameSpan);

                if (skill.getDescription() != null && !skill.getDescription().isEmpty()) {
                    Paragraph desc = new Paragraph(skill.getDescription());
                    desc.getStyle()
                            .set("color", "#666")
                            .set("font-size", "0.9em")
                            .set("margin", "5px 0 0 0");
                    skillInfo.add(desc);
                }

                // Preview video button
                Button previewBtn = new Button("Preview Video");
                previewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
                previewBtn.addClickListener(e -> showSkillVideoDialog(skill));

                // Add to player button
                Button addBtn = new Button("Assign to Player");
                addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                addBtn.addClickListener(e -> {
                    profileService.addSkillToProfile(profile.getId(), skill);
                    refreshProfiles();
                    Notification.show("Skill assigned to " + profile.getName() + "!")
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    dialog.close();
                });

                skillRow.add(skillInfo, previewBtn, addBtn);
                content.add(skillRow);
            }
        }

        if (!hasAvailableSkills) {
            Paragraph noSkillsMsg = new Paragraph(
                    "All available skills have been assigned to this player, " +
                            "or no skills exist in the library yet. " +
                            "Add new skills using the admin section above."
            );
            noSkillsMsg.getStyle()
                    .set("color", "#666")
                    .set("font-style", "italic");
            content.add(noSkillsMsg);
        }

        Button closeBtn = new Button("Close", e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.add(content);
        dialog.getFooter().add(closeBtn);
        dialog.open();
    }

    /**
     * Shows a dialog with the skill's video demonstration.
     *
     * This allows players to watch the proper technique before practicing,
     * and serves as a reference for coaches when teaching the skill.
     */
    private void showSkillVideoDialog(Skill skill) {
        Dialog videoDialog = new Dialog();
        videoDialog.setHeaderTitle(skill.getName());
        videoDialog.setWidth("700px");
        videoDialog.setHeight("600px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);

        // Display skill description
        if (skill.getDescription() != null && !skill.getDescription().isEmpty()) {
            Paragraph desc = new Paragraph(skill.getDescription());
            desc.getStyle()
                    .set("background-color", "#f5f5f5")
                    .set("padding", "10px")
                    .set("border-radius", "4px")
                    .set("margin-bottom", "15px");
            content.add(desc);
        }

        // Embed video player
        if (skill.getVideoFileName() != null && !skill.getVideoFileName().isEmpty()) {
            String videoUrl = "/api/videos/" + skill.getVideoFileName();

            Div videoContainer = new Div();
            videoContainer.getStyle()
                    .set("width", "100%")
                    .set("background-color", "#000");

            videoContainer.getElement().setProperty("innerHTML",
                    "<video width='100%' height='400' controls>" +
                            "<source src='" + videoUrl + "' type='video/mp4'>" +
                            "Your browser does not support the video tag." +
                            "</video>");

            content.add(videoContainer);
        } else {
            Paragraph noVideo = new Paragraph("No video demonstration available for this skill yet.");
            noVideo.getStyle()
                    .set("color", "#999")
                    .set("font-style", "italic")
                    .set("text-align", "center")
                    .set("padding", "40px");
            content.add(noVideo);
        }

        Button closeBtn = new Button("Close", e -> videoDialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        videoDialog.add(content);
        videoDialog.getFooter().add(closeBtn);
        videoDialog.open();
    }

    /**
     * Initializes the database with some default basketball skills.
     *
     * Only runs if the skill library is empty. These default skills can be
     * assigned to any player, and videos can be added later through the admin section.
     */
    private void initializeDefaultData() {
        if (skillService.count() == 0) {
            // Create default basketball skills (coaches can add videos later)
            Skill crossover = new Skill();
            crossover.setName("Crossover Dribble");
            crossover.setDescription("Quick ball handling move to change direction and beat defenders");
            skillService.save(crossover);

            Skill shooting = new Skill();
            shooting.setName("Jump Shot Technique");
            shooting.setDescription("Proper form for mid-range and three-point shooting");
            skillService.save(shooting);

            Skill layup = new Skill();
            layup.setName("Layup Fundamentals");
            layup.setDescription("Basic layup technique with both hands, focusing on footwork and finish");
            skillService.save(layup);

            Skill defense = new Skill();
            defense.setName("Defensive Stance");
            defense.setDescription("Proper defensive positioning, footwork, and body control");
            skillService.save(defense);

            Notification.show("Default basketball skills created! Add videos through the admin section.");
        }

        // Note: We no longer create default profiles here
        // Profiles are now created automatically in Team Roster when players are added
    }
}