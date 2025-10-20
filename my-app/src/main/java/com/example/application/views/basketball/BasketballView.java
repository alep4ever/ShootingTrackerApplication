package com.example.application.views.basketball;

import com.example.application.data.SamplePerson;
import com.example.application.services.SamplePersonService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basketball Statistics View with player-specific tracking.
 *
 * This view allows coaches and players to track shooting performance over multiple sessions.
 * Each player has their own separate statistics, enabling individual progress tracking.
 *
 * The view tracks:
 * - Time taken for each attempt (in seconds)
 * - Number of shots attempted
 * - Number of successful shots (hits)
 *
 * Data is stored in memory per session. For production use, consider persisting
 * this data to a database table with player_id foreign key relationships.
 */
@PageTitle("Basketball Stats")
@Route(value = "basketball", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.BASKETBALL_BALL_SOLID)
public class BasketballView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 3878656755224826959L;

    // UI Components
    private final ComboBox<SamplePerson> playerSelector = new ComboBox<>("Wähle ein Spieler aus");
    private final H3 currentPlayerLabel = new H3("Wähle ein Spieler aus um seine Stats zu sehen");

    private final NumberField timeField = new NumberField("Zeit (Sekunden)");
    private final NumberField shotsField = new NumberField("Anzahl Schüsse");
    private final NumberField hitsField = new NumberField("Anzahl getroffene Schüsse");
    private final Button confirmButton = new Button("Speichern");
    private final Chart chart = new Chart(ChartType.LINE);

    // Player statistics storage
    // In production, this should be persisted to a database table
    // Key: Player ID, Value: Map of statistic name to list of values
    private final Map<Long, PlayerStats> playerStatsMap = new HashMap<>();

    private SamplePerson currentPlayer;
    private final SamplePersonService personService;

    /**
     * Inner class to organize statistics for each player.
     * Keeps track of all recorded attempts with their times, shots, and hits.
     */
    private static class PlayerStats {
        List<Double> times = new ArrayList<>();
        List<Double> shots = new ArrayList<>();
        List<Double> hits = new ArrayList<>();
    }

    public BasketballView(SamplePersonService personService) {
        this.personService = personService;

        // Configure player selector to show all registered players
        playerSelector.setItems(personService.list(
                org.springframework.data.domain.PageRequest.of(0, 1000)
        ).getContent());

        // Display player names in the dropdown using the firstName + lastName format
        playerSelector.setItemLabelGenerator(person ->
                person.getFirstName() + " " + person.getLastName()
        );

        // When a player is selected, load their statistics and update the UI
        playerSelector.addValueChangeListener(event -> {
            currentPlayer = event.getValue();
            if (currentPlayer != null) {
                currentPlayerLabel.setText("Gerade werden die Stats von " + currentPlayer.getFullName() + " angezeigt");

                // Initialize statistics storage for new players if needed
                playerStatsMap.computeIfAbsent(currentPlayer.getId(), k -> new PlayerStats());

                // Refresh the chart to show this player's historical data
                updateChart();
            } else {
                currentPlayerLabel.setText("Please select a player to track statistics");
            }
        });

        // Configure input fields with sensible defaults and constraints
        timeField.setStep(1);
        timeField.setMin(0);


        shotsField.setStep(1);
        shotsField.setMin(0);


        hitsField.setStep(1);
        hitsField.setMin(0);


        confirmButton.addClickListener(e -> saveData());
        confirmButton.setDisableOnClick(true); // Prevent double-submission

        add(playerSelector, currentPlayerLabel, timeField, shotsField, hitsField, confirmButton, chart);
        setupChart();
    }

    /**
     * Saves the current drill statistics for the selected player.
     * Validates input and updates both the data storage and the visualization.
     */
    private void saveData() {
        try {
            // Validation: Ensure a player is selected
            if (currentPlayer == null) {
                Notification.show("Wähle zuerst ein Spieler aus", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Validation: Ensure all fields have values
            Double t = timeField.getValue();
            Double s = shotsField.getValue();
            Double h = hitsField.getValue();

            if (t == null || s == null || h == null) {
                Notification.show("Fülle zuerst alle Felder aus", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Validation: Hits cannot exceed total shots
            if (h > s) {
                Notification.show("Getroffene Schüsse können nicht mehr als Geschossene Schüsse sein", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Get the current player's statistics object
            PlayerStats stats = playerStatsMap.get(currentPlayer.getId());

            // Add the new data point to their historical records
            stats.times.add(t);
            stats.shots.add(s);
            stats.hits.add(h);

            // Update the chart visualization with the new data
            updateChart();

            // Clear the input fields for the next entry
            timeField.clear();
            shotsField.clear();
            hitsField.clear();

            // Calculate and display shooting percentage for immediate feedback
            double percentage = (h / s) * 100;
            Notification.show(
                    String.format("Gespeichert, deine Treffsicherheit ist: %.1f%%", percentage),
                    3000,
                    Notification.Position.BOTTOM_CENTER
            );

        } finally {
            confirmButton.setEnabled(true); // Re-enable the button
        }
    }

    /**
     * Initializes the chart with empty data series.
     * The chart will be populated when a player is selected and as data is added.
     */
    private void setupChart() {
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Basketball Statistik");
        conf.getxAxis().setTitle("Versuch Nummer");
        conf.getyAxis().setTitle("Wert");

        // Create three data series for tracking different metrics
        conf.addSeries(new ListSeries("Zeit (Sek)", new Number[0]));
        conf.addSeries(new ListSeries("Schüsse", new Number[0]));
        conf.addSeries(new ListSeries("Getroffene Schüsse", new Number[0]));
    }

    /**
     * Updates the chart to display the currently selected player's statistics.
     * If no player is selected or they have no data, the chart will be empty.
     */
    private void updateChart() {
        Configuration conf = chart.getConfiguration();

        if (currentPlayer != null) {
            PlayerStats stats = playerStatsMap.get(currentPlayer.getId());

            // Convert the ArrayLists to arrays for the chart library
            conf.setSeries(
                    new ListSeries("Zeit (Sek)", stats.times.toArray(new Number[0])),
                    new ListSeries("Schüsse", stats.shots.toArray(new Number[0])),
                    new ListSeries("Getroffene Schüsse", stats.hits.toArray(new Number[0]))
            );
        } else {
            // Clear the chart if no player is selected
            conf.setSeries(
                    new ListSeries("Zeit (Sek)", new Number[0]),
                    new ListSeries("Schüsse", new Number[0]),
                    new ListSeries("Getroffene Schüsse", new Number[0])
            );
        }

        chart.setConfiguration(conf);
    }
}