import java.text.ParseException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class TRCKEvent extends ReaderInterface{

	public static final String header = "\"Session Time\",\"Num\",\"This Interval Sum of Squares\",\"RMSD\",\"Num\"" +
			",\"Session Aggregate Sum of Squares\",\"RMSD\",\"Num\",\"Run Aggregate Sum of Squares\",\"RMSD\",\"Remarks\"";

	public static final int hcount = 11;

	private static final DateTimeFormatter stf = DateTimeFormat.forPattern("mm:ss");

	public DateTime st;
	public int num;
	public int tisos;
	public float rmsd;
	public int num2;
	public int sasos;
	public float rmsd2;
	public int num3;
	public int rasos;
	public float rmsd3;
	public String remarks = "";

	public TRCKEvent(){}

	public TRCKEvent(String line) throws ParseException{
		parse(line);
	}

	@Override
	public DateTime parse(String line) throws ParseException {

		if (line.isEmpty() || line.charAt(0) == '#')
			throw new ParseException("Invalid line: '" + line + "'", 0);

		String[] parts = line.trim().split("\\s+");

		time = readDate(parts[0]);

		if (parts.length >= 11){
			st = stf.parseDateTime(parts[1]);
			num = readInt(parts[2]);
			tisos = readInt(parts[3]);
			rmsd = Float.parseFloat(parts[4]);
			num2 = readInt(parts[5]);
			sasos = readInt(parts[6]);
			rmsd2 = Float.parseFloat(parts[7]);
			num3 = readInt(parts[8]);
			rasos = readInt(parts[9]);
			rmsd3 = Float.parseFloat(parts[10]);

			if (parts.length >= 12){
				remarks = parts[11].replaceAll(ccleaner, "");
			}

		} else {

			throw new ParseException("Could not parse line: '" + line + "' Unexpected Number of Parts",  11);

		}

		return time;
	}

	@Override
	public String toString() {

		String ret = "\"" + stf.print(st) + "\",";
		ret += num + "," + tisos + ",\"" + rmsd + "\"," + num2 + "," + sasos + ",\"" + rmsd2 + "\"," +
				num3 + "," + rasos + ",\"" + rmsd3 + "\",\"" + remarks + "\"";

		return ret;

	}

	private int readInt(String s){
		s = s.replaceAll(",", "");
		return Integer.parseInt(s);
	}

}
