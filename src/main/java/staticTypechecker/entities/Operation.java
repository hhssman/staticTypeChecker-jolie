package staticTypechecker.entities;

public class Operation implements Symbol {
	public enum OperationType{
		ONEWAY, REQRES
	}

	private String name; // the name of the operation
	private String requestType; // the name of the type of the input
	private String responseType; // the name of the type of the output
	private OperationType opType; // the type of operation (oneway or reqres)

	public Operation(String name, String requestType, String responseType, OperationType opType){
		this.name = name;
		this.requestType = requestType;
		this.responseType = responseType;
		this.opType = opType;
	}

	public String name(){
		return this.name;
	}

	public String requestType(){
		return this.requestType;
	}

	public String responseType(){
		return this.responseType;
	}

	public OperationType type(){
		return this.opType;
	}

	public String prettyString(){
		if(this.opType == OperationType.REQRES){
			return this.name + "(" + this.requestType() + ")" + "(" + this.responseType() + ")";
		}
		else{
			return this.name + "(" + this.requestType() + ")";
		}
	}
}
