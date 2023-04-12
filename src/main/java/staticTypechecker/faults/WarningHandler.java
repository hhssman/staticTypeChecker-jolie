package staticTypechecker.faults;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Handles all warnings which may occur during the type checking
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class WarningHandler {
	private static ArrayList<Warning> warnings = new ArrayList<>();

	public static void addWarning(Warning warning){
		WarningHandler.warnings.add(warning);
	}

	public static ArrayList<Warning> warnings(){
		return new ArrayList<Warning>(WarningHandler.warnings);
	}

	public static boolean isEmpty(){
		return WarningHandler.warnings.isEmpty();
	}

	public static String prettyString(){
		if(WarningHandler.warnings.isEmpty()){
			return "";
		}

		return WarningHandler.warnings.stream().map(w -> w.toString()).collect(Collectors.joining("\n"));
	}

}
