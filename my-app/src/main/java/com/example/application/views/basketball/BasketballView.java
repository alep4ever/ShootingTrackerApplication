package com.example.application.views.basketball;

import com.example.application.data.BasketballSession;
import com.example.application.services.BasketballSessionService;
import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Route(value = "basketball", layout = MainLayout.class)
public class BasketballView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 3878656755224826959L;
    private final NumberField timeField = new NumberField("Zeit (Minuten)");
    private final NumberField shotsField = new NumberField("Anzahl Schüsse");
    private final NumberField hitsField = new NumberField("Anzahl getroffene Schüsse");
    private final Button confirmButton = new Button("Speichern");
    private final Chart chart = new Chart(ChartType.LINE);

    private final List<Double> times = new ArrayList<>();
    private final List<Integer> shots = new ArrayList<>();
    private final List<Integer> hits = new ArrayList<>();

    private final BasketballSessionService sessionService;

    public BasketballView(BasketballSessionService sessionService) {
        this.sessionService = sessionService;

        timeField.setStep(0.5);
        timeField.setMin(0);
        timeField.setHelperText("Zeit in Minuten (z.B. 1.5 für 1 Minute 30 Sekunden)");

        shotsField.setStep(1);
        shotsField.setMin(0);

        hitsField.setStep(1);
        hitsField.setMin(0);

        confirmButton.addClickListener(e -> saveData());

        add(timeField, shotsField, hitsField, confirmButton, chart);
        setupChart();
        loadExistingData();
    }

    private void loadExistingData() {
        List<BasketballSession> sessions = sessionService.findAll();
        for (BasketballSession session : sessions) {
            times.add(session.getTimeMinutes());
            shots.add(session.getShots());
            hits.add(session.getHits());
        }
        updateChart();
    }

    private void saveData() {
        Double t = timeField.getValue();
        Integer s = shotsField.getValue() != null ? shotsField.getValue().intValue() : null;
        Integer h = hitsField.getValue() != null ? hitsField.getValue().intValue() : null;

        if (t == null || s == null || h == null) {
            Notification notification = Notification.show("Bitte alle Felder ausfüllen!");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (h > s) {
            Notification notification = Notification.show("Getroffene Schüsse können nicht größer als Anzahl Schüsse sein!");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        BasketballSession session = new BasketballSession();
        session.setTimeMinutes(t);
        session.setShots(s);
        session.setHits(h);

        sessionService.save(session);

        times.add(t);
        shots.add(s);
        hits.add(h);

        updateChart();

        timeField.clear();
        shotsField.clear();
        hitsField.clear();

        Notification notification = Notification.show("Daten erfolgreich gespeichert!");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void setupChart() {
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Basketball Statistik");
        conf.getxAxis().setTitle("Versuch Nummer");
        conf.getyAxis().setTitle("Wert");
        conf.addSeries(new ListSeries("Zeit (Min)", times.toArray(new Number[0])));
        conf.addSeries(new ListSeries("Schüsse", shots.toArray(new Number[0])));
        conf.addSeries(new ListSeries("Getroffene Schüsse", hits.toArray(new Number[0])));
    }

    private void updateChart() {
        Configuration conf = chart.getConfiguration();
        conf.setSeries(
                new ListSeries("Zeit (Min)", times.toArray(new Number[0])),
                new ListSeries("Schüsse", shots.toArray(new Number[0])),
                new ListSeries("Getroffene Schüsse", hits.toArray(new Number[0]))
        );
        chart.setConfiguration(conf);
    }
}