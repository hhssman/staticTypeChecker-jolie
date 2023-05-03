package staticTypechecker.faults;

public class MiscFault implements Fault {
	private String faultMessage;

	public MiscFault(String faultMessage){
		this.faultMessage = faultMessage;
	}

	public String getMessage(){
		return this.faultMessage;
	}
}
