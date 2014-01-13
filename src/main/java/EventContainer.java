import org.joda.time.DateTime;


public class EventContainer implements Comparable<EventContainer>{

	public DateTime time;
	public MATBEvent matb;
	public COMMEvent comm;
	public RMANEvent rman;
	public SYSMEvent sysm;
	public TRCKEvent trck;
	public WRSEvent wrs;
	
	public EventContainer(DateTime time){
		this.time = time;
	}

	public static String getHeader(){
		return "\"Time\"," + MATBEvent.header + ',' + COMMEvent.header + ',' + RMANEvent.header + 
				',' + SYSMEvent.header + ',' + TRCKEvent.header + ',' + WRSEvent.header;
	}

	/**
	 * Prints out formated csv of all events with extended specifications.
	 * 
	 * @return
	 */
	@Override
	public String toString(){

		String ret = "\"" + ReaderInterface.printDate(time) + "\"";
		
		if (matb != null){		
			ret += ',' + matb.toString();
		} else {
			ret += ',' + emptyCSVGen(MATBEvent.hcount);
		}

		if (comm != null){
			ret += ',' + comm.toString();
		} else {
			ret += ',' + emptyCSVGen(COMMEvent.hcount);
		}

		if (rman != null){
			ret += ',' + rman.toString();
		} else {
			ret += ',' + emptyCSVGen(RMANEvent.hcount);
		}

		if (sysm != null){
			ret += ',' + sysm.toString();
		} else {
			ret += ',' + emptyCSVGen(SYSMEvent.hcount);
		}

		if (trck != null){
			ret += ',' + trck.toString();
		} else {
			ret += ',' + emptyCSVGen(TRCKEvent.hcount);
		}
		
		if (wrs != null){
			ret += ',' + wrs.toString();
		} else {
			ret += ',' + emptyCSVGen(WRSEvent.hcount);
		}

		return ret;

	}

	private static String emptyCSVGen(int count){

		String ret = "";

		for (int i = 1; i < count; ++i){
			ret += ",";
		}

		return ret;

	}

	@Override
	public int compareTo(EventContainer e) {
		return time.compareTo(e.time);
	}
	
	public boolean equals(DateTime d){
		return d.equals(time);
	}
	
	public boolean equals(EventContainer e){
		return e.equals(time);
	}
	
}
