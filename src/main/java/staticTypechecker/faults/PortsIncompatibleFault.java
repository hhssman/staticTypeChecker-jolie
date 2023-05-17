package staticTypechecker.faults;

import java.util.List;

import jolie.lang.parse.context.ParsingContext;
import staticTypechecker.entities.Operation;
import staticTypechecker.entities.OutputPort;
import staticTypechecker.entities.Service;

public class PortsIncompatibleFault implements Fault {
	private OutputPort host;
	private Service embeddedService;
	private List<Operation> unprovidedOperations;
	private ParsingContext ctx;

	public PortsIncompatibleFault(OutputPort host, Service embeddedService, List<Operation> unprovidedOperations, ParsingContext ctx){
		this.host = host;
		this.embeddedService = embeddedService;
		this.unprovidedOperations = unprovidedOperations;
		this.ctx = ctx;
	}

	public String getMessage(){
		String res = FaultHandler.getFaultContextMessage(this.ctx) + "service '" + this.embeddedService.name() + "' does not provide operations:\n";
		
		for(Operation op : this.unprovidedOperations){
			res += "\t" + op.prettyString() + "\n";
		}
		res +=  "required by port '" + this.host.name() + "'";

		return res;
	}
}
