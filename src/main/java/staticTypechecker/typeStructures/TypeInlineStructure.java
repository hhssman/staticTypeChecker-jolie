package staticTypechecker.typeStructures;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import jolie.lang.parse.context.ParsingContext;
import jolie.util.Range;

/**
 * Represents the structure of a type in Jolie. It is a tree with a root node, which has a BasicTypeDefinition and a Range, and then a HashMap of child nodes, each referenced by a name. New children can be added until the finalize function is called (open record vs closed record). 
 * 
 * @author Kasper Bergstedt
 */
public class TypeInlineStructure extends TypeStructure {
	private BasicTypeDefinition basicType; // the type of the root node
	private HashMap<String, TypeStructure> children; // the children of the root node, not children are defined recursively as other TypeStructureDefintions
	private Range cardinality; // the cardinality of the root node
	private boolean finalized; // indicates whether this type is open to new children or not. If true, the structure is done and we do not allow more children
	private ParsingContext context;

	public TypeInlineStructure(BasicTypeDefinition basicType, Range cardinality, ParsingContext context){
		this.basicType = basicType;
		this.children = new HashMap<>();
		this.cardinality = cardinality;
		this.context = context;
		this.finalized = false;
	}

	public void reset(){
		this.basicType = null;
		this.children = null;
		this.cardinality = null;
		this.context = null;
		this.finalized = false;
	}

	public BasicTypeDefinition basicType(){
		return this.basicType;
	}

	public Range cardinality(){
		return this.cardinality;
	}

	public ParsingContext context(){
		return this.context;
	}

	public void setBasicType(BasicTypeDefinition basicType){
		this.basicType = basicType;
	}

	public void setChildren(HashMap<String, TypeStructure> children){
		this.children = children;
	}

	public void addChildren(HashMap<String, TypeStructure> children){
		this.children.putAll(children);
	}

	public void setCardinality(Range cardinality){
		this.cardinality = cardinality;
	}

	public void setContext(ParsingContext context){
		this.context = context;
	}

	/**
	 * @param name the name of the child
	 * @param structure the structure of the child
	 * @return true if this node contains a child with the specified name, false otherwise
	 */
	public boolean contains(String name, TypeStructure structure){
		if(!this.children.containsKey(name)){ // key does not exist
			return false;
		}

		return this.children.get(name).isEquivalent(structure); // if they are equivalent, return true, and false otherwise
	}

	public boolean contains(String name){
		return this.children.containsKey(name);
	}

	public TypeStructure getChild(String name){
		return this.children.get(name);
	}

	public String getChildName(TypeStructure struct){
		for(Entry<String, TypeStructure> child : this.children.entrySet()){
			if(child.getValue().equals(struct)){
				return child.getKey();
			}
		}
		return "";
	}

	public HashMap<String, TypeStructure> children(){
		return this.children;
	}

	/**
	 * Adds the entry {name, child} to this node iff the type has not been finalized. Owerwrites existing entries. 
	 * @param name the name of the child (what key to associate it to)
	 * @param child the structure of the child
	 */
	public void put(String name, TypeStructure child){
		if(!this.finalized){
			this.children.put(name, child);
		}
	}

	public void removeChild(String name){
		if(!this.finalized){
			this.children.remove(name);
		}
	}

	public void removeChild(TypeInlineStructure child){
		for(Entry<String, TypeStructure> ent : this.children.entrySet()){
			if(ent.getValue().equals(child)){
				this.children.remove(ent.getKey());
			}
		}
	}

	public void finalize(){
		this.finalized = true;
	}

	/**
	 * Checks equality for the two TypeInlineStructures
	 * @param other the other object
	 * @return true if the objects are structural equivalent and false otherwise
	 */
	public boolean isEquivalent(TypeInlineStructure other){
		if(this == other){ // pointers match
			return true;
		}

		if(!this.basicType.checkBasicTypeEqualness(other.basicType)){ // root node is not of same type
			return false;
		}

		// iterate through each child and check their equivalence
		for(Entry<String, TypeStructure> entry : this.children.entrySet()){
			String currKey = entry.getKey();
			TypeStructure currStructure = entry.getValue();

			if(!other.children.containsKey(currKey)){ // other does not have child with this key
				return false;
			}

			if(!other.children.get(currKey).isEquivalent(currStructure)){ // the other structure with this key is different from ours 
				return false;
			}

			// a child with same key has equivalent structure, we can update our structure pointer
			this.children.put(currKey, other.children.get(currKey));
		}

		return true;
	}

	public static TypeInlineStructure getBaseSymbol(){
		return new TypeInlineStructure(null, null, null);
	}

	public static TypeInlineStructure getBaseSymbol(TypeInlineDefinition typeDef){
		return (TypeInlineStructure)TypeConverter.createBaseStructure(typeDef);
	}

	public static TypeInlineStructure getBaseSymbol(TypeDefinitionLink typeDef){
		return (TypeInlineStructure)TypeConverter.createBaseStructure(typeDef);
	}

	public static TypeInlineStructure getBasicType(NativeType type){
		return new TypeInlineStructure(BasicTypeDefinition.of(type), null, null);
	}

	/**
	 * Creates a deep copy of this structure
	 * @param finalize whether or not the copy should be finalized
	 * @return the deep copy
	 */
	public TypeInlineStructure copy(boolean finalize){
		TypeInlineStructure struct = new TypeInlineStructure(this.basicType, this.cardinality, this.context);

		this.children.entrySet().forEach(child -> {
			String childName = child.getKey();
			TypeStructure childStruct = child.getValue();

			struct.put(childName, childStruct.copy(finalize));
		});

		if(finalize){
			struct.finalize();
		}
		
		return struct;
	}

	/**
	 * Dummy equals
	 * TODO make it correct
	 */
	public boolean equals(Object other){
		if(!(other instanceof TypeInlineStructure)){
			return false;
		}

		TypeInlineStructure parsedOther = (TypeInlineStructure)other;

		if(!this.basicType.equals(parsedOther.basicType())){
			return false;
		}

		// if(!this.cardinality.equals(parsedOther.cardinality)){
		// 	return false;
		// }

		for(Entry<String, TypeStructure> child : this.children.entrySet()){
			if(!parsedOther.contains(child.getKey())){
				return false;
			}

			if(!parsedOther.getChild(child.getKey()).equals(child.getValue())){
				return false;
			}
		}

		return true;
	}

	public int hashCode(){
		int hashCode = 0;
		
		hashCode += this.basicType.hashCode();

		int i = 1;
		for(Entry<String, TypeStructure> child : this.children.entrySet()){
			hashCode += (i * 31) * child.getValue().hashCode();
			i++;
		}

		return hashCode;
	}

	/**
	 * Get a nice string representing this structure
	 */
	public String prettyString(){
		return this.prettyString(0, new HashMap<>());
	}

	public String prettyString(int level, HashMap<String, Void> recursive){
		// String prettyString = this.children.size() != 0 ? "\n" + "\t".repeat(level) : "";
		String prettyString = "";
		prettyString += this.basicType != null ? this.basicType.nativeType().id() + " " : "";

		if(this.cardinality != null && (this.cardinality.min() != 1 || this.cardinality.max() != 1)){ // there is a range
			prettyString += "[" + this.cardinality.min() + "," + this.cardinality.max() + "]";
		}

		if(this.children.size() != 0){
			// prettyString += "\n" + "\t".repeat(level) + "{";
			prettyString += "{";

			prettyString += this.children.entrySet().stream().map(child -> {
				if(recursive.containsKey(child.getKey()) && false){ // TODO, temporarily disabled recursive checking (IT IS WRONG TO ONLY CHECK THE CHILD NAME)
					return "\n" + "\t".repeat(level+1) + child.getKey() + " (recursive structure)";
				}
				else{
					recursive.put(child.getKey(), null);
					HashMap<String, Void> rec = new HashMap<>(recursive); // shallow copy to not pass the same to each choice
					return "\n" + "\t".repeat(level+1) + child.getKey() + ": " + child.getValue().prettyString(level+2, rec);
				}
			})
			.collect(Collectors.joining("\n"));

			// for(Entry<String, TypeStructure> child : this.children.entrySet()){
			// 	if(recursive.containsKey(child.getKey())){
			// 		prettyString += "\n" + "\t".repeat(level+1) + child.getKey() + " (recursive structure)";
			// 	}
			// 	else{
			// 		recursive.put(child.getKey(), null);
			// 		prettyString += "\n" + "\t".repeat(level+1) + child.getKey() + "(" + level + ")" + ": " + child.getValue().prettyString(level+2, recursive);
			// 	}
			// 	prettyString += "\n" + "\t".repeat(level+1);
			// }
			prettyString += "\n" + "\t".repeat(level) + "}";
		}

		return prettyString;
	}
}
