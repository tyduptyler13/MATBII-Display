import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class Trial extends VBox{

	private Text title;
	
	private boolean hasGoodId = false;
	
	private Node content;
	private String id;
	private final DateTime timestamp;
	private ProgressBar progress;

	private final File[] files;

	private static final DateTimeFormatter tdfin = DateTimeFormat.forPattern("yyyy_MMddHHmm");
	private static final DateTimeFormatter tdfout = DateTimeFormat.forPattern("yyyy/MM/dd hh:mma");

	private ArrayList<EventContainer> events = new ArrayList<EventContainer>();

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

		this.id = Integer.toString(id); //TODO Temporary fix.
		this.files = files;
		timestamp = tdfin.parseDateTime(stamp);

		title = new Text("Trial " + id + " " + tdfout.print(timestamp));

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
	@Override
	public String toString(){
		String ret = Trial.getHeader() + "\r\n";

		String absolutePath = files[0].getAbsolutePath();
		String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));

		String prepend = "\"" + tdfout.print(timestamp) + "\",\"" + id + "\",\"" + filePath + "\",";

		for (EventContainer e : events){
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

		String prepend = "\"" + tdfout.print(timestamp) + "\",\"" + id + "\",\"" + filePath + "\",";

		for (EventContainer e : events){
			out.append(cleanCSV(prepend + e.toString() + "\r\n"));
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

		private EventContainer getEvent(DateTime time){

			for (EventContainer e : events) {
				if (e.equals(time)){
					return e;
				}
			}
			
			EventContainer ret = new EventContainer(time);
			events.add(ret);
			return ret;
			
		}

		private void readFile(File f) throws IOException{
			
			if (f.getName().endsWith("csv")) return; //Skip csv files. It breaks things.
			
			BufferedReader in = null;

			updateMessage("Reading file.");

			try{

				in = new BufferedReader( new FileReader(f));

				String line;
				while ((line = in.readLine()) != null){
					
					if (line.isEmpty() || line.charAt(0) == '#'){
						
						if (!hasGoodId && line.contains("Events Filename")){
							
							id = line.split(":\\s*")[1].trim();
							title.setText("Trial " + id + " " + tdfout.print(timestamp));
							hasGoodId = true; //Skip this change in the future. No need to waste time.
							
						}
						
						continue;
					}

					try{

						if (f.getName().startsWith("MATB")){
							MATBEvent event = new MATBEvent();
							DateTime time = event.parse(line);
							getEvent(time).matb = event;
						} else if (f.getName().startsWith("COMM")){
							COMMEvent event = new COMMEvent();
							DateTime time = event.parse(line);
							getEvent(time).comm = event;
						} else if (f.getName().startsWith("SYSM")){
							SYSMEvent event = new SYSMEvent();
							DateTime time = event.parse(line);
							getEvent(time).sysm = event;
						} else if (f.getName().startsWith("TRCK")){
							TRCKEvent event = new TRCKEvent();
							DateTime time = event.parse(line);
							getEvent(time).trck = event;
						} else if (f.getName().startsWith("RMAN")){
							RMANEvent event = new RMANEvent();
							DateTime time = event.parse(line);
							getEvent(time).rman = event;
						} else if (f.getName().startsWith("WRS")){
							WRSEvent event = new WRSEvent();
							DateTime time = event.parse(line);
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
			
			sortData();

			updateMessage("Processed");
			updateProgress(100, 100);

			return "Successfully read in Trial " + id;
		}

	}
	
	public void sortData(){
		Collections.sort(events);
	}

	public static String getHeader(){
		return Trial.header + ',' + EventContainer.getHeader();
	}

	//Begin stats functions

	public static String getStatsHeader(){
		return header + ",\"Event\",\"Reaction time (ms) (Time Since Event)\",\"Dead Time (ms) (Time Since last response)\"";
	}

	public BufferedWriter getStats(BufferedWriter out){

		String absolutePath = files[0].getAbsolutePath();
		String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));

		String prepend = "\"" + tdfout.print(timestamp) + "\",\"" + id + "\",\"" + filePath + "\",";

		long dead = -1;
		long rt[] = new long[3];

		for (int i = 0; i < 3; ++i){
			rt[i] = -1;
		}

		for (EventContainer e : events){

			MATBEvent row = e.matb;

			if (row.event.matches("(Resource Management|System Monitoring|Communications)")){

				if (row.eventType == MATBEvent.EventType.SubjectResponse){

					long time = row.time.getMillis();
					long curDead = 0;

					if (dead != -1){
						curDead = time - dead; //Gets dead time since last response.
					}

					dead = time;

					long reaction = -1;

					if (!row.comment.contains("Inappropriate")){

						if (row.event.equals("Resource Management")){
							reaction = time - rt[0];
							rt[0] = -1;
						} else if (row.event.equals("System Monitoring")){
							reaction = time - rt[1];
							rt[1] = -1;
						} else if (row.event.equals("Communications")){
							reaction = time - rt[2];
							rt[2] = -1;
						}

					}

					try {
						out.append(prepend + row.event + "," + ((reaction!=-1)?reaction:"") + "," + curDead + "\r\n");
					} catch (IOException e1) {
						Console.error("An error occured writting to the file!");
					}

				} else if (row.eventType == MATBEvent.EventType.EventProcessed){
					
					long time = row.time.getMillis();

					if (row.event.equals("Resource Management")){
						rt[0] = time;
					} else if (row.event.equals("System Monitoring")){
						rt[1] = time;
					} else if (row.event.equals("Communications")){
						rt[2] = time;
					}

				}

			}

		}

		return out;
	}

}
