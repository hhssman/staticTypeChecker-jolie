package staticTypechecker.faults;

import staticTypechecker.entities.Service;

public class NoServiceParameterFault implements Fault {
	private Service service;
	
	public NoServiceParameterFault(Service service){
		this.service = service;
	}

	public String getMessage(){
		return "no parameter passed to the service \"" + this.service.name() + "\". Expected type:\n" + this.service.parameter().prettyString();
	}
}
