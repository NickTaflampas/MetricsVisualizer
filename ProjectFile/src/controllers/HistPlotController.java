package controllers;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import Utility.Metric;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;

public class HistPlotController extends PlotController{
	
	@FXML
	BarChart<String, Number> histogram;
	@FXML
	ChoiceBox<String> choiceBoxSelection;
	@FXML
	ChoiceBox<String> genderBox;
	@FXML
	ChoiceBox<String> yearBox;
	@FXML
	CheckBox fiveYearSelector;
	
	ArrayList<Integer> countryISOCodes = new ArrayList<Integer>();
	int selectedCountryISOCode;
	
	@FXML
	public void initialize()
	{
		try
		{
			connection = setConnection();
			choiceBoxX.setOnAction(this::changeValues);
			choiceBoxY.setOnAction(this::changeCategories);
			fiveYearSelector.setOnAction(this::changeValues);
			
			Scanner scanner = new Scanner(new File("values_map.txt"));
			while (scanner.hasNextLine())
			{
				String[] splitLine = scanner.nextLine().split(":");
				metrics.add(new Metric(splitLine));
				choiceBoxY.getItems().add(splitLine[0]);
			}
			scanner.close();
			genderBox.getItems().addAll("All","Male","Female");
			genderBox.setVisible(false);
			yearBox.setVisible(false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		
	}
	
	
	public void addCountryMetric()
	{
		try
		{
			
			if (choiceBoxY.getSelectionModel().getSelectedItem() == null ||
				choiceBoxSelection.getSelectionModel().getSelectedItem() == null ||
				choiceBoxX.getSelectionModel().getSelectedItem() == null) { return; }
			
			Statement statement = connection.createStatement();
			Metric valueMetric = getMetricFromDisplayName(choiceBoxY.getSelectionModel().getSelectedItem());
			
			
			String query = "";
			
			if (choiceBoxX.getSelectionModel().getSelectedItem().equals("By Year"))
			{
				selectedCountryISOCode = countryISOCodes.get(choiceBoxSelection.getSelectionModel().getSelectedIndex());
				if (fiveYearSelector.isSelected())
				{
					query = "SELECT country_name, min(metric_year), max(metric_year), sum(" + valueMetric.getDatabaseName() 
							+ ") FROM " + valueMetric.getDatabaseTable() 
							+ " where iso_code = " + selectedCountryISOCode + " and " + valueMetric.getDatabaseName() 
							+ " IS NOT NULL group by iso_code, floor( metric_year / 5 ) "
							+ "order by metric_year;";
				}
				else
				{
					query = "SELECT metric_year, " + valueMetric.getDatabaseName() + " FROM " + valueMetric.getDatabaseTable()
					+ " WHERE iso_code = " + selectedCountryISOCode + " and " + valueMetric.getDatabaseName() 
					+ " IS NOT NULL ORDER BY metric_year;";
				}
				

			}
			else if (choiceBoxX.getSelectionModel().getSelectedItem().equals("By Country"))
			{
				query = "SELECT country_name, " + valueMetric.getDatabaseName() +
						" FROM " + valueMetric.getDatabaseTable() + 
						" WHERE metric_year=" + choiceBoxSelection.getSelectionModel().getSelectedItem() 
						+" AND " + valueMetric.getDatabaseName() + " IS NOT NULL;";
			}
			else
			{

				selectedCountryISOCode = countryISOCodes.get(choiceBoxSelection.getSelectionModel().getSelectedIndex());

				String gender = genderBox.getSelectionModel().getSelectedItem();

				if (fiveYearSelector.isSelected())
				{
					String targetColumn = "population";
					if (gender.equals("Male")) { targetColumn = "male_population"; }
					else if (gender.equals("Female")) { targetColumn = "female_population"; }
					
					query = "SELECT starting_age, ending_age, " + targetColumn + " FROM midyear_populations_by_range" 
							+ " WHERE iso_code = " + selectedCountryISOCode +" and metric_year = " 
							+ yearBox.getSelectionModel().getSelectedItem() + " order by ending_age;";
				}
				else
				{
					query = "SELECT * FROM midyear_populations_by_age" 
							+ " WHERE iso_code = " + selectedCountryISOCode 
							+ " and metric_year = " + yearBox.getSelectionModel().getSelectedItem();
					if (!gender.equals("All"))
					{
						query += " and sex = \""+ gender + "\"";
					}
					query += ";";
				}
			}
			ResultSet rs = statement.executeQuery(query);
			if (choiceBoxX.getSelectionModel().getSelectedItem().equals("By Age"))
			{
				addAgeMetric(rs);
			}
			else
			{
				addNormalMetric(rs);
			}
			
			
		}
		catch (Exception e) {System.out.println(e);}
		
		
	}
	
	void addAgeMetric(ResultSet rs) throws Exception
	{
		XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
		

		String gender = genderBox.getSelectionModel().getSelectedItem();
		series.setName(choiceBoxSelection.getSelectionModel().getSelectedItem() + " " + gender 
						+ " Population by Age in " + yearBox.getSelectionModel().getSelectedItem());
		if (fiveYearSelector.isSelected())
		{
			while (rs.next())
			{
				String startingAge = rs.getString(1);
				String endingAge = rs.getString(2);
				String category = "";
				if (startingAge.equals("0") && endingAge.equals("999")) { continue; }
				
				if (endingAge.equals("999"))
				{
					category = startingAge + "<";
				} else { category = startingAge + "-" + endingAge; }
				
				int y = Integer.parseInt(rs.getString(3));
				series.getData().add(new XYChart.Data<String, Number>(category, y));
			}
		}	
		else
		{
			if (gender.equals("All"))
			{
	
				int[] populations = new int[101];
				for (int iter = 0; iter < 2; iter++)
				{
					if (!rs.next()) { return; }
					for (int i = 0; i < 101; i++)
					{
						if (iter == 0)
						{
							populations[i] = Integer.parseInt(rs.getString(6+i));
						}
						else
						{
							populations[i] += Integer.parseInt(rs.getString(6+i));
							series.getData().add(new XYChart.Data<String, Number>("Age " + i, populations[i]));
						}
					}
				}
			}
			else
			{
				rs.next();
				for (int i = 0; i < 101; i++)
				{
					series.getData().add(new XYChart.Data<String, Number>("Age " + i, Integer.parseInt(rs.getString(6+i))));
				}
			}
		}
		
		
		if (series.getData().size() == 0) { return; }
		histogram.getData().add(series);
		for (final XYChart.Data<String, Number> data : series.getData())
		{
			data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
				Tooltip.install(data.getNode(), new Tooltip(data.getXValue() + "  " + data.getYValue()));
			});
		}
		
		
	}
	
	void addNormalMetric(ResultSet rs) throws Exception
	{
		XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
		series.setName(choiceBoxSelection.getSelectionModel().getSelectedItem() +
				" " + choiceBoxY.getSelectionModel().getSelectedItem() + " over " + choiceBoxX.getSelectionModel().getSelectedItem());

		boolean isFirstPick = true;
		while (rs.next())
		{
			String x = "X";
			double y = 0;
			if (fiveYearSelector.isSelected())
			{
				x = rs.getString(2) + "-" + rs.getString(3);
				y = Double.parseDouble(rs.getString(4));
				
				if (isFirstPick)
				{
					isFirstPick = false;
					int temp = Integer.parseInt(rs.getString(2));
					temp = temp-temp%10;
					x = temp + "-" + rs.getString(3);
				}
			}
			else
			{
				x = rs.getString(1);
				y = Double.parseDouble(rs.getString(2));
			}
		
		
			series.getData().add(new XYChart.Data<String, Number>(x, y));
			
		}

		if (series.getData().size() == 0) { return; }
		histogram.getData().add(series);

				
		//Labels
		for (final XYChart.Data<String, Number> data : series.getData())
		{
			data.getNode().addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
				Tooltip.install(data.getNode(), new Tooltip(data.getXValue() + "  " + data.getYValue()));
			});
		}
	}
	
	
	void changeCategories(ActionEvent event)
	{
		choiceBoxX.getItems().clear();
		choiceBoxX.getItems().addAll("By Year","By Country");
		genderBox.setVisible(false);
		yearBox.setVisible(false);
		
		if (choiceBoxY.getSelectionModel().getSelectedItem().equals("Population"))
		{
			choiceBoxX.getItems().add("By Age");
		}
	}
	
	void changeValues(ActionEvent event)
	{
		clearPlot();
		if (choiceBoxY.getSelectionModel().getSelectedItem() == null) { choiceBoxX.setValue(null); return; }
		
		String choice = choiceBoxX.getSelectionModel().getSelectedItem();
		if (choice == null) { return; }
		
		
		if (choice.equals("By Age")) { genderBox.setVisible(true); yearBox.setVisible(true); }
		else { genderBox.setVisible(false); yearBox.setVisible(false); }
		
		
		choiceBoxSelection.getItems().clear();
		choiceBoxSelection.setValue(null);
		
		Metric m = getMetricFromDisplayName(choiceBoxY.getSelectionModel().getSelectedItem());
		if (choice.equals("By Year"))
		{
			fiveYearSelector.setDisable(false);
			try
			{
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery("SELECT distinct c.display_name, c.iso_code FROM countries c "
						+ "JOIN " + m.getDatabaseTable() + " b ON c.iso_code = b.iso_code ORDER BY c.display_name;");

				countryISOCodes.clear();
				while (rs.next())
				{
					choiceBoxSelection.getItems().add(rs.getString(1));
					countryISOCodes.add(Integer.parseInt(rs.getString(2)));
				}
			}
			catch(Exception e) { System.out.println(e); }
		}
		else if (choice.equals("By Country"))
		{
			fiveYearSelector.setSelected(false);
			fiveYearSelector.setDisable(true);
			
			try
			{
				Statement statement = connection.createStatement();
				ResultSet rs = statement.executeQuery("SELECT distinct metric_year FROM " + m.getDatabaseTable()
				+ " WHERE " + m.getDatabaseName() + " IS NOT NULL;");
				
				while (rs.next())
				{
					choiceBoxSelection.getItems().add(rs.getString(1));
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	
		}
		else
		{
			try
			{
				String countryQuery = "";
				String yearQuery = "";
				if (fiveYearSelector.isSelected())
				{
					yearQuery = "SELECT distinct metric_year FROM midyear_populations_by_range order by metric_year;";
					countryQuery = "SELECT distinct country_name, iso_code FROM midyear_populations_by_range order by country_name;";
				}
				else
				{
					yearQuery = "SELECT distinct metric_year FROM midyear_populations_by_age order by metric_year;";
					countryQuery = "SELECT distinct country_name, iso_code FROM midyear_populations_by_age order by country_name;";
				}
				
				Statement statement = connection.createStatement();
				
				ResultSet rs = statement.executeQuery(countryQuery);
				while (rs.next()) 
				{ 
					choiceBoxSelection.getItems().add(rs.getString(1)); 
					countryISOCodes.add(Integer.parseInt(rs.getString(2)));
				}
				
				rs = statement.executeQuery(yearQuery);
				while (rs.next()) { yearBox.getItems().add(rs.getString(1)); }
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

	}
	

	
	
	public void clearPlot() { clearPlot(histogram); }
	public void clearPlot(ActionEvent event) { clearPlot(histogram); }
}
