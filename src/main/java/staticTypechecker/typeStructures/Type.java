package staticTypechecker.typeStructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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

	public abstract void put(String childName, Type structure);
	public abstract Type copy();
	public abstract Type copy(boolean finalize);
	public abstract Type copy(boolean finalize, HashMap<Type, Type> rec);
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

	/**
	 * DOES NOT WORK YET
	 */
	public static Type union(Type type1, Type type2){
		if(type1 == null && type2 == null){
			return null;
		}
		
		if(type1 == null){
			return type2.copy();
		}
		
		if(type2 == null){
			return type1.copy();
		}

		// none of them are null
		Type t1 = type1.copy();
		Type t2 = type2.copy();
		return Type.unionRec(t1, t2);
	}

	private static Type unionRec(Type t1, Type t2){
		if(t1 == null){
			return t2;
		}
		if(t2 == null){
			return t1;
		}

		if(t1 instanceof InlineType && t2 instanceof InlineType){ // t1: inline and t2: inline
			InlineType p1 = (InlineType)t1; // parsed type1
			InlineType p2 = (InlineType)t2; // parsed type2

			// merge all childnames to one list
			HashSet<String> allChildNames = new HashSet<>();
			allChildNames.addAll(p1.children().keySet());
			allChildNames.addAll(p2.children().keySet());

			if(!p1.basicType().equals(p2.basicType())){ // basic types are not the same, create a choice with a choice for each basic type both with the merged children
				ChoiceType result = new ChoiceType();
				InlineType choice1 = p1; //new InlineType(p1.basicType(), null, null);
				InlineType choice2 = p2; // new InlineType(p2.basicType(), null, null);

				// run through the childnames and create the merged version of each child, and append it to both choices
				for(String childName : allChildNames){
					Type union = Type.unionRec(p1.getChild(childName), p2.getChild(childName));
					choice1.put(childName, union);
					choice2.put(childName, union);
				}

				result.addChoice(choice1);
				result.addChoice(choice2);

				return result;
			}
			else{ // basic types are the same, union the children
				InlineType result = p1; // new InlineType(p1.basicType(), null, null);

				// run through the childnames and create the merged version of each child, and append it to both choices
				for(String childName : allChildNames){
					Type union = Type.unionRec(p1.getChild(childName), p2.getChild(childName));
					result.put(childName, union);
				}

				return result;
			}
		}
		else if(t1 instanceof InlineType && t2 instanceof ChoiceType){ // t1: inline and t2: choice
			ChoiceType result = new ChoiceType();
			InlineType p1 = (InlineType)t1;
			ChoiceType p2 = (ChoiceType)t2;
			
			// each choice of t2 will be the merged version with t1
			for(InlineType choice : p2.choices()){
				result.addChoice(Type.unionRec(choice, p1));
			}

			return result;
		}
		else if(t2 instanceof InlineType){ // t1: choice and t2: inline
			return Type.unionRec(t2, t1); // return the union of the reversed, it is the same
		}
		else{ // t1: choice and t2: choice
			ChoiceType result = new ChoiceType();
			ChoiceType p1 = (ChoiceType)t1;
			ChoiceType p2 = (ChoiceType)t2;

			// for each choice in p1, merge with all choices of p2
			for(InlineType c1 : p1.choices()){
				for(InlineType c2 : p2.choices()){
					result.addChoice(Type.unionRec(c1, c2));
				}
			}

			return result;
		}
	}
}
