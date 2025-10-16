package com.example.application.views.shootingdetails;

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

@Route(value = "shootingdetails", layout = MainLayout.class) // Annahme: MainLayout ist dein Master-Detail Layout
public class ShootingDetails extends VerticalLayout {

	@Serial
	private static final long serialVersionUID = 3878656755224826959L;
	private final NumberField timeField = new NumberField("Zeit (Sekunden)");
	private final NumberField shotsField = new NumberField("Anzahl Schüsse");
	private final NumberField hitsField = new NumberField("Anzahl getroffene Schüsse");
	private final Button confirmButton = new Button("Speichern");
	private final Chart chart = new Chart(ChartType.LINE);

	// Datenhaltung für grafische Darstellung
	private final List<Double> times = new ArrayList<>();
	private final List<Double> shots = new ArrayList<>();
	private final List<Double> hits = new ArrayList<>();

	public  ShootingDetails() {
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
		conf.getxAxis().setTitle("V ersuch Nummer");
		conf.getyAxis().setTitle("Wert");
		conf.addSeries(new ListSeries("Zeit (Sek)", (Number) times));
		conf.addSeries(new ListSeries("Schüsse", (Number) shots));
		conf.addSeries(new ListSeries("Getroffene Schüsse", (Number) hits));
	}

	private void updateChart() {
		Configuration conf = chart.getConfiguration();
		conf.setSeries(
				new ListSeries("Zeit (Sek)", (Number) times),
				new ListSeries("Schüsse", (Number) shots),
				new ListSeries("Getroffene Schüsse", (Number) hits)
		);
		chart.setConfiguration(conf);
	}
}
