package staticTypechecker.faults;

import jolie.lang.parse.context.ParsingContext;
import staticTypechecker.entities.Service;

public class NoServiceParameterFault implements Fault {
	private Service service;
	private ParsingContext ctx;
	
	public NoServiceParameterFault(Service service, ParsingContext ctx){
		this.service = service;
		this.ctx = ctx;
	}

	public String getMessage(){
		return FaultHandler.getFaultContextMessage(this.ctx) + "no parameter passed to the service \"" + this.service.name() + "\". Expected type:\n" + this.service.parameter().prettyString();
	}
}
