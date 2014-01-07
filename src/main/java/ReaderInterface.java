import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class ReaderInterface{
	
	protected static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
	protected static final String del = "[:?\\s]{2,}";
	protected static final String ccleaner = "(^[-\\s]+|[-\\s]+$)"; //Comment Cleaner
	
	public Date time;
	
	public abstract Date parse(String line) throws ParseException;
	
	/**
	 * Prints out the data in a csv format excluding headers.
	 */
	public abstract String toString();
	
}
