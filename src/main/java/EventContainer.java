
public class EventContainer{

	public MATBEvent matb;
	public COMMEvent comm;
	public RMANEvent rman;
	public SYSMEvent sysm;
	public TRCKEvent trck;
	public WRSEvent wrs;
	
	public static String getHeader(){
		return MATBEvent.header + ',' + COMMEvent.header + ',' + RMANEvent.header;
	}
	
	/**
	 * Prints out formated csv of all events with extended specifications.
	 * 
	 * @return
	 */
	public String toString(){
		
		String ret = matb.toString();
		
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
		
		return ret;
		
	}
	
	private static String emptyCSVGen(int count){
		
		String ret = "\"\"";
		
		for (int i = 1; i < count; ++i){
			ret += ",\"\"";
		}
		
		return ret;
		
	}
	
}
