import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
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

	public Text title;

	private Node content;
	public String id;
	public final DateTime timestamp;
	private ProgressBar progress;

	private final File[] files;

	public static final DateTimeFormatter tdfin = DateTimeFormat.forPattern("yyyy_MMddHHmm");
	public static final DateTimeFormatter tdfout = DateTimeFormat.forPattern("yyyy/MM/dd hh:mma");

	public ArrayList<EventContainer> events = new ArrayList<EventContainer>();

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

		TrialReader r = new TrialReader(files, this);
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

	public void sortData(){
		Collections.sort(events);
	}

	public static String getHeader(){
		return Trial.header + ',' + EventContainer.getHeader();
	}

	//Begin stats functions

	public static String getStatsHeader(){
		return header + ",\"Time\",\"Event\",\"Reaction time (Time Since Event)\",\"Time Since Last\"," +
				"\"Live Time (Time between blocks)\",\"Event Changed (Counts up)\",\"Block ID (Tracking vs other)\"," +
				"\"COMM->SYSM\",\"COMM->RMAN\",\"COMM->TRCK\",\"SYSM->COMM\",\"SYSM->RMAN\",\"SYSM->TRCK\",\"RMAN->SYSM\",\""+
				"RMAN->COMM\",\"RMAN->TRCK\",\"TRCK->SYSM\",\"TRCK->COMM\",\"TRCK->RMAN\"";
	}

	private enum EventChange{
		COMMSYSM("1,,,,,,,,,,,"),
		COMMRMAN(",1,,,,,,,,,,"),
		COMMTRCK(",,1,,,,,,,,,"),
		SYSMCOMM(",,,1,,,,,,,,"),
		SYSMRMAN(",,,,1,,,,,,,"),
		SYSMTRCK(",,,,,1,,,,,,"),
		RMANSYSM(",,,,,,1,,,,,"),
		RMANCOMM(",,,,,,,1,,,,"),
		RMANTRCK(",,,,,,,,1,,,"),
		TRCKSYSM(",,,,,,,,,1,,"),
		TRCKCOMM(",,,,,,,,,,1,"),
		TRCKRMAN(",,,,,,,,,,,1"),
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
		LocalTime blockStart = new LocalTime(-61200000); //Start of a block. (For live time)

		long rt[] = new long[3];
		int changeCounter = 0;
		int blockCounter = 0;

		for (int i = 0; i < 3; ++i){
			rt[i] = -1;
		}

		ArrayList<MATBEvent> eventList = new ArrayList<MATBEvent>();

		//Prelookup.
		for (int i = 0; i < events.size(); ++i){

			MATBEvent e = events.get(i).matb;

			if (e == null) continue;

			if (e.event.matches("(Resource Management|System Monitoring|Communications|Tracking)") &&
					(e.eventType == MATBEvent.EventType.SubjectResponse
					|| (e.eventType == MATBEvent.EventType.RecordingInterval && e.event.equals("Tracking")))){
				eventList.add(e);
			}

		}

		boolean lastGroupTracking = false; //Magic flag for marking if tracking events are alone.
		ListIterator<MATBEvent> list = eventList.listIterator();

		//Go through all events.
		while (list.hasNext()){

			MATBEvent row = list.next();

			if (row == null) continue; //Skip rows that don't have matb.

			if (row.event.matches("(Resource Management|System Monitoring|Communications|Tracking)")){

				if (row.eventType == MATBEvent.EventType.SubjectResponse || row.eventType == MATBEvent.EventType.RecordingInterval){

					//Reaction Time.

					LocalTime time = row.time;

					MATBEvent next = null, next2 = null;

					if (list.hasNext()){
						next = list.next();
						if (list.hasNext()){
							next2 = list.next();
							list.previous();
						}
						list.previous();
					}

					boolean changeFlag = false;
					if (last != null && !last.event.equals(row.event)){
						changeCounter++;
						changeFlag = true;
					}

					String blockSection;
					Period liveTime = null;

					//Block Change and blocking (chunking) code.
					if ( (lastGroupTracking && last != null && last.event.equals("Tracking") && !row.event.equals("Tracking") ) ||
							(lastGroupTracking = (next != null && row.event.equals("Tracking") && next.event.equals("Tracking"))) ||
							(last != null && !last.event.equals("Tracking") && !row.event.equals("Tracking")) && !last.event.equals(row.event) ){

						if (row.event.equals("Tracking")){
							LocalTime end = null;


							while (list.hasNext()){

								MATBEvent e = list.next();

								//Reaction time is removed for the time being.
								//if (e.eventType == MATBEvent.EventType.EventProcessed) continue;

								if (e.eventType != MATBEvent.EventType.RecordingInterval){
									//Skipping events of the same type. We used the same iterator so it will work.
									list.previous(); //It will get incremented again at while.
									break;
								}

								end = e.time;

							}

							liveTime = new Period(time, end);

						} else {//Non tracking event block start. Keep track of the beginning. We will report the time at the end.

							lastGroupTracking = false;
							blockStart = time;

						}


						blockSection = (liveTime!=null?printPeriod(liveTime):"") + "," + (changeFlag?changeCounter:"") + "," + (++blockCounter)
								+ "," + (last!=null? getDirection(last.event, row.event) : EventChange.NOCHANGE.toString());

					} else {

						//Super look ahead for tracking block to report live time.

						if (next!= null && next2 != null && !row.event.equals("Tracking") && next.event.equals("Tracking")
								&& next2.event.equals("Tracking")){
							liveTime = new Period(blockStart, time);
						}

						blockSection = (liveTime!=null?printPeriod(liveTime):"") + "," + (changeFlag?changeCounter:"") + ",," + EventChange.NOCHANGE.toString();
					}

					Period lastDiff = (last!=null ? new Period(last.time, row.time) : new Period(row.time.getMillisOfDay()));

					//Print formatting.

					String ret = prepend + "\"" + ReaderInterface.printDate(row.time) + "\","+ row.event +
							",," + printPeriod(lastDiff) + "," + blockSection + ",";

					try {
						out.append(ret + "\r\n");
					} catch (IOException e1) {
						Console.error("An error occured writting to the file!");
					}

					last = row; //Keep track of what happened last. (If the event types are different then the block changed)

				}

			}

		}

		return out;
	}

	private static String printPeriod(Period p){
		return ReaderInterface.printDate(new LocalTime(-61200000).plus(p));//Because someone is stupid and thinks 17 hours is 0 milliseconds. -_-
	}

}
