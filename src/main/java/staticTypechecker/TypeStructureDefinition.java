package staticTypechecker;

import java.util.HashMap;
import java.util.Map.Entry;

import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.context.ParsingContext;
import jolie.util.Range;

/**
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class TypeStructureDefinition {
	private BasicTypeDefinition basicType; // the type of the root node
	private HashMap<String, TypeStructureDefinition> children; // the children of the root node, not children are defined recursively as other TypeStructureDefintions
	private Range cardinality; // the cardinality of the root node
	private boolean finalized; // indicates whether this type is open to new children or not. If true, the structure is done and we do not allow more children

	public TypeStructureDefinition(BasicTypeDefinition basicType, Range cardinality, ParsingContext context){
		this.basicType = basicType;
		this.children = new HashMap<>();
		this.cardinality = cardinality;
		this.finalized = false;
	}

	public BasicTypeDefinition basicType(){
		return this.basicType;
	}

	public Range cardinality(){
		return this.cardinality;
	}

	/**
	 * @param name the name of the child
	 * @param structure the structure of the child
	 * @return true if this node contains a child with the specified name, false otherwise
	 */
	public boolean hasChild(String name, TypeStructureDefinition structure){
		if(!this.children.containsKey(name)){ // key does not exist
			return false;
		}

		return this.children.get(name).isEquivalent(structure); // if they are equivalent, return true, and false otherwise
	}

	public TypeStructureDefinition getChild(String name){
		return this.children.get(name);
	}

	/**
	 * Adds a new child to this node iff the type has not been finalized.
	 * @param name the name of the child (what key to associate it to)
	 * @param child the structure of the child
	 */
	public void addChild(String name, TypeStructureDefinition child){
		if(!this.finalized){
			// TODO: decide if we override existing keys or throw error
			this.children.put(name, child);
		}
	}

	public void finalize(){
		this.finalized = true;
	}

	/**
	 * Checks equality for the two TypeStructureDefintions
	 * @param other the other object
	 * @return true if the objects are structural equivalent and false otherwise
	 */
	public boolean isEquivalent(TypeStructureDefinition other){
		if(this == other){ // pointers match
			return true;
		}

		if(!this.basicType.checkBasicTypeEqualness(other.basicType)){ // root node is not of same type
			return false;
		}

		// iterate through each child and check their equivalence
		for(Entry<String, TypeStructureDefinition> entry : this.children.entrySet()){
			String currKey = entry.getKey();
			TypeStructureDefinition currStructure = entry.getValue();

			if(!other.children.containsKey(currKey)){ // other does not have child with this key
				return false;
			}

			if(!other.children.get(currKey).isEquivalent(currStructure)){ // the other structure with this key is different from ours 
				return false;
			}
		}

		return true;
	}

	public <C, R> R accept(OLVisitor<C,R> v, C ctx){
		return null;
	}

	/**
	 * Get a nice string representing this structure
	 */
	public String prettyString(){
		return this.prettyString(0);
	}

	private String prettyString(int level){
		String prettyString = "";

		prettyString += this.basicType.nativeType().id();

		if(this.cardinality.min() != 1 || this.cardinality.max() != 1){ // no range
			prettyString += "[" + this.cardinality.min() + "," + this.cardinality.max() + "]";
		}

		prettyString += " {";
		
		if(this.children.size() != 0){
			for(Entry<String, TypeStructureDefinition> child : this.children.entrySet()){
				prettyString += "\n" + "\t".repeat(level+1) + child.getKey() + ": " + child.getValue().prettyString(level+1);
			}
			prettyString += "\n" + "\t".repeat(level) + "}";
		}
		else{
			prettyString += "}";
		}

		return prettyString;
	}
}
