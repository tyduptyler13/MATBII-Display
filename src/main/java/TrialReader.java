import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.ListIterator;

import javafx.concurrent.Task;

import org.joda.time.LocalTime;
import org.joda.time.Period;

/**
 * Concurrent method of reading files.
 * 
 * Returns status message when finished.
 * @author Tyler
 *
 */
public class TrialReader extends Task<String>{

	private final File[] files;
	private final List<EventContainer> events;

	private final Trial t;

	private boolean hasGoodId = false;

	public TrialReader(File[] in, Trial trial){
		super();
		files = in;
		t = trial;
		events = t.events;
	}

	/**
	 * Specialized for matb event entry.
	 * @param time
	 * @return
	 */
	private EventContainer getEventMATB(LocalTime time){

		for (EventContainer e : events) {
			if (e.equals(time) && e.matb == null){ //Special condition for matb events. A matb cannot already exist there or a new one must be created.
				return e;
			}
		}

		EventContainer ret = new EventContainer(time);
		events.add(ret);

		return ret;

	}

	private EventContainer getEvent(LocalTime time){

		for (EventContainer e : events) {
			if (e.equals(time)){
				return e;
			}
		}

		EventContainer ret = new EventContainer(time);
		events.add(ret);

		return ret;

	}

	private EventContainer getEvent(LocalTime time, String event){

		for (EventContainer e : events) {
			if (e.equals(time) && e.matb != null && e.matb.event.equals(event)){
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

						t.id = line.split(":\\s*")[1].trim();
						t.title.setText("Trial " + t.id + " " + Trial.tdfout.print(t.timestamp));
						hasGoodId = true; //Skip this change in the future. No need to waste time.

					}

					continue;
				}

				try{

					if (f.getName().startsWith("MATB")){
						MATBEvent event = new MATBEvent();
						LocalTime time = event.parse(line);
						getEventMATB(time).matb = event;
					} else if (f.getName().startsWith("COMM")){
						COMMEvent event = new COMMEvent();
						LocalTime time = event.parse(line);
						getEvent(time, "Communications").comm = event;
					} else if (f.getName().startsWith("SYSM")){
						SYSMEvent event = new SYSMEvent();
						LocalTime time = event.parse(line);
						getEvent(time).sysm = event;
					} else if (f.getName().startsWith("TRCK")){
						TRCKEvent event = new TRCKEvent();
						LocalTime time = event.parse(line);
						getEvent(time, "Tracking").trck = event;
					} else if (f.getName().startsWith("RMAN")){
						RMANEvent event = new RMANEvent();
						LocalTime time = event.parse(line);
						getEvent(time).rman = event;
					} else if (f.getName().startsWith("WRS")){
						WRSEvent event = new WRSEvent();
						LocalTime time = event.parse(line);
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

	private void fixCOMMs(){

		ListIterator<EventContainer> list = events.listIterator();
		int counter = 0;

		while (list.hasNext()){//Stage first comm event. It is possible to never go into inner loop. (State 1)

			EventContainer start = list.next();

			if (start.matb != null && start.matb.eventType.equals(MATBEvent.EventType.EventProcessed)
					&& start.matb.event.equals("Communications")){

				EventContainer last = null;

				while (list.hasNext()){//Iterate through all elements. (State 2)

					EventContainer current = list.next();

					if (current.matb != null && current.matb.event.equals("Communications")){

						counter++;

						if (current.matb.eventType.equals(MATBEvent.EventType.EventProcessed)){

							//Total count needs to be tracked here.
							if (last != null && last.comm != null){
								last.comm.remarks = counter + " user interactions before enter";
							}
							counter = 0; //Reset counter.
							start = current;
							continue;

						} else if (current.matb.eventType.equals(MATBEvent.EventType.SubjectResponse)) {

							if (current.comm != null) {

								Period p = new Period(start.time, current.time);
								Console.print("Adjusting reaction time from " + current.comm.rt, "DEBUG");
								current.comm.rt = p.getMinutes() * 60 + p.getSeconds() + ((float)p.getMillis()) / 1000; //Convert period to seconds with floating point.
								Console.print("to " + current.comm.rt, "DEBUG");

							} else {

								Console.print("Creating custom COMM event for user interaction.", "DEBUG");
								current.comm = new COMMEvent();
								Period p = new Period(start.time, current.time);
								current.comm.rt = current.comm.rt = p.getMinutes() * 60 + p.getSeconds() + ((float)p.getMillis()) / 1000; //Convert period to seconds with floating point.
								current.comm.remarks = "Generated COMM event";

							}

						}

						last = current;

					}

				}

			}

		}

	}

	@Override
	protected String call() throws Exception {

		updateMessage("Ready to read");
		updateProgress(10, 100);

		for (File f : files){

			readFile(f);

		}

		t.sortData();

		updateMessage("Fixing COMM Events");
		updateProgress(50, 100);
		Console.log("Correcting reaction times for COMM events");

		fixCOMMs();

		Console.log("Corrected COMM event reaction times.");

		updateMessage("Processed");
		updateProgress(100, 100);

		return "Successfully read in Trial " + t.id;
	}

}
