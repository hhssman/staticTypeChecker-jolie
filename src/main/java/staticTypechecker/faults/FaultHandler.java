package staticTypechecker.faults;

import java.util.HashSet;

import jolie.lang.parse.context.ParsingContext;

/**
 * Handles all faults which may occur during the type checking
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class FaultHandler {
	private static HashSet<Fault> faults = new HashSet<>();

	public static void throwFault(Fault fault, boolean terminate){
		
		if(!FaultHandler.faults.contains(fault)){
			FaultHandler.faults.add(fault);
		}

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
