package staticTypechecker.typeStructures;

import java.util.HashMap;

import jolie.lang.parse.OLVisitor;

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
	public abstract String prettyString(int level, HashMap<String, Void> recursive);

}
