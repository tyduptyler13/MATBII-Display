import java.text.ParseException;
import org.joda.time.DateTime;


public class RMANEvent extends ReaderInterface{

	public static final String header = "\"Pump\",\"Pump Action\",\"Tank Update\",\"Tank A\",\"Tank B\",\"Tank C\",\"Tank D\",\"Diff A\",\"Diff B\"";
	public static final int hcount = 9;

	public int pump = -1;
	public String pumpAction= "";
	//The following are always present 
	public char tankUpdate;
	public int tankA;
	public int tankB;
	public int tankC;
	public int tankD;
	public int diffA;
	public int diffB;

	public RMANEvent(){}

	public RMANEvent(String line) throws ParseException{
		parse(line);
	}

	@Override
	public DateTime parse(String line) throws ParseException {

		if (line.isEmpty() || line.charAt(0) == '#')
			throw new ParseException("Invalid line: '" + line + "'", 0);

		String[] parts = line.split(del);

		time = readDate(parts[0]);

		if (parts.length >= 8){

			int pos = 1;

			if (parts.length == 10){
				pump = Integer.parseInt(parts[1]);
				pumpAction = parts[2];
				pos = 3;
			}

			tankUpdate = parts[pos++].charAt(0);
			tankA = Integer.parseInt(parts[pos++]);
			tankB = Integer.parseInt(parts[pos++]);
			tankC = Integer.parseInt(parts[pos++]);
			tankD = Integer.parseInt(parts[pos++]);
			diffA = Integer.parseInt(parts[pos++]);
			diffB = Integer.parseInt(parts[pos++]);

		} else {

			throw new ParseException("Could not parse line: '" + line + "'", 11);

		}

		return time;
	}

	@Override
	public String toString() {

		String ret;

		ret  = "\"" + ((pump != -1)?pump:"") + "\",";
		ret += "\"" + pumpAction + "\",";
		ret += "\"" + tankUpdate + "\",";
		ret += tankA + "," + tankB + "," + tankC +"," + tankD + "," + diffA + "," + diffB; 

		return ret;
	}

}
