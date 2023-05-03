package staticTypechecker.faults;

import java.util.List;

import staticTypechecker.entities.Operation;
import staticTypechecker.entities.OutputPort;
import staticTypechecker.entities.Service;

public class PortsIncompatibleFault implements Fault {
	private OutputPort host;
	private Service embeddedService;
	private List<Operation> unprovidedOperations;

	public PortsIncompatibleFault(OutputPort host, Service embeddedService, List<Operation> unprovidedOperations){
		this.host = host;
		this.embeddedService = embeddedService;
		this.unprovidedOperations = unprovidedOperations;
	}

	public String getMessage(){
		String res = "service '" + this.embeddedService.name() + "' does not provide operations:\n";
		
		for(Operation op : this.unprovidedOperations){
			res += "\t" + op.prettyString() + "\n";
		}
		res +=  "required by port '" + this.host.name() + "'";

		return res;
	}
}
