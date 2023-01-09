package staticTypechecker.entities;

import java.util.HashMap;
import java.util.Map.Entry;

import staticTypechecker.entities.Operation.OperationType;

public class Interface implements Symbol {
	private String name; // the name of the interface
	private HashMap<String, Operation> operations; // maps operation names to their object

	public Interface(String name){
		this.name = name;
		this.operations = new HashMap<>();
	}

	public void addOperation(String opName, Operation op){
		this.operations.put(opName, op);
	}

	public Operation getOperation(String name){
		return this.operations.get(name);
	}

	public String name(){
		return this.name;
	}

	@Override
	public String prettyString(){
		String ret = this.name + "\n";

		ret += "\tOneWay:\n";
		for(Entry<String, Operation> ent : this.operations.entrySet()){
			if(ent.getValue().type() == OperationType.ONEWAY){
				ret += "\t\t" + ent.getValue().prettyString();
			}
		}
		
		ret += "\n\n\tRequestResponse:\n";
		for(Entry<String, Operation> ent : this.operations.entrySet()){
			if(ent.getValue().type() == OperationType.REQRES){
				ret += "\t\t" + ent.getValue().prettyString();
			}
		}

		return ret;
	}
}