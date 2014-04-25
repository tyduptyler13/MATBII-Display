import java.text.ParseException;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public abstract class ReaderInterface{
	
	//private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
	
	private static final DateTimeFormatter timeFormat = DateTimeFormat.forPattern("HH:mm:ss.S");
	
	protected static final String del = "[:?\\s]{2,}";
	protected static final String ccleaner = "(^[-\\s]+|[-\\s]+$)"; //Comment Cleaner
	
	public LocalTime time;
	
	public abstract LocalTime parse(String line) throws ParseException;
	
	/**
	 * Prints out the data in a csv format excluding headers.
	 */
	@Override
	public abstract String toString();
	
	public static final LocalTime readDate(String s){
		return timeFormat.parseLocalTime(s.trim());
	}
	
	public static final String printDate(LocalTime time){
		return timeFormat.print(time);
	}
	
}
