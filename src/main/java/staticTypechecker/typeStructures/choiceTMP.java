// package staticTypechecker.typeStructures;

// import java.util.HashMap;

// import jolie.lang.parse.ast.types.TypeChoiceDefinition;

// /**
//  * A type structure representing a choice type. Choices are defined recursively as more TypeChoiceStructures
//  * 
//  * @author Kasper Bergstedt
//  */
// public class TypeChoiceStructure extends TypeStructure {
// 	private TypeStructure left;
// 	private TypeStructure right;

// 	public TypeChoiceStructure(TypeStructure left, TypeStructure right){
// 		this.left = left;
// 		this.right = right;
// 	}

// 	public void setLeft(TypeStructure newLeft){
// 		this.left = newLeft;
// 	}

// 	public void setRight(TypeStructure newRight){
// 		this.right = newRight;
// 	}

// 	public TypeStructure left(){
// 		return this.left;
// 	}

// 	public TypeStructure right(){
// 		return this.right;
// 	}

// 	public static TypeChoiceStructure getBaseSymbol(TypeChoiceDefinition typeDef){
// 		return (TypeChoiceStructure)TypeConverter.createBaseStructure(typeDef);
// 	}

// 	public String toString(){
// 		return left.toString() + " | " + right.toString();
// 	}

// 	public TypeChoiceStructure copy(boolean finalize){
// 		return new TypeChoiceStructure(this.left.copy(finalize), this.right.copy(finalize));
// 	}

// 	public String prettyString(){
// 		HashMap<String, Void> recursive = new HashMap<>();
// 		return this.left.prettyString(0, recursive) + "\n|\n" + this.right.prettyString(0, recursive);
// 	}

// 	public String prettyString(int level, HashMap<String, Void> recursive){
// 		return this.left.prettyString(level, recursive) + "\n" + "\t".repeat(level) + "|" + "\n" + "\t".repeat(level) + this.right.prettyString(level, recursive);
// 	}
// }
