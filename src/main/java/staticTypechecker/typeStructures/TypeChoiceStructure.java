package staticTypechecker.typeStructures;

/**
 * A type structure representing a choice type. Choices are defined recursively as more TypeChoiceStructures
 * 
 * @author Kasper Bergstedt
 */
public class TypeChoiceStructure extends TypeStructure {
	private TypeStructure left;
	private TypeStructure right;

	public TypeChoiceStructure(TypeStructure left, TypeStructure right){
		this.left = left;
		this.right = right;
	}

	public TypeStructure left(){
		return this.left;
	}

	public TypeStructure right(){
		return this.right;
	}

	public String toString(){
		return left.toString() + " | " + right.toString();
	}

	public String prettyString(){
		return this.left.prettyString(0) + "\n|\n" + this.right.prettyString(0);
	}

	public String prettyString(int level){
		return this.left.prettyString(level) + "\n|\n" + this.right.prettyString(level);
	}
}
