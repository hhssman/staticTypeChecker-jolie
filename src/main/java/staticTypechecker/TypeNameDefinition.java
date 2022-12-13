package staticTypechecker;

import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.context.ParsingContext;

/**
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class TypeNameDefinition extends OLSyntaxNode {
	private String name;
	private TypeStructureDefinition structure;

	public TypeNameDefinition(String name, TypeStructureDefinition structure, ParsingContext context){
		super(context);
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
	public TypeStructureDefinition structure(){
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
