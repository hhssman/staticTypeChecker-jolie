package staticTypechecker;

import java.util.HashMap;
import java.util.Map.Entry;

import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.context.ParsingContext;
import jolie.util.Range;

/**
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class TypeStructureDefinition extends OLSyntaxNode {
	private BasicTypeDefinition basicType; // the type of the root node
	private HashMap<String, TypeStructureDefinition> subtypes; // the subtypes of the root node, not subtypes are defined recursively as other TypeStructureDefintions
	private Range cardinality; // the cardinality of the root node

	public TypeStructureDefinition(BasicTypeDefinition basicType, Range cardinality, ParsingContext context){
		super(context);
		this.basicType = basicType;
		this.subtypes = new HashMap<>();
		this.cardinality = cardinality;
	}

	public BasicTypeDefinition basicType(){
		return this.basicType;
	}

	public Range cardinality(){
		return this.cardinality;
	}

	/**
	 * @param name the name of the subtype
	 * @param structure the structure of the subtype
	 * @return true if this node contains a subtype with the specified name, false otherwise
	 */
	public boolean hasSubtype(String name, TypeStructureDefinition structure){
		if(!this.subtypes.containsKey(name)){ // key does not exist
			return false;
		}

		return this.subtypes.get(name).isEquivalent(structure); // if they are equivalent, return true, and false otherwise
	}

	/**
	 * Adds a new subtype to this node.
	 * @param name the name of the subtype (what key to associate it to)
	 * @param subtype the structure of the subtype
	 */
	public void addSubtype(String name, TypeStructureDefinition subtype){
		// TODO: decide if we override existing keys or throw error
		this.subtypes.put(name, subtype);
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

		if(!this.basicType.equals(other.basicType)){ // root node is not of same type
			return false;
		}

		// iterate through each subtype and check their equivalence
		for(Entry<String, TypeStructureDefinition> entry : this.subtypes.entrySet()){
			String currKey = entry.getKey();
			TypeStructureDefinition currStructure = entry.getValue();

			if(other.subtypes.get(currKey) == null){ // other does not have subtype with this key
				return false;
			}

			if(!other.subtypes.get(currKey).isEquivalent(currStructure)){ // the other structure with this key is different from ours 
				return false;
			}
		}

		return true;
	}

	public <C, R> R accept(OLVisitor<C,R> v, C ctx){
		return null;
	}
}
