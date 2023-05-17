package staticTypechecker.faults;

/**
 * Simple fault containing a fault message
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public interface Fault {
	public String getMessage();

	public static boolean equals(Fault f1, Fault f2){
		if(f1 == f2){
			return true;
		}

		if(f1 == null || f2 == null){ // they cant both be null, since we already checked if their pointers are the same
			return false;
		}

		if(!f1.getClass().equals(f2.getClass())){
			return false;
		}
		
		return f1.getMessage().equals(f2.getMessage());
	}
}
