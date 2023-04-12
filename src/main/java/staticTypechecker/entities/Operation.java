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
	private String requestType; 			// the name of the type of the input
	private String responseType; 			// the name of the type of the output
	private OperationType operationType; 	// the type of operation (oneway or reqres)

	public Operation(String name, String requestType, String responseType, OperationType operationType){
		this.name = name;
		this.requestType = requestType;
		this.responseType = responseType;
		this.operationType = operationType;
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
		return this.operationType;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public void setRequestType(String requestType){
		this.requestType = requestType;
	}

	public void setResponseType(String responseType){
		this.responseType = responseType;
	}

	public void setOperationType(OperationType operationType){
		this.operationType = operationType;
	}

	public boolean isEqual(Operation other){
		boolean isEqual = 	this.name.equals(other.name) && 
							this.operationType.equals(other.operationType) && 
							this.requestType.equals(other.requestType);

		if(this.operationType == OperationType.REQRES){
			isEqual = isEqual && this.responseType.equals(other.responseType);
		}

		return isEqual;
	}

	public String prettyString(){
		if(this.name == null){
			return "null";
		}

		if(this.operationType == OperationType.REQRES){
			return this.name + "(" + this.requestType() + ")" + "(" + this.responseType() + ")";
		}
		else{
			return this.name + "(" + this.requestType() + ")";
		}
	}
}
