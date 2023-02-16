package staticTypechecker.faults;

public class FaultHandler {
	public static void throwFault(String faultMessage){
		System.out.println("-----------------------------------");
		System.out.println("Critical error: " + faultMessage);
		System.exit(1);
	}
}
