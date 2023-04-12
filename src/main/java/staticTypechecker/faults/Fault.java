package staticTypechecker.faults;

/**
 * Simple fault containing a fault message
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class Fault {
	private String message;

	public Fault(String message){
		this.message = message;
	}

	public String message(){
		return this.message;
	}
}
