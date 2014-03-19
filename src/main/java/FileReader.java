import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;


public abstract class FileReader extends TreeView<Node>{

	private final Main root;
	private ArrayList<Task<String>> tasks = new ArrayList<Task<String>>();

	/**
	 * Matches all files specific to MATBII
	 */
	private static final FileFilter ff = new FileFilter(){

		@Override
		public boolean accept(File f) {

			if (f.isFile()){

				if (f.getName().matches("(COMM|MATB|RMAN|SYSM|TRCK|WRS)_[0-9]{4}_[0-9]{8}\\.(txt|csv)"))
					return true;

			}

			return false;
		}

	};

	/**
	 * Scans directories for files and then reads in the data.
	 * 
	 * @param directory - Where to do the search.
	 * @param rootWindow - Needed for the trials and saving files.
	 */
	public FileReader(File directory, Main rootWindow){
		super();

		root = rootWindow;

		//Selection mode
		getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


		//Context Menu
		ContextMenu menu = new ContextMenu();
		MenuItem saveDataButton = new MenuItem("Save Data");
		saveDataButton.setOnAction(new SaveDataEventHandle());
		
		MenuItem saveStatsButton = new MenuItem("Save Stats");
		saveStatsButton.setOnAction(new SaveStatsEventHandle());
		
		menu.getItems().addAll(saveDataButton, saveStatsButton);

		setContextMenu(menu);

		//Take care of io.

		Console.log("Scanning for trials.");

		getFiles(directory);

		Console.log("Scan complete. Parsing files.");

		Thread executer = new Thread(new Runnable(){

			@Override
			public void run() {

				for (Task<String> t : tasks){

					t.run();

				}

				Console.log("Parsing complete. Ready.");

			}

		});

		Console.log("Parsing in separate thread. App thread ready.");

		executer.start();

	}

	public void getFiles(File folder){

		Text t = new Text(folder.getName());
		TreeItem<Node> root = new TreeItem<Node>(t);
		setRoot(root);
		getFiles(folder, root);

	}

	private boolean getFiles(File folder, TreeItem<Node> node){

		File[] list = folder.listFiles(ff);

		if (list.length > 0){

			Arrays.sort(list);

			//This is a trial directory.
			ArrayList<String> stamps = new ArrayList<String>();

			//Collect individual stamps.
			for (File f : list){
				String stamp = f.getName();
				stamp = stamp.substring(stamp.indexOf('_') + 1, stamp.indexOf('.'));
				if (!stamps.contains(stamp)){
					stamps.add(stamp);
				}
			}


			for (int i = 0; i < stamps.size(); ++i){
				try {
					node.getChildren().add(getTrial(list, stamps.get(i), i+1));
				} catch (ParseException e) {
					Console.error("Could not read in trial with timestamp " + stamps.get(i));
				}
			}

			return true;
		} else {
			//Keep looking

			for (File f : folder.listFiles()){

				if (f.isDirectory()){
					Text t = new Text(f.getName());
					TreeItem<Node> n = new TreeItem<Node>(t);
					if (getFiles(f, n)){
						node.getChildren().add(n);
					}
				}	

			}

			if (node.getChildren().size() > 0)
				return true;

		}

		return false;

	}

	private TreeItem<Node> getTrial(File[] files, String timeStamp, int id) throws ParseException{

		ArrayList<File> valid = new ArrayList<File>(6);

		//Make a list of files that have the correct stamp.
		for (File f : files){
			if (f.getName().contains(timeStamp))
				valid.add(f);
		}

		File[] list = new File[valid.size()];
		valid.toArray(list);

		Trial t = new Trial(id, timeStamp, list);

		tasks.add(t.setupTask());

		TreeItem<Node> ti = new TreeItem<Node>(t);

		return ti;

	}

	public abstract void display(Node n);

	/**
	 * Concurrent method for writing files.
	 * 
	 * Returns status message when finished.
	 * @author Tyler
	 *
	 */
	private final class DataWriter extends Task<String>{

		private final File file;
		private final List<Trial> trials;

		public DataWriter(File out, List<Trial> trials){
			super();
			file = out;
			this.trials = trials;
		}

		@Override
		protected String call() throws Exception {

			BufferedWriter out = null;

			try{

				out = new BufferedWriter( new FileWriter(file));

				out.append(Trial.getHeader() + "\r\n"); //DOS formated.

				for (Trial t : trials){
					t.toString(out);
				}

			} catch (Exception e){
				
				super.failed();
				updateMessage("Failed");
				Console.error("An error occured saving the file. Printed exception to System.err");
				e.printStackTrace(System.err);
				return "Failed to save file! (" + file.getName() +")";
				
			} finally {
				out.flush();
				out.close();
			}

			return "Printed data to " + file.getName();
		}

	}

	private class SaveDataEventHandle implements EventHandler<ActionEvent>{

		@Override
		public void handle(ActionEvent evt) {

			FileChooser.ExtensionFilter[] efs = {new FileChooser.ExtensionFilter("CSV files", "*.csv")};

			File f;
			try {
				f = root.getFile("Save to", efs, false);
			} catch (FileNotFoundException e1) {
				Console.log("Save aborted.");
				return;
			}

			List<Trial> trials = new ArrayList<Trial>();

			for (TreeItem<Node> t : getSelectionModel().getSelectedItems()){

				trials.addAll(getSelected(t));

			}

			DataWriter w = new DataWriter(f, trials);

			w.setOnSucceeded(new EventHandler<WorkerStateEvent>(){

				@Override
				public void handle(WorkerStateEvent t) {
					Console.log("[IO Thread] " + t.getSource().getValue());
				}

			});

			w.setOnFailed(new EventHandler<WorkerStateEvent>(){

				@Override
				public void handle(WorkerStateEvent t) {
					Console.log("Failed to save the file!");
					Console.error(t.getSource().getException().getMessage());
				}

			});

			try {

				new Thread(w).start();

			} catch (Exception e) {
				Console.error("Could not save to (" + f.getName() + "): " + e.getMessage());
			}

		}

	}
	
	private class StatsWriter extends Task<String>{
		
		private final File file;
		private final List<Trial> trials;

		public StatsWriter(File out, List<Trial> trials){
			super();
			file = out;
			this.trials = trials;
		}

		@Override
		protected String call() throws Exception {

			BufferedWriter out = null;

			try{

				out = new BufferedWriter( new FileWriter(file));

				out.append(Trial.getStatsHeader() + "\r\n"); //DOS formated.

				for (Trial t : trials){
					t.getStats(out);
				}

			} catch (Exception e){
				
				super.failed();
				updateMessage("Failed");
				Console.error("An error occured saving the file. Printed exception to System.err");
				e.printStackTrace(System.err);
				return "Failed to save file! (" + file.getName() +")";
				
			} finally {
				out.flush();
				out.close();
			}

			return "Printed data to " + file.getName();
		}
		
	}
	
	private class SaveStatsEventHandle implements EventHandler<ActionEvent>{

		@Override
		public void handle(ActionEvent event) {
			
			FileChooser.ExtensionFilter[] efs = {new FileChooser.ExtensionFilter("CSV files", "*.csv")};

			File f;
			try {
				f = root.getFile("Save to", efs, false);
			} catch (FileNotFoundException e1) {
				Console.log("Save aborted.");
				return;
			}

			List<Trial> trials = new ArrayList<Trial>();

			for (TreeItem<Node> t : getSelectionModel().getSelectedItems()){

				trials.addAll(getSelected(t));

			}

			StatsWriter w = new StatsWriter(f, trials);

			w.setOnSucceeded(new EventHandler<WorkerStateEvent>(){

				@Override
				public void handle(WorkerStateEvent t) {
					Console.log("[IO Thread] " + t.getSource().getValue());
				}

			});

			w.setOnFailed(new EventHandler<WorkerStateEvent>(){

				@Override
				public void handle(WorkerStateEvent t) {
					Console.log("Failed to save the file!");
					Console.error(t.getSource().getException().getMessage());
				}

			});

			try {

				new Thread(w).start();

			} catch (Exception e) {
				Console.error("Could not save to (" + f.getName() + "): " + e.getMessage());
			}

			
		}
		
	}

	private List<Trial> getSelected(TreeItem<Node> node){

		ArrayList<Trial> list = new ArrayList<Trial>();

		return getSelected(node, list);

	}

	private List<Trial> getSelected(TreeItem<Node> node, List<Trial> list){

		if (node.isLeaf() && node.getValue() instanceof Trial){

			list.add((Trial) node.getValue());

		} else {

			for (TreeItem<Node> n : node.getChildren()){

				getSelected(n, list);

			}

		}

		return list;

	}

}
