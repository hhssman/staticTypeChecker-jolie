package staticTypechecker.faults;

public class TypeDefinitionLinkLoopFault implements Fault {
	private String typeName;

	public TypeDefinitionLinkLoopFault(String typeName){
		this.typeName = typeName;
	}

	public String getMessage(){
		return "Type definition link loop detected: " + this.typeName + " (this might mean that the referred type has not been defined or could not be retrieved)";
	}
}
