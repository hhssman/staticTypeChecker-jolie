package staticTypechecker.entities;

import java.util.Map.Entry;
import java.util.HashMap;

public class Service implements Symbol {
	private String name; // the name of the service
	private HashMap<String, InputPort> inputPorts; // map names to input ports
	private HashMap<String, OutputPort> outputPorts; // map names to output ports
	private String parameter; // service parameter

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

	public OutputPort getOutputPort(String name){
		return this.outputPorts.get(name);
	}

	public void addInputPort(String name, InputPort port){
		this.inputPorts.put(name, port);
	}

	public void addOutputPort(String name, OutputPort port){
		this.outputPorts.put(name, port);
	}

	public String parameter(){
		return this.parameter;
	}

	public void setParameter(String parameter){
		this.parameter = parameter;
	}

	public static Service getBaseService(){
		return new Service();
	}

	public String prettyString(){
		String ret = this.name;

		ret += "\n\tInputPorts:";
		for(Entry<String, InputPort> ent : this.inputPorts.entrySet()){
			ret += "\n\t\t" + ent.getKey();
		}

		ret += "\n\tOutputPorts:";
		for(Entry<String, OutputPort> ent : this.outputPorts.entrySet()){
			ret += "\n\t\t" + ent.getKey();
		}

		return ret;
	}
}
