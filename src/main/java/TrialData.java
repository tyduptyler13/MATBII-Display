import java.io.PrintStream;
import java.util.ArrayList;


/**
 * A container for any and all data pertaining to a trial.
 * 
 * Also allows for multiple trial data sets to be linked
 * together and printed as a single file using the
 * {@link StatisticsHandler}.
 * 
 * @author Tyler
 *
 */
public class TrialData{

	public Trial parent;
	public ArrayList<MATBEvent> events = new ArrayList<MATBEvent>();

	/**
	 * Prints a valid csv formated string of data.
	 */
	public String toString(){
		String ret = "";
		for (MATBEvent e : events){
			ret += e.toString() + "\r\n";
		}
		return ret;
	}

	/**
	 * A safer and faster stream alternative to the default toString.
	 * @param out - Required output for printing to.
	 * @return Returns the printstream that was passed in.
	 */
	public PrintStream toString(PrintStream out){

		for (MATBEvent e : events){
			out.println(e.toString());
		}

		return out;

	}

}
