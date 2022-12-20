package staticTypechecker.typeStructures;

import java.util.HashMap;
import java.util.Map.Entry;

import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.lang.parse.context.ParsingContext;
import jolie.util.Range;

/**
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public abstract class TypeStructure {
	
	// TODO
	public boolean isEquivalent(TypeStructure other){
		return false;
	}

	public <C, R> R accept(OLVisitor<C,R> v, C ctx){
		return null;
	}

	public abstract String prettyString();
	public abstract String prettyString(int level);

}
