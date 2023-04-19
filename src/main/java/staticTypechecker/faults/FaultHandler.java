package staticTypechecker.faults;

import java.util.ArrayList;

import jolie.lang.parse.context.ParsingContext;

/**
 * Handles all faults which may occur during the type checking
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class FaultHandler {
	private static ArrayList<Fault> faults = new ArrayList<>();

	public static void throwFault(String faultMessage, ParsingContext ctx){
		FaultHandler.throwFault(faultMessage, ctx, false);
	}
	
	public static void throwFault(String faultMessage, ParsingContext ctx, boolean terminate){
		String message = "Critical error in file '" + ctx.sourceName() + "' on line " + ctx.line() + ":\n" + faultMessage;
		FaultHandler.faults.add(new Fault(message));

		if(terminate){
			FaultHandler.printFaults();
			System.exit(0);
		}
	}

	public static void throwFault(String faultMessage){
		FaultHandler.throwFault(faultMessage, false);
	}

	public static void throwFault(String faultMessage, boolean terminate){
		String message = "Critical error: " + faultMessage;
		FaultHandler.faults.add(new Fault(message));

		if(terminate){
			FaultHandler.printFaults();
			System.exit(0);
		}
	}

	public static void printFaults(){
		for(Fault f : FaultHandler.faults){
			System.out.println(f.message());
			System.out.println();
		}
	}
}
