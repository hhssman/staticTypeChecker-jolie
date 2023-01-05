package staticTypechecker.typeStructures;

import java.util.HashMap;
import java.util.Map.Entry;

import jolie.lang.parse.ast.types.BasicTypeDefinition;
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
	public boolean hasChild(String name, TypeStructure structure){
		if(!this.children.containsKey(name)){ // key does not exist
			return false;
		}

		return this.children.get(name).isEquivalent(structure); // if they are equivalent, return true, and false otherwise
	}

	public TypeStructure getChild(String name){
		return this.children.get(name);
	}

	public HashMap<String, TypeStructure> children(){
		return this.children;
	}

	/**
	 * Adds a new child to this node iff the type has not been finalized.
	 * @param name the name of the child (what key to associate it to)
	 * @param child the structure of the child
	 */
	public void addChild(String name, TypeStructure child){
		if(!this.finalized){
			// TODO: decide if we override existing keys or throw error
			this.children.put(name, child);
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

	/**
	 * Get a nice string representing this structure
	 */
	public String prettyString(){
		return this.prettyString(0, new HashMap<>());
	}

	public String prettyString(int level, HashMap<String, Void> recursive){
		String prettyString = "";

		prettyString += this.basicType.nativeType().id();

		if(this.cardinality.min() != 1 || this.cardinality.max() != 1){ // no range
			prettyString += "[" + this.cardinality.min() + "," + this.cardinality.max() + "]";
		}

		prettyString += " {";
		
		if(this.children.size() != 0){
			for(Entry<String, TypeStructure> child : this.children.entrySet()){
				if(recursive.containsKey(child.getKey())){
					prettyString += "\n" + "\t".repeat(level+1) + child.getKey() + " (recursive structure)";
				}
				else{
					recursive.put(child.getKey(), null);
					prettyString += "\n" + "\t".repeat(level+1) + child.getKey() + ": " + child.getValue().prettyString(level+1, recursive);
				}
			}
			prettyString += "\n" + "\t".repeat(level) + "}";
		}
		else{
			prettyString += "}";
		}

		return prettyString;
	}
}
