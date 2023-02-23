package staticTypechecker.typeStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import jolie.lang.parse.OLVisitor;
import staticTypechecker.entities.Symbol;

/**
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public abstract class Type implements Symbol {
	public abstract boolean isSubtypeOf(Type other);
	public abstract Type merge(Type other);

	public <C, R> R accept(OLVisitor<C,R> v, C ctx){
		return null;
	}

	public abstract Type copy(boolean finalize);
	public abstract Type copy(boolean finalize, ArrayList<Type> rec);
	public abstract String prettyStringHashCode();
	public abstract String prettyStringHashCode(int level, ArrayList<Type> recursive);

	public abstract String prettyString();
	public abstract String prettyString(int level, ArrayList<Type> recursive);

	/**
	 * If ts is empty or on error, return null
	 * @param ts
	 * @return
	 */
	public static Type union(Type[] ts){
		if(ts.length == 0){
			return null;
		}
		if(ts.length == 1){
			return ts[0];
		}

		Type tmp = null;
		for(int i = 1; i < ts.length; i++){
			tmp = Type.union(ts[i-1], ts[i]);
		}

		return tmp;
	}

	public static Type union(Type t1, Type t2){
		if(t1 == null){
			return t2;
		}

		if(t2 == null){
			return t1;
		}

		// none of them are null

		if(t1 instanceof InlineType && t2 instanceof InlineType){ // t1: inline and t2: inline
			InlineType p1 = (InlineType)t1; // parsed type1
			InlineType p2 = (InlineType)t2; // parsed type2

			// merge all childnames to one list
			HashSet<String> allChildNames = new HashSet<>();
			allChildNames.addAll(p1.children().keySet());
			allChildNames.addAll(p2.children().keySet());

			if(!p1.basicType().equals(p2.basicType())){ // basic types are not the same, create a choice with a choice for each basic type both with the merged children
				ChoiceType result = new ChoiceType();
				InlineType choice1 = new InlineType(p1.basicType(), null, null);
				InlineType choice2 = new InlineType(p2.basicType(), null, null);

				// run through the childnames and create the merged version of each child, and append it to both choices
				for(String childName : allChildNames){
					Type union = Type.union(p1.getChild(childName), p2.getChild(childName));
					choice1.put(childName, union);
					choice2.put(childName, union);
				}

				result.addChoice(choice1);
				result.addChoice(choice2);

				return result;
			}
			else{ // basic types are the same, union the children
				InlineType result = new InlineType(p1.basicType(), null, null);

				// run through the childnames and create the merged version of each child, and append it to both choices
				for(String childName : allChildNames){
					Type union = Type.union(p1.getChild(childName), p2.getChild(childName));
					result.put(childName, union);
				}
			}
		}
		else if(t1 instanceof InlineType && t2 instanceof ChoiceType){ // t1: inline and t2: choice

		}
		else if(t2 instanceof InlineType){ // t1: choice and t2: inline

		}
		else{ // t1: choice and t2: choice

		}

		return null;
	}
}
