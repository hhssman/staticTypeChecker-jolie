package staticTypechecker.typeStructures;

import jolie.lang.parse.OLVisitor;

/**
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class TypeNameDefinition {
	private String name;
	private TypeStructure structure;

	public TypeNameDefinition(String name, TypeStructure structure){
		this.name = name;
		this.structure = structure;
	}

	/**
	 * @return the name of this type
	 */
	public String name(){
		return this.name;
	}

	/**
	 * @return the structure of this type
	 */
	public TypeStructure structure(){
		return this.structure;
	}

	public boolean isEqual(TypeNameDefinition other){
		if(this.structure == other.structure){ // structures are the same object
			return true;
		}
		
		if(this.structure.isEquivalent(other.structure)){ // structures are equivalent, update our pointer
			this.structure = other.structure;
			return true;
		}

		return false;
	}

	public <C, R> R accept(OLVisitor<C,R> v, C ctx){
		return null;
	}
}
