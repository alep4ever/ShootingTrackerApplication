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
     * Clean, minimal interface without explanatory text.
     */
    private void createAdminSection() {
        adminSection.addClassName("admin-section");
        adminSection.getStyle()
                .set("background-color", "#f5f5f5")
                .set("padding", "20px")
                .set("border-radius", "8px")
                .set("margin-bottom", "20px");

        // Skill creation form - clean and focused
        HorizontalLayout skillForm = new HorizontalLayout();
        TextField skillName = new TextField("Skill Name");
        skillName.setPlaceholder("z.B. Crossover");
        skillName.setWidth("200px");

        TextArea skillDescription = new TextArea("Beschreibung");
        skillDescription.setPlaceholder("");
        skillDescription.setWidth("300px");
        skillDescription.setHeight("100px");

        // File upload for video demonstration
        MemoryBuffer buffer = new MemoryBuffer();
        Upload videoUpload = new Upload(buffer);
        videoUpload.setAcceptedFileTypes("video/mp4", ".mp4");
        videoUpload.setMaxFiles(1);
        videoUpload.setMaxFileSize(50 * 1024 * 1024); // 50MB max

        Button saveSkillBtn = new Button("Skill hinzufügen");
        saveSkillBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Handle video upload and skill creation
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

                try (InputStream inputStream = buffer.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                    inputStream.transferTo(outputStream);
                }

                // Create and save the skill to the library
                Skill skill = new Skill();
                skill.setName(skillName.getValue());
                skill.setDescription(skillDescription.getValue());
                skill.setVideoFileName(fileName);
                skillService.save(skill);

                Notification.show("Skill hinzugefügt")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Clear form for next entry
                skillName.clear();
                skillDescription.clear();

            } catch (Exception e) {
                Notification.show("Error beim speichern des Videos: " + e.getMessage())
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        skillForm.add(skillName, skillDescription, videoUpload, saveSkillBtn);
        skillForm.setAlignItems(Alignment.END);

        // Only add the form - no extra text or instructions
        adminSection.add(skillForm);
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

            H3 emptyTitle = new H3("Noch keine Spieler-Profile");

            emptyState.add(emptyTitle);
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

        Span skillsLabel = new Span("Skills");
        skillsLabel.getStyle()
                .set("font-weight", "bold")
                .set("color", "#333");
        skillsContainer.add(skillsLabel);

        // Display each skill assigned to this player
        if (profile.getSkills().isEmpty()) {
            Paragraph noSkills = new Paragraph("Noch keine Skills hinzugefügt");
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
        Button addSkillBtn = new Button("+ Skill hinzufügen");
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
            Notification.show("Skill wurde von " + profile.getName() + " entfernt");
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
        dialog.setHeaderTitle("Skills auswählen den du " + profile.getName() + " zuweisen willst");
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
                Button previewBtn = new Button("Video Vorschau");
                previewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
                previewBtn.addClickListener(e -> showSkillVideoDialog(skill));

                // Add to player button
                Button addBtn = new Button("Zum Spieler hinzufügen");
                addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                addBtn.addClickListener(e -> {
                    profileService.addSkillToProfile(profile.getId(), skill);
                    refreshProfiles();
                    Notification.show("Skill wurde " + profile.getName() + "hinzugefügt")
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    dialog.close();
                });

                skillRow.add(skillInfo, previewBtn, addBtn);
                content.add(skillRow);
            }
        }

        if (!hasAvailableSkills) {
            Paragraph noSkillsMsg = new Paragraph(
                    "" +
                            "Es wurden noch keine Skills erstellt" +
                            ""
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
            Paragraph noVideo = new Paragraph("Es gibt noch keine Vorschau zu diesem Skill");
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
        }

        // Note: We no longer create default profiles here
        // Profiles are now created automatically in Team Roster when players are added

}