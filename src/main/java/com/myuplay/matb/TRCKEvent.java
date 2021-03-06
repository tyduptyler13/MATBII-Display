package com.myuplay.matb;

import java.text.ParseException;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TRCKEvent extends ReaderInterface {

	public static final String header = "\"Session Time\",\"Num\",\"This Interval Sum of Squares\",\"RMSD\",\"Num\""
			+ ",\"Session Aggregate Sum of Squares\",\"RMSD\",\"Num\",\"Run Aggregate Sum of Squares\",\"RMSD\",\"Compass\""
			+ ",\"X\",\"Y\",\"Remarks\"";

	public static final int hcount = 14;

	private static final DateTimeFormatter stf = DateTimeFormat
			.forPattern("mm:ss");

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
	public String compass;
	public double x;
	public double y;
	public String remarks = "";

	public TRCKEvent() {
	}

	public TRCKEvent(String line) throws ParseException {
		parse(line);
	}

	@Override
	public LocalTime parse(String line) throws ParseException {

		if (line.isEmpty() || line.charAt(0) == '#')
			throw new ParseException("Invalid line: '" + line + "'", 0);

		String[] parts = line.trim().split("\\s+");

		time = readDate(parts[0]);

		if (parts.length >= 14) {
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
			compass = parts[11];
			x = Double.parseDouble(parts[12]);
			y = Double.parseDouble(parts[13]);

			if (parts.length >= 15) {
				// Collect all parts beyond the end, in case of spaces.
				for (int x = 14; x < parts.length; ++x) {
					remarks += parts[x].replaceAll(ccleaner, "") + " ";
				}

				remarks.trim();// Clean superfluous spaces.

			}

		} else {

			throw new ParseException("Could not parse line: '" + line
					+ "' Unexpected Number of Parts", 14);

		}

		return time;
	}

	@Override
	public String toString() {

		String ret = "\"" + stf.print(st) + "\",";
		ret += num + "," + tisos + ",\"" + rmsd + "\"," + num2 + "," + sasos
				+ ",\"" + rmsd2 + "\"," + num3 + "," + rasos + ",\"" + rmsd3
				+ "\",\"" + compass + "\",\"" + x + "\",\"" + y + "\",\""
				+ remarks + "\"";

		return ret;

	}

	private int readInt(String s) {
		s = s.replaceAll(",", "");
		return Integer.parseInt(s);
	}
	
	/**
	 * Shallow compare.
	 * @param e
	 * @return
	 */
	public boolean equals(TRCKEvent e){
		return st.equals(e.st) && num == e.num;
	}

}
