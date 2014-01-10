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
					
					line = line.trim();

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
							EventContainer e = new EventContainer(event.parse(line));
							e.matb = event;
							events.add(e);
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
		return header + ",\"Time\",\"Event\",\"Reaction time (Time Since Event)\",\"Time Since Last\"," +
				"\"Dead Time (Time between blocks)\",\"Event Changed (Counts up)\",\"Block ID (Tracking vs other)\"," +
				"\"COMM->SYSM\",\"COMM->RMAN\",\"COMM->TRCK\",\"SYSM->COMM\",\"SYSM->RMAN\",\"SYSM->TRCK\",\"RMAN->SYSM\",\""+
				"RMAN->COMM\",\"RMAN->TRCK\",\"TRCK->SYSM\",\"TRCK->COMM\",\"TRCK->RMAN\"";
	}

	private enum EventChange{
		COMMSYSM("x,,,,,,,,,,,"),
		COMMRMAN(",x,,,,,,,,,,"),
		COMMTRCK(",,x,,,,,,,,,"),
		SYSMCOMM(",,,x,,,,,,,,"),
		SYSMRMAN(",,,,x,,,,,,,"),
		SYSMTRCK(",,,,,x,,,,,,"),
		RMANSYSM(",,,,,,x,,,,,"),
		RMANCOMM(",,,,,,,x,,,,"),
		RMANTRCK(",,,,,,,,x,,,"),
		TRCKSYSM(",,,,,,,,,x,,"),
		TRCKCOMM(",,,,,,,,,,x,"),
		TRCKRMAN(",,,,,,,,,,,x"),
		NOCHANGE(",,,,,,,,,,,");

		private String out;
		private EventChange(String s){
			out = s;
		}

		public String toString(){
			return out;
		}

	}

	private String getDirection(String last, String current){

		if (last.equals("Communications")){

			if (current.equals("Resource Management")){
				return EventChange.COMMRMAN.toString();
			} else if (current.equals("System Monitoring")){
				return EventChange.COMMSYSM.toString();
			} else if (current.equals("Tracking")){
				return EventChange.COMMTRCK.toString();
			}

		} else if (last.equals("Resource Management")){

			if (current.equals("Communications")){
				return EventChange.RMANCOMM.toString();
			} else if (current.equals("System Monitoring")){
				return EventChange.RMANSYSM.toString();
			} else if (current.equals("Tracking")){
				return EventChange.RMANTRCK.toString();
			}

		} else if (last.equals("System Monitoring")){

			if (current.equals("Communications")){
				return EventChange.SYSMCOMM.toString();
			} else if (current.equals("Resource Management")){
				return EventChange.SYSMRMAN.toString();
			} else if (current.equals("Tracking")){
				return EventChange.SYSMTRCK.toString();
			}

		} else if (last.equals("Tracking")){

			if (current.equals("Resource Management")){
				return EventChange.TRCKRMAN.toString();
			} else if (current.equals("Communications")){
				return EventChange.TRCKCOMM.toString();
			} else if (current.equals("System Monitoring")){
				return EventChange.TRCKSYSM.toString();
			}

		}

		//Something has gone wrong.
		Console.log("WARNING: Statistics trasition could not be found. Directed graph may be inaccurate.");

		return EventChange.NOCHANGE.toString();

	}

	public BufferedWriter getStats(BufferedWriter out){

		String absolutePath = files[0].getAbsolutePath();
		String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));

		String prepend = "\"" + tdfout.print(timestamp) + "\",\"" + id + "\",\"" + filePath + "\",";

		MATBEvent last = null; //Last event read.

		long rt[] = new long[3];
		int changeCounter = 0;
		int blockCounter = 0;

		for (int i = 0; i < 3; ++i){
			rt[i] = -1;
		}

		for (int i = 0; i < events.size(); ++i){

			MATBEvent row = events.get(i).matb;

			if (row == null) continue; //Skip rows that don't have matb.

			if (row.event.matches("(Resource Management|System Monitoring|Communications|Tracking)")){

				MATBEvent next = null;

				//Look aheads. (Expensive)
				for (int j = i+1; j < events.size(); ++j){

					MATBEvent e = events.get(j).matb;
					
					if (e == null) continue;
					
					if (e.event.matches("(Resource Management|System Monitoring|Communications|Tracking)") &&
							(e.eventType == MATBEvent.EventType.SubjectResponse || e.eventType == MATBEvent.EventType.RecordingInterval)){
						next = events.get(j).matb;
						break;
					}
					
				}

				if (row.eventType == MATBEvent.EventType.SubjectResponse || row.event.equals("Tracking")){

					//Reaction Time.

					long reaction = -1;
					long time = row.time.getMillis();

					if (!row.comment.contains("Inappropriate")){

						if (row.event.equals("Resource Management")){
							reaction = time - rt[0];
						} else if (row.event.equals("System Monitoring")){
							reaction = time - rt[1];
						} else if (row.event.equals("Communications")){
							reaction = time - rt[2];
						}

					}

					long lastDiff = 0;

					boolean changeFlag = false;
					if (last != null){
						lastDiff = time - last.time.getMillis();
						
						if (!last.event.equals(row.event)){
							changeCounter++;
							changeFlag = true;
						}
					}

					String blockSection;
					
					
					//Block Change
					if (next != null && last != null && (last.event.equals("Tracking") || row.event.equals("Tracking")) &&
							!last.event.equals(row.event) && (row.event.equals("Tracking") == next.event.equals("Tracking"))){

						blockSection = lastDiff + "," + (changeFlag?changeCounter:"") + "," + (blockCounter++) + "," + getDirection(last.event, row.event);

					} else {
						blockSection = "," + (changeFlag?changeCounter:"") + ",," + EventChange.NOCHANGE.toString();
					}

					//Print formatting.

					String ret = prepend + "\"" + ReaderInterface.printDate(row.time) + "\","+ row.event +
							"," + ((reaction!=-1)?reaction:"") + "," + lastDiff + "," + blockSection + ",";

					try {
						out.append(ret + "\r\n");
					} catch (IOException e1) {
						Console.error("An error occured writting to the file!");
					}
					
					last = row; //Keep track of what happened last. (If the event types are different then the block changed)

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
