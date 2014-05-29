package com.myuplay.matb;

import java.io.File;
import java.io.FileNotFoundException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	private static Stage window;
	private static Stage consoleWindow;
	private static final TextArea console = new TextArea();
	private static BorderPane list;

	public static void main(String[] args) {

		Console.setup();
		Console.log("Preping all interfaces.");

		Console.log("Launching GUI");

		launch(Main.class, args);

	}

	@Override
	public void start(Stage stage) {

		window = stage;

		BorderPane border = new BorderPane();

		border.setTop(createMenu());

		border.setCenter(createDisplay());

		Scene scene = new Scene(border);
		stage.setScene(scene);
		stage.setTitle("MATBII-Display");
		stage.show();

		consoleWindow = new Stage();

		openConsole();

		consoleWindow.show();

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent e) {
				consoleWindow.close();
			}
		});

		stage.toFront();

	}

	private Pane createMenu() {

		final HBox menu = new HBox();
		menu.setPadding(new Insets(15, 12, 15, 12));
		menu.setSpacing(10);
		menu.setStyle("-fx-background-color: #336699");

		final Button open = new Button("Open");
		open.setPrefSize(100, 20);
		open.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				onOpenDirectory();
			}
		});

		Tooltip tt = new Tooltip(
				"Select the top level of your trials. This program will search for all trials within this directory.");
		Tooltip.install(open, tt);

		// New menu buttons go here.

		menu.getChildren().addAll(open);

		return menu;

	}

	private Node createDisplay() {

		list = new BorderPane();
		Text title = new Text("File Tree:");

		list.setTop(title);

		list.setPrefHeight(300);
		list.setPrefWidth(500);

		return list;

	}

	private static void setList(Node n) {
		list.setCenter(n);
	}

	private static void onOpenDirectory() {

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Choose the top directory");
		File file = chooser.showDialog(window);

		if (file == null)
			return;

		FileReader fr = new FileReader(file);

		setList(fr);

	}

	/**
	 * Allows you to retrieve a file from a GUI.
	 * 
	 * @param title
	 *            - Title of window
	 * @param open
	 *            - Open existing file (If false, opens save dialog)
	 * @return Chosen File
	 * @throws FileNotFoundException
	 */
	public static File getFile(String title, FileChooser.ExtensionFilter[] ef,
			boolean open) throws FileNotFoundException {
		FileChooser fc = new FileChooser();
		fc.getExtensionFilters().addAll(ef);
		fc.setTitle(title);

		File f;

		if (open) {
			f = fc.showOpenDialog(window);
		} else {
			f = fc.showSaveDialog(window);
		}

		if (f == null)
			throw new FileNotFoundException();

		if (!f.getName().endsWith(".csv")) {
			f = new File(f.getAbsolutePath() + ".csv");
		}

		return f;

	}

	/**
	 * Thread safe implementation of the println for its console.
	 */
	private static void println(final String s) {

		// If already on the application thread then print immediately.
		if (Platform.isFxApplicationThread()) {
			console.appendText(s + "\r\n");
		} else {

			// Otherwise we need to queue the task to run on the application
			// thread.
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					println(s);
				}

			});

		}

	}

	private static void openConsole() {

		console.setPrefHeight(300);
		console.setPrefWidth(500);
		console.setEditable(false);

		Console.addOutput(new PrintInterface() {

			@Override
			public void print(String s) {
				println(s);
			}

		});

		final Scene s = new Scene(console);
		consoleWindow.setScene(s);

		consoleWindow.setTitle("MATBII-Console");

		Console.log("Loaded ConsoleGUI");

	}

}
