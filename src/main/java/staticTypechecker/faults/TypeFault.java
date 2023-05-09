package staticTypechecker.faults;


public class TypeFault implements Fault {
	private String message;

	public TypeFault(String message){
		this.message = message;
	}
	
	public String getMessage(){
		return message;
	}
}
