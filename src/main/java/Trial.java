import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class Trial extends VBox{

	private Node content;
	private final int id;
	private final Date timestamp;
	private ProgressBar progress;

	private final File[] files;

	private static final SimpleDateFormat tdfin = new SimpleDateFormat("yyyy_MMddHHmm");
	private static final SimpleDateFormat tdfout = new SimpleDateFormat("yyyy/MM/dd hh:mma");
	
	private TreeMap<Date, EventContainer> events = new TreeMap<Date, EventContainer>();

	/**
	 * Defines the default header for this scope of printing.
	 * 
	 * This will be appended to event data.
	 */
	public static final String header = "\"TimeStamp\",\"Trial Name\",\"Folder\"";

	/**
	 * Contains all visual and numerical data of a trial set.
	 * 
	 * @param name - Requires a trial name for display and printing.
	 * @param stamp - String version of the associated timestamp.
	 * @param files - A list of associated files to parse.
	 * @param rootWindow - Access to the root window for opening dialogs.
	 * @throws ParseException 
	 */
	public Trial(int id, String stamp, File[] files) throws ParseException{
		super();

		this.id = id;
		this.files = files;
		timestamp = tdfin.parse(stamp);

		Text title = new Text("Trial " + id + " " + tdfout.format(timestamp));

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
		String ret = Trial.getHeader() + "\r\n";

		String absolutePath = files[0].getAbsolutePath();
		String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
		
		String prepend = "\"" + tdfout.format(timestamp) + "\",\"" + id + "\",\"" + filePath + "\",";
		
		for (Map.Entry<Date, EventContainer> e : events.entrySet()){
			ret += prepend + e.toString() + "\r\n";
		}
		
		return ret;
	}
	
	/**
	 * This reduces the characters required in a csv line so that
	 * it doesn't take up as much space.
	 * @param in
	 * @return
	 */
	private String cleanCSV(String in){

		return in.replaceAll("\"\"", "");

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
			out.append(Trial.getHeader() + "\r\n");
		}
		
		String absolutePath = files[0].getAbsolutePath();
		String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));
		
		String prepend = "\"" + tdfout.format(timestamp) + "\",\"" + id + "\",\"" + filePath + "\",";

		for (Map.Entry<Date, EventContainer> e : events.entrySet()){
			out.append(cleanCSV(prepend + e.getValue().toString() + "\r\n"));
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
		
		private EventContainer getEvent(Date key){
			if (events.containsKey(key)){
				return events.get(key);
			} else {
				EventContainer e = new EventContainer();
				events.put(key, e);
				return e;
			}
		}

		private void readFile(File f) throws IOException{
			BufferedReader in = null;

			updateMessage("Reading file.");

			try{

				in = new BufferedReader( new FileReader(f));

				String line;
				while ((line = in.readLine()) != null){

					try{

						if (f.getName().startsWith("MATB")){
							MATBEvent event = new MATBEvent();
							Date time = event.parse(line);
							getEvent(time).matb = event;
						} else if (f.getName().startsWith("COMM")){
							COMMEvent event = new COMMEvent();
							Date time = event.parse(line);
							getEvent(time).comm = event;
						} else if (f.getName().startsWith("SYSM")){
							SYSMEvent event = new SYSMEvent();
							Date time = event.parse(line);
							getEvent(time).sysm = event;
						} else if (f.getName().startsWith("TRCK")){
							TRCKEvent event = new TRCKEvent();
							Date time = event.parse(line);
							getEvent(time).trck = event;
						} else if (f.getName().startsWith("RMAN")){
							RMANEvent event = new RMANEvent();
							Date time = event.parse(line);
							getEvent(time).rman = event;
						} else if (f.getName().startsWith("WRS")){
							WRSEvent event = new WRSEvent();
							Date time = event.parse(line);
							getEvent(time).wrs = event;
						}

					} catch (ParseException e) {
						continue; //We can handle files that are poorly parsed by skipping lines.
					} catch (Exception e){
						Console.error("An error occured in parsing! The results are likely unusable. Details printed to System.err.");
						e.printStackTrace(System.err);
					}
				}

			} finally{

				in.close();

			}
			
			updateMessage("Finished reading file.");
			
		}

		@Override
		protected String call() throws Exception {

			updateMessage("Ready to read");
			updateProgress(10, 100);

			for (File f : files){

				readFile(f);

			}

			updateMessage("Processed");
			updateProgress(100, 100);

			return "Successfully read in Trial " + id;
		}

	}
	
	public static String getHeader(){
		return Trial.header + ',' + EventContainer.getHeader();
	}

}
