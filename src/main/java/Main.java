import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Main extends Application {

	public static void main(String[] args) {

		Console.setup();
		Console.log("Preping all interfaces.");

		Console.log("Launching GUI");

		launch(Main.class, args);

	}

	public void start(Stage stage){

		BorderPane border = new BorderPane();

		border.setTop(createMenu());
		
		Scene scene = new Scene(border);
		stage.setScene(scene);
		stage.setTitle("MATBII-Display");
		stage.show();
		
	}

	private Pane createMenu(){

		HBox menu = new HBox();
		menu.setPadding(new Insets(15, 12, 15, 12));
		menu.setSpacing(10);
		menu.setStyle("-fx-background-color: #336699");

		Button open = new Button("Open");
		open.setPrefSize(100, 20);

		//New menu buttons go here.

		menu.getChildren().addAll(open);

		return menu;

	}

}
