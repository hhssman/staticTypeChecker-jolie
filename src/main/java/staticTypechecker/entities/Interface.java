package staticTypechecker.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import staticTypechecker.entities.Operation.OperationType;

/**
 * Represents an interface in Jolie
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
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

	public void addOperation(Operation op){
		this.operations.put(op.name(), op);
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

	public int hashCode(){
		return this.name.hashCode() + 31 * this.operations.hashCode();
	}

	public boolean equals(Object other){
		if(this == other){
			return true;
		}
		
		if(!(other instanceof Interface)){
			return false;
		}

		Interface parsedOther = (Interface)other;
		for(Entry<String, Operation> ent : this.operations.entrySet()){
			String opName = ent.getKey();
			Operation op = ent.getValue();

			if(!parsedOther.operations.containsKey(opName)){
				return false;
			}
			if(!op.equals(parsedOther.operations.get(opName))){
				return false;
			}
		}

		return true;
	}

	@Override
	public String prettyString(){
		if(this.name == null){
			return "null";
		}

		String ret = this.name;

		ArrayList<Operation> oneWay = new ArrayList<>();
		ArrayList<Operation> reqRes = new ArrayList<>();

		// split the operations based on type
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