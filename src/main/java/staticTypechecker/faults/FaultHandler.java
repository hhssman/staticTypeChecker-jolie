package staticTypechecker.faults;

import java.util.ArrayList;

import jolie.lang.parse.context.ParsingContext;

public class FaultHandler {
	private static ArrayList<Fault> faults = new ArrayList<>();

	public static void throwFault(String faultMessage, ParsingContext ctx){
		String message = "Critical error in file '" + ctx.sourceName() + "' on line " + ctx.line() + ":\n" + faultMessage;
		FaultHandler.faults.add(new Fault(message));
	}

	public static void printFaults(){
		for(Fault f : FaultHandler.faults){
			System.out.println(f.message());
			System.out.println();
		}
	}
}
