package com.example.application.views.masterdetail;

import com.example.application.data.Profile;
import com.example.application.data.SamplePerson;
import com.example.application.services.ProfileService;
import com.example.application.services.SamplePersonService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

/**
 * Team Roster view for managing basketball player profiles.
 *
 * IMPORTANT: This view now automatically creates a training profile in the Skills system
 * whenever a new player is created or updated. This ensures that every player on the roster
 * immediately has a corresponding profile where skills can be assigned.
 *
 * The integration works as follows:
 * 1. Coach creates/edits a player in this view (name, physical stats, position)
 * 2. Upon saving, the system checks if a training profile exists for this player
 * 3. If no profile exists, one is automatically created and linked to the player
 * 4. The player's training profile then appears in the Skills view
 */
@PageTitle("Team Roster")
@Route("/:samplePersonID?/:action?(edit)")
@Menu(order = 0, icon = LineAwesomeIconUrl.USERS_SOLID)
@RouteAlias("")
@Uses(Icon.class)
public class MasterDetailView extends Div implements BeforeEnterObserver {

    private final String SAMPLEPERSON_ID = "samplePersonID";
    private final String SAMPLEPERSON_EDIT_ROUTE_TEMPLATE = "/%s/edit";

    private final Grid<SamplePerson> grid = new Grid<>(SamplePerson.class, false);

    // Form fields for player information
    private TextField firstName;
    private TextField lastName;

    // Physical attributes
    private NumberField height;
    private NumberField wingspan;
    private NumberField weight;

    private DatePicker dateOfBirth;
    private TextField role;
    private Checkbox important;

    private final Button cancel = new Button("Cancel");
    private final Button save = new Button("Save");

    private final BeanValidationBinder<SamplePerson> binder;

    private SamplePerson samplePerson;

    private final SamplePersonService samplePersonService;
    // NEW: ProfileService is now injected to enable automatic profile creation
    private final ProfileService profileService;

    public MasterDetailView(SamplePersonService samplePersonService, ProfileService profileService) {
        this.samplePersonService = samplePersonService;
        this.profileService = profileService;

        addClassNames("master-detail-view");

        // Create UI with split layout
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid columns
        grid.addColumn("firstName").setAutoWidth(true).setHeader("First Name");
        grid.addColumn("lastName").setAutoWidth(true).setHeader("Last Name");
        grid.addColumn("height").setAutoWidth(true).setHeader("Height (cm)");
        grid.addColumn("wingspan").setAutoWidth(true).setHeader("Wingspan (cm)");
        grid.addColumn("weight").setAutoWidth(true).setHeader("Weight (kg)");
        grid.addColumn("dateOfBirth").setAutoWidth(true).setHeader("Birth Date");
        grid.addColumn("role").setAutoWidth(true).setHeader("Position");

        LitRenderer<SamplePerson> importantRenderer = LitRenderer.<SamplePerson>of(
                        "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", important -> important.isImportant() ? "star" : "star-o")
                .withProperty("color", important -> important.isImportant()
                        ? "var(--lumo-primary-text-color)"
                        : "var(--lumo-disabled-text-color)");

        grid.addColumn(importantRenderer).setHeader("Star Player").setAutoWidth(true);

        grid.setItems(query -> samplePersonService.list(VaadinSpringDataHelpers.toSpringPageRequest(query)).stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(SAMPLEPERSON_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(MasterDetailView.class);
            }
        });

        // Configure Form binder
        binder = new BeanValidationBinder<>(SamplePerson.class);
        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        // UPDATED: Save button now includes automatic profile creation
        save.addClickListener(e -> {
            try {
                if (this.samplePerson == null) {
                    this.samplePerson = new SamplePerson();
                }

                // Save the player data first
                binder.writeBean(this.samplePerson);
                SamplePerson savedPerson = samplePersonService.save(this.samplePerson);

                // AUTOMATIC PROFILE CREATION:
                // Check if this player already has a training profile
                Optional<Profile> existingProfile = profileService.findByPlayer(savedPerson);

                if (existingProfile.isEmpty()) {
                    // No profile exists yet - create one automatically
                    Profile newProfile = new Profile();
                    newProfile.setName(savedPerson.getFirstName() + " " + savedPerson.getLastName());
                    newProfile.setDescription("Training profile for " + savedPerson.getRole());
                    newProfile.setPlayer(savedPerson);
                    profileService.save(newProfile);

                    // Notify the user that both player and profile were created
                    Notification.show("Player saved and training profile created! You can now assign skills in the Skills tab.");
                } else {
                    // Profile already exists - just update player data
                    // Also update the profile name in case the player's name changed
                    Profile profile = existingProfile.get();
                    profile.setName(savedPerson.getFirstName() + " " + savedPerson.getLastName());
                    profile.setDescription("Training profile for " + savedPerson.getRole());
                    profileService.save(profile);

                    Notification.show("Player data updated");
                }

                clearForm();
                refreshGrid();
                UI.getCurrent().navigate(MasterDetailView.class);

            } catch (ObjectOptimisticLockingFailureException exception) {
                Notification n = Notification.show(
                        "Error saving data. Another user has modified this record. Please refresh and try again.");
                n.setPosition(Position.MIDDLE);
                n.addThemeVariants(NotificationVariant.LUMO_ERROR);
            } catch (ValidationException validationException) {
                Notification.show("Please check that all values are valid before saving");
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Long> samplePersonId = event.getRouteParameters().get(SAMPLEPERSON_ID).map(Long::parseLong);
        if (samplePersonId.isPresent()) {
            Optional<SamplePerson> samplePersonFromBackend = samplePersonService.get(samplePersonId.get());
            if (samplePersonFromBackend.isPresent()) {
                populateForm(samplePersonFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested player was not found, ID = %s", samplePersonId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                refreshGrid();
                event.forwardTo(MasterDetailView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();

        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");

        height = new NumberField("Height (cm)");
        height.setMin(100);
        height.setMax(250);
        height.setStep(0.1);

        wingspan = new NumberField("Wingspan (cm)");
        wingspan.setMin(100);
        wingspan.setMax(300);
        wingspan.setStep(0.1);

        weight = new NumberField("Weight (kg)");
        weight.setMin(40);
        weight.setMax(200);
        weight.setStep(0.1);

        dateOfBirth = new DatePicker("Date of Birth");

        role = new TextField("Position");
        role.setPlaceholder("e.g., Point Guard, Center, Forward");

        important = new Checkbox("Star Player / Captain");

        formLayout.add(firstName, lastName, height, wingspan, weight, dateOfBirth, role, important);

        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getDataProvider().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(SamplePerson value) {
        this.samplePerson = value;
        binder.readBean(this.samplePerson);
    }
}