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

	/*****************************************
	 * Begin the stats section.              *
	 *****************************************/

	/**
	 * Returns CVS formated header.
	 * @return
	 */
	public static String getStatsHeader() {
		return header
				+ ",Time,Event,"
				+ "Block Duration,Block ID,"
				+ "COMM->SYSM,COMM->RMAN,COMM->TRCK,SYSM->COMM,SYSM->RMAN,SYSM->TRCK,RMAN->SYSM,"
				+ "RMAN->COMM,RMAN->TRCK,TRCK->SYSM,TRCK->COMM,TRCK->RMAN";
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

	private static String advGetDirection(String lastXIdle, SuperIterator it, EventContainer current) {

		if (lastXIdle != null && !isIdle(it)) { // We have an event that
			// isn't null before us
			// and the current event
			// isn't null

			return getDirection(lastXIdle, current.matb.event);

		}

		// If we can't have the above conditions then return nochange (empty)
		return EventChange.NOCHANGE.toString();

	}

	public BufferedWriter getStats(BufferedWriter out) throws IOException {

		String absolutePath = files[0].getAbsolutePath();
		String filePath = absolutePath.substring(0,
				absolutePath.lastIndexOf(File.separator));

		// Data relevant to which file the data is coming from.
		String prepend = "\"" + tdfout.print(timestamp) + "\",\"" + id
				+ "\",\"" + filePath + "\",";

		//New object container for handling hard work.
		ECList list = new ECList(events);
		SuperIterator it = list.iterator();

		int blockNumber = 0;

		String lastXIdle = null;

		// Go through all events.
		while (it.hasNext()) {

			EventContainer current = it.next();

			if (!current.hasMATB())
				continue; // Skip rows that don't have matb.

			//Increment changes.
			blockNumber++;

			String blockSection;

			//Block code.
			//We should always consume a block.
			Period blockTime = consumeBlock(it);

			if (blockTime.equals(Period.ZERO)){
				blockTime.minusSeconds(-1); //Subtract.
			}

			blockSection = printPeriod(blockTime) //No longer have single events.
					+ "," + blockNumber + "," //We know the block changed.
					+ advGetDirection(lastXIdle, it, current);

			// Print formatting.
			String ret = prepend + "\"" + ReaderInterface.printDate(current.time) + "\","
					+ (current.hasTRCK() ? (isIdle(it) ? "Idle" : "Tracking") : current.matb.event)
					+ "," + blockSection + ",";


			out.append(ret + "\r\n");

			// Special case of tracking non idle for switching.
			if (!isIdle(it))// If not idle.
				lastXIdle = current.matb.event;// Track non idle.

		}

		return out;
	}

	/**
	 * New block consuming code.
	 * @param it
	 * @return
	 */
	private static Period consumeBlock(SuperIterator it){

		EventContainer start = it.peek(0); //First block that is considered part of the block.

		boolean isIdle = isIdle(it); //Check if start is idle.

		while (it.hasNext()){

			EventContainer current = it.peek();

			if (!start.equals(current.matb)){ //The events are different.

				if (it.has(1) && isIdle(it.cloneAt(1))){ //Is the next event idle.

					if (it.has(2) && isIdle(it.cloneAt(2))){ //Is the one after the next idle?
						break;
					}

					//continue because this is ok to have just one idle.

				} else { //The next event was not idle. Break out.
					break;
				}

			} else if (current.hasTRCK()){//Special logic for tracking events.

				if (isIdle){ //Is this block idle?

					//This will get hit if both events are tracking and the block is idle.
					if (!isIdle(it.cloneAt(1))){
						//The next event is tracking. Stop the idle block.
						break;
					}

				} else { //Not idle

					//This will get hit only if it[0] and it[1] are tracking.
					//We don't know if it[2] is tracking so do a cloneAtTRCK instead.
					if (isIdle(it.cloneAt(1)) && isIdle(it.cloneAtTRCK(2))){
						//The next 2 events are idle so we should leave the block.
						break;
					}

				}

			}

			it.next(); //Increment.

		}

		return new Period(start.time, it.peek(0).time);

	}


	private static String printPeriod(Period p) {
		return ReaderInterface.printDate(new LocalTime(-61200000).plus(p));//Fix the weird 7 hour thing.
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

		Boolean val;
		if ( (val = it.isIdle()) != null){
			return val.booleanValue();
		}

		boolean tmp;

		if (!it.peek(0).hasTRCK()){ //Has to be a tracking event to be idle.
			it.setIdle(false);
			return false;	
			//Need at least 3 events for comparison
		} else if (checkTime(it, -1, 0, 1)) { //Prefer this first.
			tmp = getDeviation(it.peekTRCK(-1), it.peekTRCK(0), it.peekTRCK(1)) <= 16 * 2;
		} else if (checkTime(it, 0, 1, 2)){ //Maybe we don't have a previous event. Look ahead instead.
			tmp = getDeviation(it.peekTRCK(0), it.peekTRCK(1), it.peekTRCK(2)) <= 16 * 2;
		} else if (checkTime(it, -2, -1, 0)){ //Don't have events in front? Check behind.
			tmp = getDeviation(it.peekTRCK(-2), it.peekTRCK(-1), it.peekTRCK(0)) <= 16 * 2;
		} else {
			tmp = isIdle(it.peek(0));
		}

		it.setIdle(tmp);
		return tmp;
	}

	private static boolean isIdle(EventContainer current) {
		return (current.hasTRCK() && current.trck.compass.equals("C"));
	}

	private static boolean checkTime(SuperIterator it, int ... offsets){

		for (int offset : offsets){
			if (!it.hasTRCK(offset))
				return false; //Missing a position.
		}

		for (int i = 0; i + 1 < offsets.length; ++i){

			Period diff = new Period(it.peekTRCK(offsets[i]).time, it.peekTRCK(offsets[i + 1]).time);

			if (diff.getSeconds() > 10)
				return false; //An event is outside of the allowed time.

		}

		return true;

	}

}
