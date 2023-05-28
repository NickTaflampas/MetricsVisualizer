package controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;

import Utility.Metric;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;


public abstract class PlotController {

	Connection connection;
	ArrayList<Metric> metrics = new ArrayList<Metric>();
	
	@FXML
	ChoiceBox<String> choiceBoxY;
	@FXML
	ChoiceBox<String> choiceBoxX;
	
	double prevMouseX = Double.NaN;
	double prevMouseY = Double.NaN;
	
	void initDragEvent(XYChart chart)
	{
		chart.addEventHandler(MouseEvent.MOUSE_DRAGGED, event ->
		{
			if (Double.isNaN(prevMouseX))
			{
				prevMouseX = event.getX();
				prevMouseY = event.getY();
			}
			else
			{
				double xVector = event.getX() - prevMouseX;
				double yVector = event.getY() - prevMouseY;
				
				setAutoRanging(false, chart);
				
				NumberAxis t = (NumberAxis) chart.getXAxis();
				double dragSpeed = xVector * (t.getUpperBound()-t.getLowerBound())/120;
				
				t.setLowerBound(t.getLowerBound() - dragSpeed);
				t.setUpperBound(t.getUpperBound() - dragSpeed);
				
				t = (NumberAxis) chart.getYAxis();
				dragSpeed = yVector * (t.getUpperBound()-t.getLowerBound())/120;
				t.setLowerBound(t.getLowerBound() + dragSpeed);
				t.setUpperBound(t.getUpperBound() + dragSpeed);
				
				prevMouseX = event.getX();
				prevMouseY = event.getY();
			}
		});
		
		chart.addEventHandler(MouseEvent.MOUSE_RELEASED, event ->
		{
			prevMouseX = Double.NaN;
			prevMouseY = Double.NaN;
		});
	}
	
	
	
	void initZoomEvent(XYChart chart)
	{
		chart.addEventHandler(ScrollEvent.SCROLL, event ->
		{
			double delta = event.getDeltaY();

			NumberAxis t = (NumberAxis) chart.getXAxis();
			t.setAutoRanging(false);
			double zoomSpeed = (delta/32)* (t.getUpperBound()-t.getLowerBound())/20;
			t.setLowerBound(Math.max(-8500, t.getLowerBound() + zoomSpeed));
			
			
			t = (NumberAxis) chart.getYAxis();
			t.setAutoRanging(false);
			zoomSpeed = (delta/32)* (t.getUpperBound()-t.getLowerBound())/20;
			t.setLowerBound(Math.max(-8500, t.getLowerBound() + zoomSpeed));
			
			
		});
	}
	
	
	Connection setConnection() throws Exception
	{
		String url = "jdbc:mysql://localhost:3306/countrydatabase";
		String name = "root";
		String password = "password";
		
		Class.forName("com.mysql.cj.jdbc.Driver");
		Connection con = DriverManager.getConnection(url, name, password);
		return con;
	}
	
	public Metric getMetricFromDisplayName(String name)
	{
		for (Metric i : metrics)
		{
			if (i.getDisplayName().equals(name))
			{
				return i;
			}
		}
		return null;
	}
	
	public void setAutoRanging(boolean value, XYChart chart)
	{	
		Axis t = chart.getXAxis();
		t.setAutoRanging(value);
		t = chart.getYAxis();
		t.setAutoRanging(value);
	}
	
	public void clearPlot(XYChart chart)
	{
		chart.getData().clear();
		setAutoRanging(true, chart);
	}
	
	
	public void changeSceneToHistogram(ActionEvent event) throws Exception
	{
		changeScene("../barPage.fxml", event);
	}
	
	public void changeSceneToLineplot(ActionEvent event) throws Exception
	{
		changeScene("../plotPage.fxml", event);
	}
	
	public void changeSceneToScatter(ActionEvent event) throws Exception
	{
		changeScene("../scatterPage.fxml", event);
	}
	
	void changeScene(String path, ActionEvent event) throws Exception
	{
		Parent root = FXMLLoader.load(getClass().getResource(path));
		Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	
	
}
