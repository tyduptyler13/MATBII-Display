import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class WRSEvent extends ReaderInterface{

	public static final String header = "\"Time\",\"MENL\",\"Phys\",\"Temp\",\"Perf\",\"EFFT\",\"FRUS\",\"Mean\",\"Remarks\"";
	public static final int hcount = 10;

	public static final SimpleDateFormat stf = new SimpleDateFormat("mm:ss.S");

	public Date time;
	public int menl;
	public int phys;
	public int temp;
	public int perf;
	public int efft;
	public int frus;
	public float mean;
	public String remarks = "";

	@Override
	public Date parse(String line) throws ParseException {

		if (line.isEmpty() || line.charAt(0) == '#')
			throw new ParseException("Invalid line: '" + line + "'", 0);

		String[] parts = line.split(del);

		time = sdf.parse(parts[0]);

		if (parts.length >= 9){

			time = stf.parse(parts[1]);
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

		String ret = stf.format(time) + ',' + menl + ',' + phys + ',' + perf + ',' +
				efft + ',' + frus + ",\"" + mean + "\",\"" + remarks + "\"";

		return ret;
	}

}
