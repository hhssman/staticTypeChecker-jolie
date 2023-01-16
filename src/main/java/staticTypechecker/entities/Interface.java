package staticTypechecker.entities;

import java.util.ArrayList;
import java.util.Collection;
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

	public Collection<Entry<String, Operation>> operations(){
		return this.operations.entrySet();
	}

	public String name(){
		return this.name;
	}

	public void setName(String name){
		this.name = name;
	}

	public static Interface getBaseInterface(){
		return new Interface(null);
	}

	@Override
	public String prettyString(){
		if(this.name == null){
			return "null";
		}

		String ret = this.name;

		ArrayList<Operation> oneWay = new ArrayList<>();
		ArrayList<Operation> reqRes = new ArrayList<>();

		// sort the operations
		for(Entry<String, Operation> ent : this.operations.entrySet()){
			Operation op = ent.getValue();
			
			if(op.type() == OperationType.ONEWAY){ // of type one way
				oneWay.add(op);
			}
			else{ // of type req res
				reqRes.add(op);
			}
		}
		
		// add oneway operations if any
		ret += "\n\tOneWay:";
		if(!oneWay.isEmpty()){
			for(Operation op : oneWay){
				ret += "\n\t\t" + op.prettyString();
			}
		}

		// add req res operations if any
		ret += "\n\tRequestResponse:";
		if(!reqRes.isEmpty()){
			for(Operation op : reqRes){
				ret += "\n\t\t" + op.prettyString();
			}
		}

		return ret;
	}
}