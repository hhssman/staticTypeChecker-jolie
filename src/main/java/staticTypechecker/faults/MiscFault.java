package staticTypechecker.faults;

import jolie.lang.parse.context.ParsingContext;

public class MiscFault implements Fault {
	private String faultMessage;

	public MiscFault(String faultMessage, ParsingContext ctx){
		this.faultMessage = FaultHandler.getFaultContextMessage(ctx) + faultMessage;
	}

	public String getMessage(){
		return this.faultMessage;
	}
}
