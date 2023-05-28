package controllers;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import Utility.Metric;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

public class LinePlotController extends PlotController {

	@FXML
	private LineChart<Number, Number> linePlot;
	double prevMouseX = Double.NaN;
	double prevMouseY = Double.NaN;
	@FXML
	ChoiceBox<String> choiceBoxCountry;

	ArrayList<Integer> countryISOCodes = new ArrayList<Integer>();
	int selectedCountryISOCode;

	@FXML
	public void initialize()
	{

		initZoomEvent(linePlot);
		initDragEvent(linePlot);
		
		try
		{
			connection = setConnection();
			choiceBoxY.setOnAction(this::changeValues);
			choiceBoxCountry.setOnAction(this::changeCountry);
			
			
			Scanner scanner = new Scanner(new File("values_map.txt"));
			while (scanner.hasNextLine())
			{
				String[] splitLine = scanner.nextLine().split(":");
				metrics.add(new Metric(splitLine));
				choiceBoxY.getItems().add(splitLine[0]);
				choiceBoxX.getItems().add(splitLine[0]);
			}
			choiceBoxX.getItems().add("Year");
			scanner.close();
			
		}
		catch(Exception e)
		{
			System.out.println("Database Connection Error:" + e);
		}
		
		
	}
	
	public void addCountryMetric()
	{
		try
		{
			
			if (choiceBoxY.getSelectionModel().getSelectedItem() == null ||
				choiceBoxCountry.getSelectionModel().getSelectedItem() == null ||
				choiceBoxX.getSelectionModel().getSelectedItem() == null) { return; }
			
			Statement statement = connection.createStatement();
			Metric valueMetric = getMetricFromDisplayName(choiceBoxY.getSelectionModel().getSelectedItem());
			Metric coordinateMetric = getMetricFromDisplayName(choiceBoxX.getSelectionModel().getSelectedItem());
			
			
			String query = "";
			
			if (choiceBoxX.getSelectionModel().getSelectedItem().equals("Year"))
			{
				coordinateMetric = new Metric("Years", "","");
				query = "SELECT " + valueMetric.getDatabaseName() + ", metric_year "
						+ "FROM " + valueMetric.getDatabaseTable() + " WHERE iso_code = " + selectedCountryISOCode
						+ " AND metric_year IS NOT NULL AND " + valueMetric.getDatabaseName() + " IS NOT NULL "
						+ " order by metric_year;";
			}
			else
			{
				query = "SELECT a." + valueMetric.getDatabaseName() + ", b." + coordinateMetric.getDatabaseName()
				+ " FROM " + valueMetric.getDatabaseTable() + " a "
				+ "JOIN " + coordinateMetric.getDatabaseTable() + " b ON a.metric_year = b.metric_year AND"
				+ " a.iso_code = " + selectedCountryISOCode + " AND a.iso_code = b.iso_code  AND a." + valueMetric.getDatabaseName()
				+ " IS NOT NULL AND b." + coordinateMetric.getDatabaseName() + " IS NOT NULL ORDER BY b." + coordinateMetric.getDatabaseName();

			}
			
			
			ResultSet rs = statement.executeQuery(query);
			XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
			series.setName(choiceBoxCountry.getSelectionModel().getSelectedItem() +
					" " + choiceBoxY.getSelectionModel().getSelectedItem() + " over " + choiceBoxX.getSelectionModel().getSelectedItem());

			while (rs.next())
			{
				double x = Double.parseDouble(rs.getString(2));
				double y = Double.parseDouble(rs.getString(1));
				series.getData().add(new XYChart.Data<Number, Number>(x, y));
				
			}

			if (series.getData().size() == 0) { return; }
			linePlot.getData().add(series);

			//Labels
			for (final XYChart.Data<Number, Number> data : series.getData())
			{
				data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
					Tooltip.install(data.getNode(), new Tooltip(data.getXValue() + "  " + data.getYValue()));
				});
			}
			
		}
		catch (Exception e) {System.out.println(e);}
		
		
	}
	
	
	void changeCountry(ActionEvent event)
	{
		selectedCountryISOCode = countryISOCodes.get(choiceBoxCountry.getSelectionModel().getSelectedIndex());
	}
	
	public void clearPlot() { clearPlot(linePlot); }
	
	void changeValues(ActionEvent event)
	{
		Metric m = getMetricFromDisplayName(choiceBoxY.getSelectionModel().getSelectedItem());
		setAutoRanging(true, linePlot);
		try
		{
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT distinct c.display_name, c.iso_code FROM countries c "
							+ "JOIN " + m.getDatabaseTable() + " b ON c.iso_code = b.iso_code ORDER BY c.display_name;");
			
			choiceBoxCountry.setValue(null);
			choiceBoxCountry.getItems().clear();
			countryISOCodes.clear();
			while (rs.next())
			{
				choiceBoxCountry.getItems().add(rs.getString(1));
				countryISOCodes.add(Integer.parseInt(rs.getString(2)));
			}
		}
		catch(Exception e) { System.out.println(e); }

	}
	
	
	
}
