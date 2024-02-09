package staticTypechecker.utils;

import java.util.HashMap;

import jolie.util.Range;
import staticTypechecker.entities.ChoiceType;
import staticTypechecker.entities.EmptyType;
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
		if(t1 instanceof ChoiceType) {
			ChoiceType tempT1 = (ChoiceType) t1;
			ChoiceType s = new ChoiceType();
			seen.put(pair, s);
			for(Type choice1 : tempT1.choices()) {
				if(t2 instanceof ChoiceType) {
					ChoiceType tempT2 = (ChoiceType) t2;
					for(Type choice2 : tempT2.choices()) {
						Type toAdd = intersection(choice1, choice2, seen);
						if(!(toAdd instanceof EmptyType)) {
							s.addChoiceUnsafe(toAdd);
						}
					}
				} else {
					Type toAdd = intersection(choice1, t2, seen);
					if(!(toAdd instanceof EmptyType)) {
						s.addChoiceUnsafe(toAdd);
					}
				}
			}
			if(s.choices().isEmpty()) {
				seen.put(pair, new EmptyType());
				return new EmptyType();
			}
			return s;
		} else if(t2 instanceof ChoiceType) {
			ChoiceType tempT2 = (ChoiceType) t2;
			ChoiceType s = new ChoiceType();
			seen.put(pair, s);
			for(Type choice : tempT2.choices()) {
				Type toAdd = intersection(t1, choice, seen);
				if(!(toAdd instanceof EmptyType)) {
					s.addChoiceUnsafe(toAdd);
				}
			}
			if(s.choices().isEmpty()) {
				seen.put(pair, new EmptyType());
				return new EmptyType();
			}
			return s;
		}

		//Both are InlineType
		InlineType iT1 = (InlineType)t1;
		InlineType iT2 = (InlineType)t2;

		Type s = basicIntersection(iT1, iT2);
		seen.put(pair, s);

		if(!(s instanceof EmptyType)) {
			InlineType iS = (InlineType)s;
			for(String child : iT1.children().keySet()) {
				if(iT2.contains(child)) {
					Type tempT = intersection(iT1.getChild(child), iT2.getChild(child), seen);
					if(!(tempT instanceof EmptyType)) {
						iS.addChildUnsafe(child, tempT);
					} else if(!isOptinal(iT1.getChild(child)) || !isOptinal(iT2.getChild(child))) {
						seen.put(pair, new EmptyType());
						return new EmptyType();
					}

				} else if(iT2.isOpen()) {
					iS.addChildUnsafe(child, iT1.getChild(child));

				} else if(!isOptinal(iT1.getChild(child))) {
					seen.put(pair, new EmptyType());
					return new EmptyType();
				}
			}

			for(String child : iT2.children().keySet()) {
				if(!iS.contains(child)) {
					if(iT1.isOpen()) {
						iS.addChildUnsafe(child, iT2.getChild(child));
					} else if(!isOptinal(iT2.getChild(child))) {
						seen.put(pair, new EmptyType());
						return new EmptyType();
					}
				}
			}
		}

		return s;
	}

	private static Type basicIntersection(InlineType t1, InlineType t2) {
		int min = t1.cardinality().min() < t2.cardinality().min() ? t2.cardinality().min() : t1.cardinality().min();
		int max = t1.cardinality().max() > t2.cardinality().max() ? t2.cardinality().max() : t1.cardinality().max();
		Range cardinality = new Range(min, max);
		if(t1.basicType().checkBasicTypeEqualness(t2.basicType())) return new InlineType(t1.basicType(), cardinality, t1.context(), t1.isOpen() && t2.isOpen());
		else if(t1.basicType().checkBasicTypeEqualness(Type.ANY().basicType())) return new InlineType(t2.basicType(), cardinality, t2.context(), t1.isOpen() && t2.isOpen());
		else if(t2.basicType().checkBasicTypeEqualness(Type.ANY().basicType())) return new InlineType(t1.basicType(), cardinality, t1.context(), t1.isOpen() && t2.isOpen());
		else return new EmptyType();
	}

	private static boolean isOptinal(Type child) {
		if(child instanceof InlineType) {
			return ((InlineType)child).cardinality().min() == 0;
		} else {
			InlineType choiceChild = ((ChoiceType)child).choices().get(0);
			return choiceChild.cardinality().min() == 0;
		}
	}
}
