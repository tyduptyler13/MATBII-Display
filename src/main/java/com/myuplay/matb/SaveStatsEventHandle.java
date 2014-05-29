package com.myuplay.matb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.stage.FileChooser;

public class SaveStatsEventHandle implements EventHandler<ActionEvent> {

	private final FileReader fileReader;

	public SaveStatsEventHandle(FileReader fr) {
		fileReader = fr;
	}

	@Override
	public void handle(ActionEvent event) {

		FileChooser.ExtensionFilter[] efs = { new FileChooser.ExtensionFilter(
				"CSV files", "*.csv") };

		File f;
		try {
			f = Main.getFile("Save to", efs, false);
		} catch (FileNotFoundException e1) {
			Console.log("Save aborted.");
			return;
		}

		List<Trial> trials = new ArrayList<Trial>();

		for (TreeItem<Node> t : fileReader.getSelectionModel()
				.getSelectedItems()) {

			trials.addAll(fileReader.getSelected(t));

		}

		StatsWriter w = new StatsWriter(f, trials);

		w.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent t) {
				Console.log("[IO Thread] " + t.getSource().getValue());
			}

		});

		w.setOnFailed(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent t) {
				Console.log("Failed to save the file!");
				Console.error(t.getSource().getMessage());
			}

		});

		try {

			new Thread(w).start();

		} catch (Exception e) {
			Console.error("Could not save to (" + f.getName() + "): "
					+ e.getMessage());
		}

	}

}
