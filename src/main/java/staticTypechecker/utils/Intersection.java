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
		//Type t = intersection(t1, t2, new HashMap<>());
		//TypeUtils.hasValue(t, true);
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
					if(!(tempT instanceof EmptyType && !(tempT instanceof ChoiceType))) {
						Range card = findChildCard(iT1.getChildAndCard(child).key(), iT2.getChildAndCard(child).key());
						iS.addChildUnsafe(child, card, tempT);
					} else if(!(iT1.getChildAndCard(child).key().min() == 0) || !(iT2.getChildAndCard(child).key().min() == 0)) {
						seen.put(pair, new EmptyType());
						return new EmptyType();
					}

				} else if(iT2.isOpen()) {
					iS.addChildUnsafe(child, iT1.getChildAndCard(child).key(), iT1.getChild(child));

				} else if(!(iT1.getChildAndCard(child).key().min() == 0)) {
					seen.put(pair, new EmptyType());
					return new EmptyType();
				}
			}

			for(String child : iT2.children().keySet()) {
				if(!iS.contains(child)) {
					if(iT1.isOpen()) {
						iS.addChildUnsafe(child, iT2.getChildAndCard(child).key(), iT2.getChild(child));
					} else if(!(iT2.getChildAndCard(child).key().min() == 0)) {
						seen.put(pair, new EmptyType());
						return new EmptyType();
					}
				}
			}
		}

		return s;
	}

	private static Range findChildCard(Range child1, Range child2) {
		return new Range(child1.min() < child2.min() ? child1.min() : child2.min(), child1.max() > child2.max() ? child1.max() : child2.max());
	}

	private static Type basicIntersection(InlineType t1, InlineType t2) {
		if(t1.basicType().checkBasicTypeEqualness(t2.basicType())) return new InlineType(t1.basicType(), null, t1.context(), t1.isOpen() && t2.isOpen());
		else if(t1.basicType().checkBasicTypeEqualness(Type.ANY().basicType())) return new InlineType(t2.basicType(), null, t2.context(), t1.isOpen() && t2.isOpen());
		else if(t2.basicType().checkBasicTypeEqualness(Type.ANY().basicType())) return new InlineType(t1.basicType(), null, t1.context(), t1.isOpen() && t2.isOpen());
		else return new EmptyType();
	}

	public static boolean isOptinal(Type child) {
		if(child instanceof InlineType) {
			return ((InlineType)child).cardinality().min() == 0;
		} else {
			return ((ChoiceType)child).cardinality().min() == 0;
		}
	}
}
