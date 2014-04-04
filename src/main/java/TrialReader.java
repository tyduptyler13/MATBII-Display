import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javafx.concurrent.Task;

import org.joda.time.LocalTime;

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

						t.id = line.split(":\\s*")[1].trim();
						t.title.setText("Trial " + t.id + " " + Trial.tdfout.print(t.timestamp));
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

	private void fixCOMMs(){

		ListIterator<EventContainer> list = events.listIterator();

		while (list.hasNext()){

			EventContainer e = list.next();

			if (e.comm != null){

				while (e != null){
					e = fixCOMM(list, e.comm);
				}

			}

		}


	}

	private EventContainer fixCOMM(ListIterator<EventContainer> list, COMMEvent start){

		while (list.hasNext()){

			EventContainer e = list.next();

			//if (e.)

		}

		return null;

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
