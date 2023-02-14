package staticTypechecker.faults;

public class Warning {
	private String message;

	public Warning(String message){
		this.message = "WARNING: " + message;
	}

	public String message(){
		return this.message;
	}

	public String toString(){
		return this.message;
	}
}
