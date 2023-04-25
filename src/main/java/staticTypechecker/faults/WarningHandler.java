package staticTypechecker.faults;

import java.util.ArrayList;

import jolie.lang.parse.context.ParsingContext;

/**
 * Handles all warnings which may occur during the type checking
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class WarningHandler {
	private static ArrayList<Warning> warnings = new ArrayList<>();

	public static void throwWarning(Warning warning){
		WarningHandler.warnings.add(warning);
	}

	public static void throwWarning(String warningMessage, ParsingContext ctx){
		if(ctx != null){
			String message = "WARNING in file '" + ctx.sourceName() + "' on line " + ctx.line() + ":\n" + warningMessage;
			WarningHandler.warnings.add(new Warning(message));
		}
		else{
			WarningHandler.warnings.add(new Warning("WARNING: " + warningMessage));
		}
	}

	public static ArrayList<Warning> warnings(){
		return new ArrayList<Warning>(WarningHandler.warnings);
	}

	public static boolean isEmpty(){
		return WarningHandler.warnings.isEmpty();
	}

	public static void printWarnings(){
		for(Warning w : WarningHandler.warnings){
			System.out.println(w.message());
		}		
	}
}
