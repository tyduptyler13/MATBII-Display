import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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

		private LinkedList<EventContainer> getEvents(LocalTime time){

			LinkedList<EventContainer> eventlist = new LinkedList<EventContainer>();
			
			for (EventContainer e : events) {
				if (e.equals(time)){
					eventlist.add(e);
				}
			}

			if (eventlist.size() == 0){
				EventContainer ret = new EventContainer(time);
				events.add(ret);
				eventlist.add(ret);
			}
			
			return eventlist;

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
							LocalTime time = event.parse(line);
							for (EventContainer e : getEvents(time)){
								e.comm = event;
							}
						} else if (f.getName().startsWith("SYSM")){
							SYSMEvent event = new SYSMEvent();
							LocalTime time = event.parse(line);
							for (EventContainer e : getEvents(time)){
								e.sysm = event;
							}
						} else if (f.getName().startsWith("TRCK")){
							TRCKEvent event = new TRCKEvent();
							LocalTime time = event.parse(line);
							for (EventContainer e : getEvents(time)){
								e.trck = event;
							}
						} else if (f.getName().startsWith("RMAN")){
							RMANEvent event = new RMANEvent();
							LocalTime time = event.parse(line);
							for (EventContainer e : getEvents(time)){
								e.rman = event;
							}
						} else if (f.getName().startsWith("WRS")){
							WRSEvent event = new WRSEvent();
							LocalTime time = event.parse(line);
							for (EventContainer e : getEvents(time)){
								e.wrs = event;
							}
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
		return header + ",\"Time\",\"Event\",\"Time Since Last\"," +
				"\"Block Duration\",\"Event Changed (Counts up)\",\"Block ID (Tracking vs other)\"," +
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
			} else if (current.equals("Tracking")){
				return EventChange.NOCHANGE.toString();//Special case.
			}

		}

		//Something has gone wrong.
		Console.log("WARNING: Statistics trasition could not be found. Directed graph may be inaccurate.");

		return EventChange.NOCHANGE.toString();

	}

	public BufferedWriter getStats(BufferedWriter out){

		String absolutePath = files[0].getAbsolutePath();
		String filePath = absolutePath.substring(0,absolutePath.lastIndexOf(File.separator));

		//Data relevant to which file the data is coming from.
		String prepend = "\"" + tdfout.print(timestamp) + "\",\"" + id + "\",\"" + filePath + "\",";

		LinkedList<EventContainer> eventList = new LinkedList<EventContainer>();

		//Prelookup.
		for (EventContainer event : events){

			if (event.matb == null) continue;

			if (event.matb.event.matches("(Resource Management|System Monitoring|Communications|Tracking)") && //Must be one of these types.
					(event.matb.eventType == MATBEvent.EventType.SubjectResponse //Accept any subject responce types.
					|| (event.matb.event.equals("Tracking")))){//Ignore track with c
				eventList.add(event);
			}

		}

		ListIterator<EventContainer> list = eventList.listIterator();

		//Persistant variables.
		EventContainer last = null;

		int changeCounter = 0;
		int blockCounter = 0;

		//Go through all events.
		while (list.hasNext()){

			EventContainer current = list.next();
			MATBEvent row = current.matb;

			if (row == null) continue; //Skip rows that don't have matb.

			//Prelookups.
			EventContainer next1;
			next1 = (list.hasNext()?list.next():null);
			if (next1!=null) list.previous();

			//Change detection
			boolean changeFlag = false;
			if (last == null || !last.matb.event.equals(row.event) || (last.trck != null && current.trck != null && 
					stateChange(last.trck, current.trck)) ){
				changeCounter++;
				changeFlag = true;
			}

			String blockSection;

			//Block Change and blocking (chunking) code.
			//This is true if the last event differs with this event and the next event is the same as this event
			//This also checks to make sure that tracking has the same compass as the next (these are considered differently)
			if ( changeFlag && row.event.equals(next1.matb.event) && 
					(next1.matb.event.equals("Tracking")? //Conditional conditional
							!stateChange(current.trck, next1.trck):true)){ //TODO Possibly needs fixes.

				Period liveTime = null;

				if (row.event.equals("Tracking")){

					while (list.hasNext()){

						EventContainer e = list.next();

						//Tracking events will be groups into single events.
						//Either we hit a non tracking event or we hit a idle tracking state.
						if (!e.matb.event.equals("Tracking") || (e.trck != null && stateChange(e.trck, current.trck))){//Keep the null check!
							//Skipping events of the same type. We used the same iterator so it will remove them from the list.
							list.previous();
							liveTime = new Period(row.time, e.time);
							break;
						}

					}

				}


				blockSection = (liveTime!=null? printPeriod(liveTime):"") + "," + (changeFlag?changeCounter:"") + "," + (++blockCounter)
						+ "," + (last!=null? getDirection(last.matb.event, row.event) : EventChange.NOCHANGE.toString());

			} else {

				blockSection = "," + (changeFlag?changeCounter:"") + ",," + EventChange.NOCHANGE.toString();

			}

			Period lastDiff = (last!=null ? new Period(last.time, row.time) : new Period(row.time.getMillisOfDay())); //TODO Check this.

			//Print formatting.

			String ret = prepend + "\"" + ReaderInterface.printDate(row.time) + "\","+ (row.event.equals("Tracking")?
					(current.trck.compass.equals("C")?"Idle":"Tracking"):row.event) +
					"," + printPeriod(lastDiff) + "," + blockSection + ",";

			try {
				out.append(ret + "\r\n");
			} catch (IOException e1) {
				Console.error("An error occured writting to the file!");
			}

			last = current;

		}

		return out;
	}

	private static String printPeriod(Period p){
		return ReaderInterface.printDate(new LocalTime(-61200000).plus(p));//Because someone is stupid and thinks 17 hours is 0 milliseconds. -_-
	}

	/**
	 * True if the state changes from idle to tracking or tracking to idle.
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean stateChange (TRCKEvent a, TRCKEvent b){
		//Idle to tracking || tracking to idle.
		return ((a.compass.equals("C") && !b.compass.equals("C")) || (!a.compass.equals("C") && b.compass.equals("C")));

	}

}
