package staticTypechecker.entities;

import java.util.Map.Entry;

import java.util.Collection;
import java.util.HashMap;

/**
 * Represents a service in Jolie
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class Service implements Symbol {
	private String name; 								// the name of the service
	private HashMap<String, InputPort> inputPorts; 		// map names to input ports
	private HashMap<String, OutputPort> outputPorts; 	// map names to output ports
	private Type parameter; 							// service parameter

	public Service(){
		this.name = null;
		this.inputPorts = new HashMap<>();
		this.outputPorts = new HashMap<>();
		this.parameter = null;
	}

	public String name(){
		return this.name;
	}

	public void setName(String name){
		this.name = name;
	}

	public InputPort getInputPort(String name){
		return this.inputPorts.get(name);
	}

	public Operation getOperation(String name){
		for(InputPort ip : this.inputPorts.values()){
			if(ip.containsOperation(name)){
				return ip.getOperation(name);
			}
		}
		return null;
	}

	public OutputPort getOutputPort(String name){
		return this.outputPorts.get(name);
	}

	public void addInputPort(String name, InputPort port){
		this.inputPorts.put(name, port);
	}

	public void addOutputPort(String name, OutputPort port){
		this.outputPorts.put(name, port);
	}

	public Collection<Entry<String, InputPort>> inputPorts(){
		return this.inputPorts.entrySet();
	}

	public Type parameter(){
		return this.parameter;
	}

	public void setParameter(Type parameter){
		this.parameter = parameter;
	}

	public String prettyString(){
		return this.prettyString(0);
	}

	public String prettyString(int level){
		String ret = this.name;

		if(this.parameter != null){
			String param = this.parameter.prettyString(level+1);
			if(param.contains("\n")){
				ret += "(\n" + "\t".repeat(level+1) + param + "\n" + "\t".repeat(level) + ")";
			}
			else{
				ret += "(" + param + ")";
			}
		}

		if(!this.inputPorts.isEmpty()){
			ret += "\n" + "\t".repeat(level+1) + "InputPorts:";
			for(Entry<String, InputPort> ent : this.inputPorts.entrySet()){
				ret += "\n" + ent.getValue().prettyString(level+2);
			}
		}

		if(!this.inputPorts.isEmpty()){
			ret += "\n" + "\t".repeat(level+1) + "OutputPorts:";
			for(Entry<String, OutputPort> ent : this.outputPorts.entrySet()){
				ret += "\n" + ent.getValue().prettyString(level+2);
			}
		}

		return ret;
	}
}
