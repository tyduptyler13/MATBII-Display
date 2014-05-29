package com.myuplay.matb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.myuplay.matb.ECList.SuperIterator;

public class Trial extends VBox {

	public Text title;

	public String id;
	public final DateTime timestamp;
	private ProgressBar progress;

	private final File[] files;

	public static final DateTimeFormatter tdfin = DateTimeFormat
			.forPattern("yyyy_MMddHHmm");
	public static final DateTimeFormatter tdfout = DateTimeFormat
			.forPattern("yyyy/MM/dd hh:mma");

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
	 * @param name
	 *            - Requires a trial name for display and printing.
	 * @param stamp
	 *            - String version of the associated timestamp.
	 * @param files
	 *            - A list of associated files to parse.
	 * @param rootWindow
	 *            - Access to the root window for opening dialogs.
	 * @throws ParseException
	 */
	public Trial(int id, String stamp, File[] files) throws ParseException {
		super();

		this.id = Integer.toString(id); // TODO Temporary fix.
		this.files = files;
		timestamp = tdfin.parseDateTime(stamp);

		title = new Text("Trial " + id + " " + tdfout.print(timestamp));

		progress = new ProgressBar();

		getChildren().addAll(title, progress);

	}

	public Task<String> setupTask() {

		TrialReader r = new TrialReader(files, this);
		r.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent t) {

				Console.log("[Parser] " + t.getSource().getValue());

			}

		});

		r.setOnFailed(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent t) {

				Console.log("[Parser] Failed to parse! Check logs!");

				t.getSource().getException().printStackTrace();

			}

		});

		progress.progressProperty().bind(r.progressProperty());

		return r;

	}

	/**
	 * Prints out data that can identify everything about this trial in a csv
	 * format.
	 */
	@Override
	public String toString() {
		String ret = Trial.getHeader() + "\r\n";

		String absolutePath = files[0].getAbsolutePath();
		String filePath = absolutePath.substring(0,
				absolutePath.lastIndexOf(File.separator));

		String prepend = "\"" + tdfout.print(timestamp) + "\",\"" + id
				+ "\",\"" + filePath + "\",";

		for (EventContainer e : events) {
			ret += prepend + e.toString() + "\r\n";
		}

		return ret;
	}

	/**
	 * This reduces the characters required in a csv line so that it doesn't
	 * take up as much space.
	 * 
	 * @param in
	 * @return
	 */
	private String cleanCSV(String in) {

		return in.replaceAll("\"\"", "");

	}

	/**
	 * A safer and faster stream alternative to the default toString.
	 * 
	 * Immediately offloads saving to file by printing directly to it, instead
	 * of building a string and recursively returning it.
	 * 
	 * @param out
	 *            - Required output for printing to.
	 * @return Returns the printstream that was passed in.
	 * @throws IOException
	 */
	public BufferedWriter toString(BufferedWriter out) throws IOException {

		return toString(out, false);

	}

	/**
	 * Same as other to string of same type. This one just allows you to control
	 * the header printing.
	 * 
	 * @param out
	 *            - Where to print to.
	 * @param useHeader
	 *            - Include header?
	 * @return out
	 * @throws IOException
	 */
	public BufferedWriter toString(BufferedWriter out, boolean useHeader)
			throws IOException {

		if (useHeader) {
			out.append(Trial.getHeader() + "\r\n");
		}

		String absolutePath = files[0].getAbsolutePath();
		String filePath = absolutePath.substring(0,
				absolutePath.lastIndexOf(File.separator));

		String prepend = "\"" + tdfout.print(timestamp) + "\",\"" + id
				+ "\",\"" + filePath + "\",";

		for (EventContainer e : events) {
			out.append(cleanCSV(prepend + e.toString() + "\r\n"));
		}

		return out;
	}

	public void sortData() {
		Collections.sort(events);
	}

	public static String getHeader() {
		return Trial.header + ',' + EventContainer.getHeader();
	}

	// Begin stats functions

	public static String getStatsHeader() {
		return header
				+ ",\"Time\",\"Event\",\"Time Since Last\","
				+ "\"Block Duration\",\"Event Changed (Counts up)\",\"Block ID (Tracking vs other)\","
				+ "\"COMM->SYSM\",\"COMM->RMAN\",\"COMM->TRCK\",\"SYSM->COMM\",\"SYSM->RMAN\",\"SYSM->TRCK\",\"RMAN->SYSM\",\""
				+ "RMAN->COMM\",\"RMAN->TRCK\",\"TRCK->SYSM\",\"TRCK->COMM\",\"TRCK->RMAN\"";
	}

	private static enum EventChange {
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

		private EventChange(String s) {
			out = s;
		}

		public String toString() {
			return out;
		}

	}

	private static String getDirection(String last, String current) {

		if (last.equals("Communications")) {

			if (current.equals("Resource Management")) {
				return EventChange.COMMRMAN.toString();
			} else if (current.equals("System Monitoring")) {
				return EventChange.COMMSYSM.toString();
			} else if (current.equals("Tracking")) {
				return EventChange.COMMTRCK.toString();
			}

		} else if (last.equals("Resource Management")) {

			if (current.equals("Communications")) {
				return EventChange.RMANCOMM.toString();
			} else if (current.equals("System Monitoring")) {
				return EventChange.RMANSYSM.toString();
			} else if (current.equals("Tracking")) {
				return EventChange.RMANTRCK.toString();
			}

		} else if (last.equals("System Monitoring")) {

			if (current.equals("Communications")) {
				return EventChange.SYSMCOMM.toString();
			} else if (current.equals("Resource Management")) {
				return EventChange.SYSMRMAN.toString();
			} else if (current.equals("Tracking")) {
				return EventChange.SYSMTRCK.toString();
			}

		} else if (last.equals("Tracking")) {

			if (current.equals("Resource Management")) {
				return EventChange.TRCKRMAN.toString();
			} else if (current.equals("Communications")) {
				return EventChange.TRCKCOMM.toString();
			} else if (current.equals("System Monitoring")) {
				return EventChange.TRCKSYSM.toString();
			} else if (current.equals("Tracking")) {
				return EventChange.NOCHANGE.toString();// Special case.
			}

		}

		// Something has gone wrong.
		// This isn't true anymore. We do checks all the time regardless of
		// change detected.
		// Console.log("WARNING: Statistics trasition could not be found. Directed graph may be inaccurate.");

		return EventChange.NOCHANGE.toString();

	}

	private static String advGetDirection(String lastXIdle, EventContainer current) {

		if (lastXIdle != null && !isIdle(current)) { // We have an event that
			// isn't null before us
			// and the current event
			// isn't null

			return getDirection(lastXIdle, current.matb.event);

		}

		// If we can't have the above conditions then return nochange (empty)
		return EventChange.NOCHANGE.toString();

	}

	public BufferedWriter getStats(BufferedWriter out) {

		String absolutePath = files[0].getAbsolutePath();
		String filePath = absolutePath.substring(0,
				absolutePath.lastIndexOf(File.separator));

		// Data relevant to which file the data is coming from.
		String prepend = "\"" + tdfout.print(timestamp) + "\",\"" + id
				+ "\",\"" + filePath + "\",";

		//New object container for handling hard work.
		ECList list = new ECList(events);
		SuperIterator it = list.iterator();

		int changeCounter = 0;
		int blockCounter = 0;
		
		String lastXIdle = null;

		// Go through all events.
		while (it.hasNext()) {

			EventContainer current = it.next();

			if (!current.hasMATB())
				continue; // Skip rows that don't have matb.

			// Change detection
			boolean changeFlag = false;
			SuperIterator tmp = it.clone(); //Copy of iterator.
			tmp.previousTRCK(); //Move back one track event.
			if ((it.hasPrevious() && !it.peekPrevious().equals(current.matb)) ||
					(it.hasPreviousTRCK() && current.hasTRCK() && stateChange(tmp, it))) { //Test previous track event against current.
				changeCounter++;
				changeFlag = true;
			}

			String blockSection;

			// Block Change and blocking (chunking) code.
			// This is true if the last event differs with this event and the
			// next event is the same as this event
			// This also checks to make sure that tracking has the same compass
			// as the next (these are considered differently)
			tmp = it.clone();
			tmp.nextTRCK();
			if (changeFlag && it.hasNext() && it.peek().equals(current.matb)
					&& (it.isNextTracking() ? !stateChange(it, tmp) : true)) {

				Period liveTime = null;

				if (current.hasTRCK()) {
					
					SuperIterator cit = it.clone(); //Clone of iterator at current position before loop.
					//Consider the first element.

					while (it.hasNext()) {

						tmp = it.clone();
						EventContainer e = tmp.next();

						// This tests to see if either we hit a non tracking
						// event (total miss) or we
						// hit a statechange idle-tracking etc.
						if (!e.hasTRCK()) {

							// Skipping events of the same type. We used the
							// same iterator so it will remove them from the
							// list.
							liveTime = new Period(current.time, it.peek(0).time);
							break;

						} else if (stateChange(cit, tmp)) { //Is this idle, is the next idle?

							if (isIdle(cit)) { //Did we start idle?

								// State change in an idle block behaves as
								// normal. Break immediately.

								// Skipping events of the same type. We used the
								// same iterator so it will remove them from the
								// list.
								liveTime = new Period(current.time, it.peek(0).time);
								break;

							} else { // Tracking block

								// This needs to be handled differently. We will
								// only break if the next next event in
								// comparison to
								// e is also stateChange compared to the initial
								// event. (Single idle events are allowed in a
								// block)

								// We already know that current.trck exists and
								// can use this as additive logic.
								SuperIterator tmp2 = tmp.clone();
								tmp2.next();
								if (stateChange(tmp, tmp2)) { //Looking at n + 1 and n + 2

									// We only need to roll back once. We did a
									// e2 lookahead.
									liveTime = new Period(current.time, it.peek(0).time);
									break;

								}

							}

						}

						it = tmp; //Just remove the old iterator. Inexpensive.

					}

				}

				blockSection = (liveTime != null ? printPeriod(liveTime) : "")
						+ "," + (changeFlag ? changeCounter : "") + ","
						+ (++blockCounter) + ","
						+ advGetDirection(lastXIdle, current);

			} else { // Not a tracking block

				blockSection = "," + (changeFlag ? changeCounter : "") + ",,"
						+ advGetDirection(lastXIdle, current);

			}

			Period lastDiff = (it.hasPrevious() ? new Period(it.peekPrevious().time, current.time)
			: new Period(current.time.getMillisOfDay()));

			// Print formatting.

			String ret = prepend
					+ "\""
					+ ReaderInterface.printDate(current.time)
					+ "\","
					+ (current.hasTRCK() ? (isIdle(it) ? "Idle" : "Tracking") : current.matb.event)
							+ "," + printPeriod(lastDiff) + "," + blockSection + ",";

			try {
				out.append(ret + "\r\n");
			} catch (IOException e1) {
				Console.error("An error occured writting to the file!");
			}

			// Special case of tracking non idle for switching.
			if (!isIdle(it))// If not idle.
				lastXIdle = current.matb.event;// Track non idle.

		}

		return out;
	}

	private static String printPeriod(Period p) {
		return ReaderInterface.printDate(new LocalTime(-61200000).plus(p));//Fix the weird 7 hour thing.
	}

	private static boolean stateChange(ECList.SuperIterator a, ECList.SuperIterator b){
		return (isIdle(a) != isIdle(b));
	}

	/**
	 * Calculates the total deviation between a, b, and c.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	private static double getDeviation(TRCKEvent a, TRCKEvent b, TRCKEvent c) {
		double total = Math.abs(a.x - b.x);
		total += Math.abs(b.x - c.x);
		total += Math.abs(a.y - b.y);
		total += Math.abs(b.y - c.y);
		return total;
	}
	
	/**
	 * Tests the current position of an iteratior for idle.
	 * Uses 4 different tests depending on location and how many track
	 * events are nearby.
	 * 
	 * More accurate than the other isIdle.
	 * @param it
	 * @return
	 */
	private static boolean isIdle(ECList.SuperIterator it){
		if (it.hasTRCK(2)){ //Need at least 3 events for comparison
			return getDeviation(it.peekTRCK(0), it.peekTRCK(1), it.peekTRCK(2)) <= 16;
		} else if (it.hasTRCK(1) && it.hasTRCK(-1)) {
			return getDeviation(it.peekTRCK(0), it.peekTRCK(1), it.peekTRCK(-1)) <= 16;
		} else if (it.hasTRCK(-2)){
			return getDeviation(it.peekTRCK(0), it.peekTRCK(-1), it.peekTRCK(-2)) <= 16;
		} else {
			return isIdle(it.peek(0));
		}
	}
	
	private static boolean isIdle(EventContainer current) {
		return (current.hasTRCK() && current.trck.compass.equals("C"));
	}

}
