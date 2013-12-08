import java.util.ArrayList;

public class Console{
	
	/**
	 * Adds standard out to the list of outputs.
	 */
	public static void setup(){
		outputs.add(new Console.stdout());
	}

	public static String name = "MATBII-Display";
	public static ArrayList<PrintInterface> outputs = new ArrayList<PrintInterface>();

	public static void print(String message){
		printToAll("["+name+"] "+message);
	}
	
	public static void error(String message){
		print(message, "ERROR");
	}
	
	public static void log(String message){
		print(message, "log");
	}
	
	/**
	 * Customized printing for output.
	 * <p>Format:
	 * <code>[SomeProgram][channel] message.</code>
	 * </p>
	 * @param message Message after formated output.
	 * @param channel The channel is a formated section before the message like "error" or "log".
	 */
	public static void print(String message, String channel){
		printToAll("["+name+"]["+channel+"] " + message);
	}
	
	protected static void printToAll(String s){
		for (PrintInterface i : outputs){
			i.print(s);
		}
	}
	
	/**
	 * The standard output for the interface.
	 * @author Tyler
	 *
	 */
	protected static class stdout implements PrintInterface{
		public void print(String s) {
			System.out.println(s);
		}
	}
	
}
