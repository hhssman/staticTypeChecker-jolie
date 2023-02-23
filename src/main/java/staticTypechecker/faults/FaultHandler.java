package staticTypechecker.faults;

import java.util.ArrayList;

public class FaultHandler {
	private static ArrayList<Fault> faults = new ArrayList<>();

	public static void throwFault(String faultMessage){
		FaultHandler.faults.add(new Fault(faultMessage));
	}

	public static void printFaults(){
		for(Fault f : FaultHandler.faults){
			System.out.println("Critical error: " + f.message());
		}
	}
}
