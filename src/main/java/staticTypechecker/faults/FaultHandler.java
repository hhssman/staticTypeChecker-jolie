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

	public static void throwFault(Fault fault, boolean terminate){
		
		FaultHandler.faults.add(fault);

		if(terminate){
			FaultHandler.printFaults();
			System.exit(0);
		}
	}

	public static boolean isEmpty(){
		return FaultHandler.faults.isEmpty();
	}

	public static boolean contains(Fault fault){
		for(Fault f : FaultHandler.faults){
			if(Fault.equals(f, fault)){
				return true;
			}
		}
		return false;
	}

	public static void printFaults(){
		for(Fault f : FaultHandler.faults){
			System.out.println(f.getMessage());
			System.out.println();
		}
	}

	public static String getFaultContextMessage(ParsingContext ctx){
		if(ctx == null){
			return "";
		}
		
		return "Critical error in file '" + ctx.sourceName() + "' on line " + ctx.line() + ":\n";
	}
}
