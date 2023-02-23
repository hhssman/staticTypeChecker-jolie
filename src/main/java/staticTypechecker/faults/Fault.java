package staticTypechecker.faults;

public class Fault {
	private String message;

	public Fault(String message){
		this.message = message;
	}

	public String message(){
		return this.message;
	}
}
