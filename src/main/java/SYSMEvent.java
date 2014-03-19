import java.text.ParseException;

import org.joda.time.LocalTime;


public class SYSMEvent extends ReaderInterface{

	public static final String header = "\"RT\",\"System\",\"Light/Scale\",\"Sys Ok\",\"Remarks\"";
	public static final int hcount = 5;

	public enum System{
		Light,
		Scale
	}

	public float rt = Float.NaN;
	public System system;
	public String ls;
	public Boolean sysok;
	public String remarks = "";

	public SYSMEvent(){}

	public SYSMEvent(String line) throws ParseException{
		parse(line);
	}

	@Override
	public LocalTime parse(String line) throws ParseException {

		if (line.isEmpty() || line.charAt(0) == '#')
			throw new ParseException("Invalid line: '" + line + "'", 0);

		String[] parts = line.split(del);

		time = readDate(parts[0]);

		for (int i = 1; i < parts.length; ++i){

			String part = parts[i];

			if (part.matches("^-?[0-9]+(\\.[0-9])?$")){
				rt = Float.parseFloat(part);
			} else if (part.matches("^(Light|Scale)$")){
				if (part == "Light"){
					system = System.Light;
				} else {
					system = System.Scale;
				}
			} else if (part.matches("(TRUE|FALSE)")){
				sysok = Boolean.parseBoolean(part);
			} else if (part.startsWith("-")){
				remarks = part.replaceAll(ccleaner, "");
			} else {
				ls = part.replaceAll(ccleaner, "");
			}

		}

		return time;
	}

	@Override
	public String toString() {

		String ret = "\"" + (Float.isNaN(rt)?rt:"") + "\",\"" + (system==System.Light?"Light":"Scale") +
				"\",\"" + ls + "\",\"" + (sysok!=null?sysok:"") + "\",\"" + remarks + "\"";

		return ret;
	}

}
