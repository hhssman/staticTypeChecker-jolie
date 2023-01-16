package staticTypechecker.entities;

import java.util.List;

public class OutputPort implements Symbol {
	private String name; // the name of the port
	private String location; // the location of the port
	private String protocol; // the protocol of the port
	private List<String> interfaces; // a list of the interfaces this output port uses

	public OutputPort(String name, String location, String protocol, List<String> interfaces){
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

	public static OutputPort getBasePort(){
		return new OutputPort(null, null, null, null);
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

	public void setInterfaces(List<String> interfaces){
		this.interfaces = interfaces;
	}

	public String prettyString(){
		if(this.name == null){
			return "null";
		}
		
		return this.name + " at " + this.location + " via " + this.protocol + " using " + this.interfaces;
	}
}
