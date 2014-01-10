import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class WRSEvent extends ReaderInterface{

	public static final String header = "\"Time\",\"MENL\",\"Phys\",\"Temp\",\"Perf\",\"EFFT\",\"FRUS\",\"Mean\",\"Remarks\"";
	public static final int hcount = 10;

	public static final DateTimeFormatter stf = DateTimeFormat.forPattern("mm:ss.S");

	public DateTime time2;
	public int menl;
	public int phys;
	public int temp;
	public int perf;
	public int efft;
	public int frus;
	public float mean;
	public String remarks = "";

	@Override
	public DateTime parse(String line) throws ParseException {

		if (line.isEmpty() || line.charAt(0) == '#')
			throw new ParseException("Invalid line: '" + line + "'", 0);

		String[] parts = line.trim().split("\\s+");

		time = readDate(parts[0]);

		if (parts.length >= 9){

			time2 = stf.parseDateTime(parts[1]);
			menl = Integer.parseInt(parts[2]);
			phys = Integer.parseInt(parts[3]);
			temp = Integer.parseInt(parts[4]);
			perf = Integer.parseInt(parts[5]);
			efft = Integer.parseInt(parts[6]);
			frus = Integer.parseInt(parts[7]);
			mean = Float.parseFloat(parts[8]);

			if (parts.length >= 10){

				remarks = parts[9].replaceAll(ccleaner, "");

			}

		}

		return time;
	}

	@Override
	public String toString() {

		String ret = stf.print(time) + ',' + menl + ',' + phys + ',' + perf + ',' +
				efft + ',' + frus + ",\"" + mean + "\",\"" + remarks + "\"";

		return ret;
	}

}
