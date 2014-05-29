package com.myuplay.matb;

import org.joda.time.LocalTime;

public class EventContainer implements Comparable<EventContainer> {

	public LocalTime time;
	public MATBEvent matb;
	public COMMEvent comm;
	public RMANEvent rman;
	public SYSMEvent sysm;
	public TRCKEvent trck;
	public WRSEvent wrs;

	public EventContainer(LocalTime time) {
		this.time = time;
	}

	public static String getHeader() {
		return "\"Time\"," + MATBEvent.header + ',' + COMMEvent.header + ','
				+ RMANEvent.header + ',' + SYSMEvent.header + ','
				+ TRCKEvent.header + ',' + WRSEvent.header;
	}

	/**
	 * Prints out formated csv of all events with extended specifications.
	 * 
	 * @return
	 */
	@Override
	public String toString() {

		String ret = "\"" + ReaderInterface.printDate(time) + "\"";

		if (matb != null) {
			ret += ',' + matb.toString();
		} else {
			ret += ',' + emptyCSVGen(MATBEvent.hcount);
		}

		if (comm != null) {
			ret += ',' + comm.toString();
		} else {
			ret += ',' + emptyCSVGen(COMMEvent.hcount);
		}

		if (rman != null) {
			ret += ',' + rman.toString();
		} else {
			ret += ',' + emptyCSVGen(RMANEvent.hcount);
		}

		if (sysm != null) {
			ret += ',' + sysm.toString();
		} else {
			ret += ',' + emptyCSVGen(SYSMEvent.hcount);
		}

		if (trck != null) {
			ret += ',' + trck.toString();
		} else {
			ret += ',' + emptyCSVGen(TRCKEvent.hcount);
		}

		if (wrs != null) {
			ret += ',' + wrs.toString();
		} else {
			ret += ',' + emptyCSVGen(WRSEvent.hcount);
		}

		return ret;

	}

	private static String emptyCSVGen(int count) {

		String ret = "";

		for (int i = 1; i < count; ++i) {
			ret += ",";
		}

		return ret;

	}

	@Override
	public int compareTo(EventContainer e) {
		return time.compareTo(e.time);
	}

	public boolean equals(LocalTime d) {
		return d.equals(time);
	}

	public boolean equals(EventContainer e) {
		return e.equals(time);
	}
	
	public boolean equals(TRCKEvent e){
		if (hasTRCK()){
			return e.equals(trck);
		} else {
			return false;
		}
	}

	/*
	 * Checks the matb against the one passed in.
	 */
	public boolean equals(MATBEvent e){
		if (hasMATB()){
			return e.equals(matb);
		} else {
			return false;
		}
	}
	
	/**
	 * Checks the event type in matb against the string.
	 * @param s
	 * @return
	 */
	public boolean equals(String s){
		if (hasMATB()){
			return s.equals(matb.event);
		} else {
			return false;
		}
	}
	
	public boolean hasMATB() {
		return matb != null;
	}

	public boolean hasCOMM() {
		return comm != null;
	}

	public boolean hasRMAN() {
		return rman != null;
	}

	public boolean hasSYSM() {
		return sysm != null;
	}

	public boolean hasTRCK() {
		return trck != null;
	}

	public boolean hasWRS() {
		return wrs != null;
	}

}
