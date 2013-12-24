import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class Trial extends VBox{

	private Node content;
	private final String name;
	private final String stamp;
	private ProgressBar progress;

	private final File[] files;

	private ArrayList<MATBEvent> events = new ArrayList<MATBEvent>();

	/**
	 * Defines the default header for this scope of printing.
	 * 
	 * This will be appended to event data.
	 */
	public static final String header = "\"TimeStamp\",\"Trial Name\"";

	/**
	 * Contains all visual and numerical data of a trial set.
	 * 
	 * @param name - Requires a trial name for display and printing.
	 * @param stamp - String version of the associated timestamp.
	 * @param files - A list of associated files to parse.
	 * @param rootWindow - Access to the root window for opening dialogs.
	 */
	public Trial(String name, String stamp, File[] files){
		super();

		this.name = name;
		this.stamp = stamp;
		this.files = files;

		Text title = new Text(name);

		progress = new ProgressBar();

		getChildren().addAll(title, progress);

	}

	public Task<String> setupTask(){

		Reader r = new Reader(files);
		r.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent t) {

				Console.log("[Parser] " + t.getSource().getValue());

			}

		});

		progress.progressProperty().bind(r.progressProperty());

		return r;

	}

	/**
	 * Prints out data that can identify everything about this trial in a csv format.
	 */
	public String toString(){
		String ret = header + ',' + MATBEvent.header;
		String prepend = "\"" + stamp + "\",\"" + name + "\",";
		for (MATBEvent e : events){
			ret += prepend + e.toString() + "\r\n";
		}
		return ret;
	}

	/**
	 * A safer and faster stream alternative to the default toString.
	 * 
	 * Immediately offloads saving to file by printing directly to it,
	 * instead of building a string and recursively returning it.
	 * 
	 * @param out - Required output for printing to.
	 * @return Returns the printstream that was passed in.
	 * @throws IOException 
	 */
	public BufferedWriter toString(BufferedWriter out) throws IOException{

		return toString(out, false);

	}

	/**
	 * Same as other to string of same type. This one just allows you to
	 * control the header printing.
	 * 
	 * @param out - Where to print to.
	 * @param useHeader - Include header?
	 * @return out
	 * @throws IOException 
	 */
	public BufferedWriter toString(BufferedWriter out, boolean useHeader) throws IOException{
		
		if (useHeader){
			out.append(header + ',' + MATBEvent.header + "\r\n");
		}
		
		String prepend = "\"" + stamp + "\",\"" + name + "\",";

		for (MATBEvent e : events){
			out.append(prepend + e.toString() + "\r\n");
		}

		return out;
	}

	/**
	 * Concurrent method of reading files.
	 * 
	 * Returns status message when finished.
	 * @author Tyler
	 *
	 */
	private final class Reader extends Task<String>{

		private final File[] files;

		public Reader(File[] in){
			super();
			files = in;
		}

		private void readMATB(File f) throws IOException{
			BufferedReader in = null;

			updateMessage("Reading file");

			try{

				in = new BufferedReader( new FileReader(f));

				String line;
				while ((line = in.readLine()) != null){

					try{

						MATBEvent event = new MATBEvent(line);
						events.add(event);

					} catch (ParseException e) {
						continue; //We can handle files that are poorly parsed by skipping lines.
					}
				}

			} finally{

				in.close();

			}
		}

		@Override
		protected String call() throws Exception {

			updateMessage("Ready to read");
			updateProgress(10, 100);

			for (File f : files){

				if (f.getName().startsWith("MATB")){
					readMATB(f);
				}
				//Currently don't directly support the other files.

			}

			updateMessage("Processed");
			updateProgress(100, 100);

			return "Successfully read in " + name;
		}

	}

}
