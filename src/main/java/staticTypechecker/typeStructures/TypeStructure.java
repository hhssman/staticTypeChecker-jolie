package staticTypechecker.typeStructures;

import java.util.HashMap;

import jolie.lang.parse.OLVisitor;
import staticTypechecker.entities.Symbol;

/**
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public abstract class TypeStructure implements Symbol {
	
	// TODO
	public abstract boolean equals(TypeStructure other);
	public abstract boolean isSubtypeOf(TypeStructure other);

	public <C, R> R accept(OLVisitor<C,R> v, C ctx){
		return null;
	}

	public abstract TypeStructure copy(boolean finalize);

	public abstract String prettyString();
	public abstract String prettyString(int level, HashMap<String, Void> recursive);
}
