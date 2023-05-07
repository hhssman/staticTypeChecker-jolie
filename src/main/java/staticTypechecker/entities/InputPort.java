package staticTypechecker.entities;

import java.util.HashSet;

/**
 * Represents an input port in Jolie
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class InputPort implements Symbol {
	private String name; 				// the name of the port
	private String location; 			// the location of the port
	private String protocol; 			// the protocol of the port
	private HashSet<Interface> interfaces; 	// a list of the interfaces this input port uses

	public InputPort(String name, String location, String protocol, HashSet<Interface> interfaces){
		this.name = name;
		this.location = location;
		this.protocol = protocol;
		this.interfaces = interfaces;
	}

	public String name(){
		return this.name;
	}

	public String location(){
		return this.location;
	}

	public String protocol(){
		return this.protocol;
	}

	public HashSet<Interface> interfaces(){
		return this.interfaces;
	}

	public void setName(String name){
		this.name = name;
	}

	public void setLocation(String location){
		this.location = location;
	}

	public void setProtocol(String protocol){
		this.protocol = protocol;
	}

	public void setInterfaces(HashSet<Interface> interfaces){
		this.interfaces = interfaces;
	}

	public int hashCode(){
		return (int)(this.name.hashCode() + 31 * this.location.hashCode() + Math.pow(31, 2) * this.protocol.hashCode() + Math.pow(31, 3) * this.interfaces.hashCode());
	}

	public boolean equals(Object other){
		if(this == other){
			return true;
		}

		if(!(other instanceof InputPort)){
			return false;
		}

		InputPort parsedOther = (InputPort)other;

		if(!(this.name.equals(parsedOther.name) && this.location.equals(parsedOther.location) && this.protocol.equals(parsedOther.protocol))){
			return false;
		}

		if(this.interfaces.size() != parsedOther.interfaces.size()){
			return false;
		}

		for(Interface i : this.interfaces){
			if(!parsedOther.interfaces.contains(i)){
				return false;
			}
		}

		return true;
	}
	
	public String prettyString(){
		
		if(this.name == null){
			return "null";
		}
		
		String res = this.name + " at " + this.location + " via " + this.protocol + " exposing [";

		if(this.interfaces.size() == 0){
			res += "]";
		}
		else{
			for(Interface i : this.interfaces){
				res += "\n-\n" + i.prettyString();
			}

			res += "\n]";
		}

		return res;
	}
}
