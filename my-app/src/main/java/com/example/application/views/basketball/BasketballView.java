package com.example.application.views.basketball;

import com.example.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.ChartType;
import com.vaadin.flow.component.charts.model.Configuration;
import com.vaadin.flow.component.charts.model.ListSeries;
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
	private final NumberField timeField = new NumberField("Zeit (Sekunden)");
	private final NumberField shotsField = new NumberField("Anzahl Schüsse");
	private final NumberField hitsField = new NumberField("Anzahl getroffene Schüsse");
	private final Button confirmButton = new Button("Speichern");
	private final Chart chart = new Chart(ChartType.LINE);

    private final List<Double> times = new ArrayList<>();
    private final List<Double> shots = new ArrayList<>();
    private final List<Double> hits = new ArrayList<>();

	public BasketballView() {
		timeField.setStep(1);
		timeField.setMin(0);

		shotsField.setStep(1);
		shotsField.setMin(0);

		hitsField.setStep(1);
		hitsField.setMin(0);

        confirmButton.addClickListener(e -> saveData());

        add(timeField, shotsField, hitsField, confirmButton, chart);
        setupChart();
    }

	private void saveData() {
		// Werte validieren
		Double t = timeField.getValue();
		Double s = shotsField.getValue();
		Double h = hitsField.getValue();
		if (t == null || s == null || h == null) return;

		times.add(t);
		shots.add(s);
		hits.add(h);

		updateChart();
	}

    private void setupChart() {
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Basketball Statistik");
        conf.getxAxis().setTitle("Versuch Nummer");
        conf.getyAxis().setTitle("Wert");
        conf.addSeries(new ListSeries("Zeit (Sek)", times.toArray(new Number[0])));
        conf.addSeries(new ListSeries("Schüsse", shots.toArray(new Number[0])));
        conf.addSeries(new ListSeries("Getroffene Schüsse", hits.toArray(new Number[0])));
    }

    private void updateChart() {
        Configuration conf = chart.getConfiguration();
        conf.setSeries(
                new ListSeries("Zeit (Sek)", times.toArray(new Number[0])),
                new ListSeries("Schüsse", shots.toArray(new Number[0])),
                new ListSeries("Getroffene Schüsse", hits.toArray(new Number[0]))
        );
        chart.setConfiguration(conf);
    }
}