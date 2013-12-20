import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.text.Text;


public abstract class FileReader extends TreeView<Node>{

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

	public FileReader(File directory){
		super();
		
		getFiles(directory);

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
				node.getChildren().add(getTrial(list, stamps.get(i), "Trial " + (i+1) + " ["+stamps.get(i)+"]"));
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

	private TreeItem<Node> getTrial(File[] files, String timeStamp, String id){
		
		ArrayList<File> valid = new ArrayList<File>(6);

		//Make a list of files that have the correct stamp.
		for (File f : files){
			if (f.getName().contains(timeStamp))
				valid.add(f);
		}

		File[] list = new File[valid.size()];
		valid.toArray(list);

		Trial t = new Trial(id, list);
		
		TreeItem<Node> ti = new TreeItem<Node>(t);
		
		return ti;

	}

	public abstract void display(Node n);

}
