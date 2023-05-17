package staticTypechecker.faults;

import jolie.lang.parse.context.ParsingContext;

public class UnknownFunctionFault implements Fault {
	private String message;

	public UnknownFunctionFault(String message, ParsingContext ctx){
		this.message = FaultHandler.getFaultContextMessage(ctx) + message;
	}
	
	public String getMessage(){
		return this.message;
	}
}
