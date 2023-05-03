package staticTypechecker.faults;

import java.util.ArrayList;

import jolie.lang.parse.context.ParsingContext;

/**
 * Handles all faults which may occur during the type checking
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class FaultHandler {
	private static ArrayList<String> faults = new ArrayList<>();

	public static void throwFault(Fault fault, ParsingContext ctx, boolean terminate){
		String message = "Critical error in file '" + ctx.sourceName() + "' on line " + ctx.line() + ":\n" + fault.getMessage();
		FaultHandler.faults.add(message);

		if(terminate){
			FaultHandler.printFaults();
			System.exit(0);
		}
	}

	public static boolean isEmpty(){
		return FaultHandler.faults.isEmpty();
	}

	public static void printFaults(){
		for(String f : FaultHandler.faults){
			System.out.println(f);
			System.out.println();
		}
	}
}
