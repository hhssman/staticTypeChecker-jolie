package staticTypechecker.entities;

/**
 * Represents an operation in Jolie
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class Operation implements Symbol {
	public enum OperationType{
		ONEWAY, REQRES
	}

	private String name; 					// the name of the operation
	private Type requestType; 			// the name of the type of the input
	private Type responseType; 			// the name of the type of the output
	private OperationType operationType; 	// the type of operation (oneway or reqres)

	public Operation(String name, Type requestType, Type responseType, OperationType operationType){
		this.name = name;
		this.requestType = requestType;
		this.responseType = responseType;
		this.operationType = operationType;
	}

	public String name(){
		return this.name;
	}

	public Type requestType(){
		return this.requestType;
	}

	public Type responseType(){
		return this.responseType;
	}

	public OperationType type(){
		return this.operationType;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public void setRequestType(Type requestType){
		this.requestType = requestType;
	}

	public void setResponseType(Type responseType){
		this.responseType = responseType;
	}

	public void setOperationType(OperationType operationType){
		this.operationType = operationType;
	}

	public boolean equals(Object other){
		if(this == other){
			return true;
		}
		
		if(!(other instanceof Operation)){
			return false;
		}
		
		Operation parsedOther = (Operation)other;

		boolean isEqual = 	this.name.equals(parsedOther.name) && 
							this.operationType.equals(parsedOther.operationType) && 
							this.requestType.equals(parsedOther.requestType);

		if(this.operationType == OperationType.REQRES){
			isEqual = isEqual && this.responseType.equals(parsedOther.responseType);
		}

		return isEqual;
	}

	public String prettyString(){
		if(this.name == null){
			return "null";
		}

		String ret = this.name + "\n\t\t(\n\t\t\t" + this.requestType().prettyString(3) + "\n\t\t)";

		if(this.operationType == OperationType.REQRES){
			ret += "\n\t\t(\n\t\t\t" + this.responseType().prettyString(3) + "\n\t\t)";
		}

		return ret;
	}
}
