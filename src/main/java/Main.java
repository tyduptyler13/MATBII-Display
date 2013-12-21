import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;


public class Main extends Application {

	private Stage stage;
	private ScrollPane list;
	private ScrollPane content;

	public static void main(String[] args) {

		Console.setup();
		Console.log("Preping all interfaces.");

		Console.log("Launching GUI");

		launch(Main.class, args);

	}

	public void start(Stage stage){
		
		openConsole();

		this.stage = stage;

		BorderPane border = new BorderPane();

		border.setTop(createMenu());

		final SplitPane sp = new SplitPane();
		final StackPane sp1 = new StackPane();
		sp1.getChildren().add(createList());
		final StackPane sp2 = new StackPane();
		sp2.getChildren().add(createDisplayArea());
		sp.getItems().addAll(sp1, sp2);
		sp.setDividerPositions(.3);
		border.setCenter(sp);

		Scene scene = new Scene(border);
		stage.setScene(scene);
		stage.setTitle("MATBII-Display");
		stage.show();

	}

	private Pane createMenu(){

		final HBox menu = new HBox();
		menu.setPadding(new Insets(15, 12, 15, 12));
		menu.setSpacing(10);
		menu.setStyle("-fx-background-color: #336699");

		final Button open = new Button("Open");
		open.setPrefSize(100, 20);
		open.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				onOpenDirectory();
			}
		});

		Tooltip tt = new Tooltip("Select the top level of your trials. This program will search for all trials within this directory.");
		Tooltip.install(open, tt);

		//New menu buttons go here.

		menu.getChildren().addAll(open);

		return menu;

	}

	private Node createList(){

		BorderPane bp = new BorderPane();

		list = new ScrollPane();
		Text title = new Text("File Tree:");

		bp.setTop(title);
		bp.setCenter(list);

		return bp;

	}

	private Node createDisplayArea(){
		content = new ScrollPane();
		
		//TODO Finish trial views.
		Text t = new Text("This view is not yet available. Statistics will be available here soon.");
		
		content.setContent(t);

		content.setPrefSize(500, 500);

		return content;
	}

	public void setContent(Node n){
		content.setContent(n);
	}

	public void setList(Node n){
		list.setContent(n);
	}

	protected void onOpenDirectory(){

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose the top directory");
		File file = chooser.showDialog(stage);

		if (file == null) return;

		FileReader fr = new FileReader(file, this){

			@Override
			public void display(Node n) {
				setContent(n);
			}

		};

		setList(fr);

	}

	/**
	 * Allows you to retrieve a file from a GUI.
	 * @param title - Title of window
	 * @param open - Open existing file (If false, opens save dialog)
	 * @return Chosen File
	 */
	public File getFile(String title, FileChooser.ExtensionFilter[] ef , boolean open){
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().addAll(ef);
		fc.setTitle(title);
		if (open){
			return fc.showOpenDialog(stage);
		} else {
			return fc.showSaveDialog(stage);
		}
	}

	private static void openConsole(){


		final Stage console = new Stage();
		//console.initStyle(StageStyle.UTILITY);

		final TextArea ta = new TextArea();
		ta.setPrefHeight(300);
		ta.setPrefWidth(500);
		ta.setEditable(false);
		
		Console.addOutput(new PrintInterface(){

			@Override
			public void print(String s) {
				ta.appendText(s + "\r\n");
			}
			
		});



		final Scene s = new Scene(ta);
		console.setScene(s);

		console.setTitle("MATBII-Console");
		
		console.show();
		
		Console.log("Loaded ConsoleGUI");

	}

}
