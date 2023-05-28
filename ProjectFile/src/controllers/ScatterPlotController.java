package controllers;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import Utility.Metric;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

public class ScatterPlotController extends PlotController {
	@FXML
	private ScatterChart<Number, Number> scatterPlot;
	
	@FXML
	ChoiceBox<String> choiceBoxYear;

	HashMap<String, String> countriesToIso = new HashMap<String, String>();
	
	
	
	@FXML
	public void initialize()
	{
		initZoomEvent(scatterPlot);
		initDragEvent(scatterPlot);
		
		try
		{
			connection = setConnection();
			choiceBoxY.setOnAction(this::changeValues);
			
			
			Scanner scanner = new Scanner(new File("values_map.txt"));
			while (scanner.hasNextLine())
			{
				String[] splitLine = scanner.nextLine().split(":");
				metrics.add(new Metric(splitLine));
				choiceBoxY.getItems().add(splitLine[0]);
				choiceBoxX.getItems().add(splitLine[0]);
			}
			scanner.close();
			
			
			String query = "SELECT iso_code, display_name FROM countries;";
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(query);
			while (rs.next())
			{
				countriesToIso.put(rs.getString(1), rs.getString(2));
			}
			
			
		}
		catch(Exception e)
		{
			System.out.println("Database Connection Error:" + e);
		}
		
		
	}
	
	public void addMetric()
	{
		try
		{
			
			if (choiceBoxY.getSelectionModel().getSelectedItem() == null ||
				choiceBoxYear.getSelectionModel().getSelectedItem() == null ||
				choiceBoxX.getSelectionModel().getSelectedItem() == null) { return; }
			
			Statement statement = connection.createStatement();
			Metric valueMetric = getMetricFromDisplayName(choiceBoxY.getSelectionModel().getSelectedItem());
			Metric coordinateMetric = getMetricFromDisplayName(choiceBoxX.getSelectionModel().getSelectedItem());
			String year = choiceBoxYear.getSelectionModel().getSelectedItem();
			

			String query = "SELECT a." + valueMetric.getDatabaseName() + ", b." + coordinateMetric.getDatabaseName()
			+ ", a.iso_code"
			+ " FROM " + valueMetric.getDatabaseTable() + " a "
			+ "JOIN " + coordinateMetric.getDatabaseTable() + " b ON a.metric_year = b.metric_year AND a.metric_year = " + year
			+ " AND a.iso_code = b.iso_code  AND a." + valueMetric.getDatabaseName()
			+ " IS NOT NULL AND b." + coordinateMetric.getDatabaseName() + " IS NOT NULL";

		
			
			
			ResultSet rs = statement.executeQuery(query);
			XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
			series.setName(valueMetric.getDisplayName() + " over " + coordinateMetric.getDisplayName() + " in " + year);

			ArrayList<String> labelCountries = new ArrayList<String>();
			while (rs.next())
			{
				double x = Double.parseDouble(rs.getString(2));
				double y = Double.parseDouble(rs.getString(1));
				series.getData().add(new XYChart.Data<Number, Number>(x, y));
				labelCountries.add(countriesToIso.get(rs.getString(3)));	
			}

			if (series.getData().size() == 0) { return; }
			scatterPlot.getData().add(series);

			
			
			//Labels
			int i = 0;
			for (final XYChart.Data<Number, Number> data : series.getData())
			{
				String countryName = labelCountries.get(i);
				
				data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, event -> 
				{
					Tooltip.install(data.getNode(), new Tooltip(countryName + " " 
																+ data.getXValue() + "  " + data.getYValue()));
				});
				
				i++;
			}
			
		}
		catch (Exception e) {System.out.println(e);}
		
		
	}
	
	
	void changeValues(ActionEvent event)
	{
		Metric m = getMetricFromDisplayName(choiceBoxY.getSelectionModel().getSelectedItem());
		setAutoRanging(true, scatterPlot);
		try
		{
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT DISTINCT metric_year FROM " + m.getDatabaseTable() + 
												  " ORDER BY metric_year");
			
			choiceBoxYear.setValue(null);
			choiceBoxYear.getItems().clear();
			while (rs.next())
			{
				choiceBoxYear.getItems().add(rs.getString(1));
			}
		}
		catch(Exception e) { System.out.println(e); }

	}
	
	public void clearPlot() { clearPlot(scatterPlot); }
	

}
