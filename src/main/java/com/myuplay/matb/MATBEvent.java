package com.myuplay.matb;

import java.text.ParseException;

import org.joda.time.LocalTime;

public class MATBEvent extends ReaderInterface {

	public static enum EventType {
		EventProcessed("Event Processed"), DeviceInit("Device Initialization"), RecordingInterval(
				"Recording Inverval Triggered"), SubjectResponse(
						"Subject Response"), EventTerminated("Event Terminated"), Information(
								"Information");

		public final String type;

		EventType(String type) {
			this.type = type;
		}
	}

	/**
	 * Default csv header fields.
	 */
	public static final String header = "\"Event Number\",\"EventType\",\"Details\",\"Comment\"";
	public static final int hcount = 4;

	// Time included from superclass
	public int eventNumber = -1;
	public EventType eventType;
	public String event = "";
	public String comment = "";

	/**
	 * Dud constructor. If this is used you need to use parse to fill it.
	 */
	public MATBEvent() {
	}

	/**
	 * Requires a line from the MATB file.
	 * 
	 * @param line
	 * @throws ParseException
	 */
	public MATBEvent(String line) throws ParseException {

		parse(line);

	}

	@Override
	public LocalTime parse(String line) throws ParseException {

		if (line.isEmpty() || line.charAt(0) == '#')
			throw new ParseException("Invalid line: '" + line + "'", 0);

		String[] parts = line.split(del);

		time = readDate(parts[0]);

		try {

			// Has an event number.
			if (parts[1].matches("[0-9]+")) {

				eventNumber = Integer.parseInt(parts[1]);

				if (parts[2].contains("Event Processed")) {
					eventType = EventType.EventProcessed;
					event = parts[3];
					if (4 < parts.length)
						comment = parts[4].replaceAll(ccleaner, "");
				}

			} else {// Doesn't have an event number.

				if (parts[1].contains("Device Initialization")) {
					eventType = EventType.DeviceInit;
					if (2 < parts.length)
						comment = parts[2].replaceAll(ccleaner, "");
				} else if (parts[1].contains("Recording Interval")) {
					eventType = EventType.RecordingInterval;
					event = parts[2];
				} else if (parts[1].contains("Subject Response")) {
					eventType = EventType.SubjectResponse;
					event = parts[2];
					if (3 < parts.length)
						comment = parts[3].replaceAll(ccleaner, "");
				} else if (parts[1].contains("Event Terminated")) {
					eventType = EventType.EventTerminated;
					event = parts[2];
					if (3 < parts.length)
						comment = parts[3].replaceAll(ccleaner, "");
				} else if (parts[2].contains("Information")) { // Hacky fix for
					// a dash. :(
					eventType = EventType.Information;
					event = parts[3];
					if (3 < parts.length)
						comment = parts[4].replaceAll(ccleaner, "");
				}

			}

		} catch (Exception e) {
			throw new ParseException("Could not parse: '" + line + "'", 0);
		}

		return time;

	}

	@Override
	public String toString() {

		String ret = "\"" + ((eventNumber != -1) ? eventNumber : "") + "\"";

		ret += ",\"" + eventType.type + "\"";

		ret += ",\"" + event + "\"";

		ret += ",\"" + comment + "\"";

		return ret;

	}

	/**
	 * Shallow compare of event types.
	 * @param e
	 * @return
	 */
	public boolean equals(MATBEvent e){

		return e.event.equals(event);

	}

}
