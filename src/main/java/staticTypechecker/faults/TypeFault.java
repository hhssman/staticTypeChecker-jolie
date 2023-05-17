package staticTypechecker.faults;

import jolie.lang.parse.context.ParsingContext;

public class TypeFault implements Fault {
	private String message;

	public TypeFault(String message, ParsingContext ctx){
		this.message = FaultHandler.getFaultContextMessage(ctx) + message;
	}
	
	public String getMessage(){
		return message;
	}
}
