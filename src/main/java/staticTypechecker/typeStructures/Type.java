package staticTypechecker.typeStructures;

import java.util.ArrayList;

import jolie.lang.parse.OLVisitor;
import staticTypechecker.entities.Symbol;

/**
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public abstract class Type implements Symbol {
	public abstract boolean equals(Type other);
	public abstract boolean isSubtypeOf(Type other);
	public abstract Type merge(Type other);

	public <C, R> R accept(OLVisitor<C,R> v, C ctx){
		return null;
	}

	public abstract Type copy(boolean finalize);

	public abstract String prettyString();
	public abstract String prettyString(int level, ArrayList<Type> recursive);
}
