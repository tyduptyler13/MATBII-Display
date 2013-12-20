import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MATBEvent{

	public enum EventType{
		EventProcessed("Event Processed"),
		DeviceInit("Device Initialization"),
		RecordingInterval("Recording Inverval Triggered"),
		SubjectResponse("Subject Response"),
		EventTerminated("Event Terminated");

		public final String type;
		EventType(String type){
			this.type = type;
		}
	}

	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
	private static final String del = "[:?\\s]{2,}";

	/**
	 * Default csv header fields.
	 */
	public static final String header = "\"Time\",\"Event Number\",\"EventType\",\"Details\",\"Comment\"";

	public Date time;
	public int eventNumber = -1;
	public EventType eventType;
	public String event = "";
	public String comment = "";

	/**
	 * Requires a line from the MATB file.
	 * @param line
	 * @throws ParseException 
	 */
	public MATBEvent(String line) throws ParseException{

		parse(line);

	}

	private void parse(String line) throws ParseException{

		if (line.charAt(0) == '#') throw new ParseException("Invalid line: '" + line + "'", 0);

		String[] parts = line.split(del);

		time = sdf.parse(parts[0]);

		try {

			//Has an event number.
			if (parts[1].matches("[0-9]+")){

				eventNumber = Integer.parseInt(parts[1]);

				if (parts[2].contains("Event Processed")){
					eventType = EventType.EventProcessed;
					event = parts[3];
					if (4 < parts.length)
						comment = parts[4];
				}

			} else {//Doesn't have an event number.

				int part = 1;

				if (parts[part].contains("Device Initialization")){
					eventType = EventType.DeviceInit;
					if (part+1 < parts.length)
						comment = parts[part+1];
				} else if (parts[part].contains("Recording Interval")){
					eventType = EventType.RecordingInterval;
					event = parts[part].split(": ")[1];
				} else if (parts[part].contains("Subject Response")){
					eventType = EventType.SubjectResponse;
					event = parts[part].split(": ")[1];
					if (part + 1 < parts.length)
						comment = parts[part+1];
				} else if (parts[part].contains("Event Terminated")){
					eventType = EventType.EventTerminated;
					event = parts[part].split(": ")[1];
					if (part + 1 < parts.length){
						comment = parts[part+1];
					}
				}

			}


		} catch (Exception e){
			throw new ParseException("Could not parse: '" + line + "'", 0);
		}
	}

	public String toString(){

		String ret = "\"" + sdf.format(time) + "\"";

		ret += ",\"" + ((eventNumber != -1)?eventNumber:"") + "\"";

		ret += ",\"" + eventType.type + "\"";

		ret += ",\"" + event + "\"";

		ret += ",\"" + comment + "\"";
		
		return ret;

	}

}

