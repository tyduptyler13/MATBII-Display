import java.text.ParseException;

import org.joda.time.DateTime;



public class COMMEvent extends ReaderInterface{

	public static final String header = "\"RT\",\"Ship\",\"Radio_T\",\"Freq_T\","
			+ "\"Radio_S\",\"Freq_S\",\"R_Ok\",\"F_Ok\",\"Remarks\"";
	public static final int hcount = 9;

	public float rt = Float.NaN;
	public String ship = "";
	public String radiot = "";
	public String freqt = "";
	public String radios = "";
	public String freqs = "";
	public boolean rok;
	public boolean fok;
	public String remarks = "";

	public COMMEvent(){}

	public COMMEvent(String line) throws ParseException {
		parse(line);
	}

	@Override
	public DateTime parse(String line) throws ParseException {

		if (line.isEmpty() || line.charAt(0) == '#')
			throw new ParseException("Invalid line: '" + line + "'", 0);

		String[] parts = line.split(del);

		time = readDate(parts[0]);

		if (parts.length == 2){//Only a comment and time.
			remarks = parts[1].replaceAll(ccleaner, "");
		} else if (parts.length > 2){

			rt = Float.parseFloat(parts[1]);
			ship = parts[2];
			radiot = parts[3];
			freqt = parts[4];
			radios = parts[5];
			freqs = parts[6];
			rok = Boolean.parseBoolean(parts[7]);
			fok = Boolean.parseBoolean(parts[8]);

			if (parts.length >= 10){
				remarks = parts[9].replaceAll(ccleaner, "");
			}

		} else {
			throw new ParseException("Unexpected COMM format!", 11);
		}

		return time;

	}

	@Override
	public String toString(){

		if (Float.isNaN(rt)){
			return ",,,,,,,\"" + remarks + "\"";
		}

		String ret = rt + "," + ship + "," + radiot + "," +
				freqt + "," + radios + "," + freqs + "," + rok +
				"," + fok + ",\"" + remarks + "\"";

		return ret;

	}

}
