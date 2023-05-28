import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ApplicationStart extends Application {

	
	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		Parent root = FXMLLoader.load(getClass().getResource("plotPage.fxml"));
		
		
		Scene scene = new Scene(root);
		
		
		Image icon = new Image("yes.png");
		primaryStage.getIcons().add(icon);
		primaryStage.setTitle("CountryMetrics Application");
		
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
