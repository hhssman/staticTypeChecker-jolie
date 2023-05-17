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
	private Type requestType; 				// the name of the type of the input
	private Type responseType; 				// the name of the type of the output
	private OperationType operationType; 	// the type of operation (oneway or reqres)

	public Operation(String name, Type requestType, Type responseType, OperationType operationType){
		this.name = name;
		this.requestType = requestType == null ? Type.VOID() : requestType;
		this.responseType = responseType == null ? Type.VOID() : responseType;
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

	public int hashCode(){
		return (int)(this.name.hashCode() + 31 * this.requestType.hashCode() + Math.pow(31, 2) * (this.responseType == null ? 0 : this.responseType.hashCode()) + this.operationType.hashCode());
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

	public boolean isCompatibleWith(Operation other){
		Operation parsedOther = (Operation)other;

		boolean isEqual = 	this.name.equals(parsedOther.name) && 
							this.operationType.equals(parsedOther.operationType) && 
							this.requestType.isSubtypeOf(parsedOther.requestType);

		if(this.operationType == OperationType.REQRES){
			isEqual = isEqual && this.responseType.isSubtypeOf(parsedOther.responseType);
		}

		return isEqual;
	}

	public String prettyString(){
		return prettyString(0);
	}

	public String prettyString(int level){
		String ret = "\t".repeat(level);

		if(this.name == null){
			return ret + "null";
		}

		ret += this.name;

		String request = this.requestType().prettyString(level+1);
		if(request.contains("\n")){
			ret += "\n" + "\t".repeat(level) + "(\n" + "\t".repeat(level+1) + request + "\n" + "\t".repeat(level) + ")";
		}
		else{
			ret += "(" + request + ")";
		}

		if(this.operationType == OperationType.REQRES){
			String response = this.requestType().prettyString(level+1);
			if(response.contains("\n")){
				ret += "\n" + "\t".repeat(level) + "(\n" + "\t".repeat(level+1) + response + "\n" + "\t".repeat(level) + ")";
			}
			else{
				ret += "(" + response + ")";
			}
		}

		return ret;
	}
}
