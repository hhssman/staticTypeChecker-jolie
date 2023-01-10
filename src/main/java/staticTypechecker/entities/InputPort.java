package staticTypechecker.entities;

import java.util.List;

public class InputPort implements Symbol {
	private String name; // the name of the port
	private String location; // the location of the port
	private String protocol; // the protocol of the port
	private List<String> interfaces; // a list of the interfaces this input port uses

	public InputPort(String name, String location, String protocol, List<String> interfaces){
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

	public List<String> interfaces(){
		return this.interfaces;
	}

	public String prettyString(){
		return this.name + " at " + this.location + " via " + this.protocol + " using " + this.interfaces;
	}
}
