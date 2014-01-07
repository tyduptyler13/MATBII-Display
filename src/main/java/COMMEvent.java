import java.text.ParseException;
import java.util.Date;


public class COMMEvent extends ReaderInterface{

	public static final String header = "\"RT\",\"Ship\",\"Radio_T\",\"Freq_S\",\"R_Ok\",\"F_Ok\",\"Remarks\"";
	public static final int hcount = 7;

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

	public Date parse(String line) throws ParseException {

		if (line.isEmpty() || line.charAt(0) == '#')
			throw new ParseException("Invalid line: '" + line + "'", 0);

		String[] parts = line.split(del);

		time = sdf.parse(parts[0]);

		if (parts.length == 2){//Only a commend and time.
			remarks = parts[1];
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

	public String toString(){
		
		if (Float.isNaN(rt)){
			return "\"\",\"\",\"\",\"\",\"\",\"\",\"" + remarks + "\"";
		}

		String ret = "\"" + rt + "\",\"" + ship + "\",\"" + radiot + "\",\"" +
				freqt + "\",\"" + radios + "\",\"" + freqs + "\",\"" + rok +
				"\",\"" + fok + "\",\"" + remarks + "\"";

		return ret;

	}

}
