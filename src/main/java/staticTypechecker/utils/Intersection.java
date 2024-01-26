package staticTypechecker.utils;

import java.util.HashMap;

import staticTypechecker.entities.ChoiceType;
import staticTypechecker.entities.InlineType;
import staticTypechecker.entities.Type;
import staticTypechecker.entities.SymbolTable.Pair;

public class Intersection {
    
	public static Type intersection(Type t1, Type t2) {
		return intersection(t1, t2, new HashMap<>());
	}

	private static Type intersection(Type t1, Type t2, HashMap<Pair<Type, Type>, Type> seen) {
		Pair<Type, Type> pair = new Pair<>(t1, t2);
		if(seen.containsKey(pair)) {
			return seen.get(pair);
		}

		if(t1.isSubtypeOf(t2)) return t1;
		if(t2.isSubtypeOf(t1)) return t2;

		//If one or both are ChoiceType
		//TODO Don't add nothing to ChoiceType
		if(t1 instanceof ChoiceType) {
			ChoiceType tempT1 = (ChoiceType) t1;
			ChoiceType s = new ChoiceType();
			seen.put(pair, s);
			for(Type choice1 : tempT1.choices()) {
				if(t2 instanceof ChoiceType) {
					ChoiceType tempT2 = (ChoiceType) t2;
					for(Type choice2 : tempT2.choices()) {
						Type toAdd = intersection(choice1, choice2, seen);
						s.addChoiceUnsafe(toAdd);
					}
				} else {
					Type toAdd = intersection(choice1, t2, seen);
					s.addChoiceUnsafe(toAdd);
				}
			} //TODO Need a check to make sure that s contains choices
			return s;
		} else if(t2 instanceof ChoiceType) {
			ChoiceType tempT2 = (ChoiceType) t2;
			ChoiceType s = new ChoiceType();
			seen.put(pair, s);
			for(Type choice : tempT2.choices()) {
				Type toAdd = intersection(t1, choice, seen);
				s.addChoiceUnsafe(toAdd);
			} //TODO Need a check to make sure that s contains choices
			return s;
		}

		//Both are InlineType

		InlineType s = basicIntersection((InlineType)t1, (InlineType)t2);
		seen.put(pair, s);

		InlineType iT1 = (InlineType)t1;
		InlineType iT2 = (InlineType)t2;

		if(s != null) {
			for(String child : iT1.children().keySet()) {
				if(iT2.contains(child)) {

				} else if(iT2.isOpen()) {

				} //Check if child is optinmal
			}
		}

		return s;
	}

	//TODO Check for cardinality
	private static InlineType basicIntersection(InlineType t1, InlineType t2) {
		if(t1.basicType().checkBasicTypeEqualness(t2.basicType())) return new InlineType(t1.basicType(), t1.cardinality(), t1.context(), t1.isOpen());
		else if(t1.basicType().checkBasicTypeEqualness(Type.ANY().basicType())) return new InlineType(t2.basicType(), t2.cardinality(), t2.context(), t2.isOpen());
		else if(t2.basicType().checkBasicTypeEqualness(Type.ANY().basicType())) return new InlineType(t1.basicType(), t1.cardinality(), t1.context(), t1.isOpen());
		else return null;
	}
}
