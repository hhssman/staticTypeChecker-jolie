package staticTypechecker.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Set;

import jolie.lang.NativeType;
import jolie.util.Pair;
import staticTypechecker.typeStructures.ChoiceType;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;

public class Bisimulator {
	public static boolean isSubtypeOf(Type t1, Type t2){
		return isSubtypeOfRec(t1, t2, new IdentityHashMap<>());
	}

	public static boolean equivalent(Type t1, Type t2){
		// return naive(t1, t2);
		return isSubtypeOf(t1, t2) && isSubtypeOf(t2, t1);
	}

	private static boolean isSubtypeOfRec(Type t1, Type t2, IdentityHashMap<Type, Type> R){
		if(isInRelation(t1, t2, R)){
			return true;
		}

		R.put(t1, t2);
		if(t1 instanceof InlineType && t2 instanceof InlineType){
			InlineType x = (InlineType)t1;
			InlineType y = (InlineType)t2;

			Set<String> xLabels = x.children().keySet();
			Set<String> yLabels = y.children().keySet();

			if(!basicSubtype(x, y)){
				return false;
			}
			if(!isSubSetOf(yLabels, xLabels)){
				return false;
			}
			for(String label : yLabels){
				Type xChild = x.getChild(label);
				Type yChild = y.getChild(label);

				// System.out.println("xChild: " + xChild.prettyString());
				// System.out.println("yChild: " + yChild.prettyString());

				if(!isSubtypeOfRec(xChild, yChild, R)){
					return false;
				}
			}
			if(y.isClosed()){
				if(!isSubSetOf(xLabels, yLabels) || x.isOpen()){
					return false;
				}
			}
			return true;
		}
		else if(t1 instanceof InlineType && t2 instanceof ChoiceType){
			InlineType x = (InlineType)t1;
			ChoiceType y = (ChoiceType)t2;

			for(InlineType choice : y.choices()){
				if(isSubtypeOfRec(x, choice, R)){
					return true;
				}
			}

			return false;
		}
		else if(t1 instanceof ChoiceType && t2 instanceof InlineType){
			ChoiceType x = (ChoiceType)t1;
			InlineType y = (InlineType)t2;

			for(InlineType choice : x.choices()){
				if(!isSubtypeOfRec(choice, y, R)){
					return false;
				}
			}

			return true;
		}
		else{ // both choice types
			ChoiceType x = (ChoiceType)t1;
			ChoiceType y = (ChoiceType)t2;

			for(InlineType xChoice : x.choices()){
				boolean xChoiceIsSubtype = false;

				for(InlineType yChoice : y.choices()){
					if(isSubtypeOfRec(xChoice, yChoice, R)){
						xChoiceIsSubtype = true;
						break;
					}
				}

				if(!xChoiceIsSubtype){
					return false;
				}
			}

			return true;
		}
	}

	private static boolean isInRelation(Type t1, Type t2, IdentityHashMap<Type, Type> R){
		return R.containsKey(t1) && R.get(t1) == t2;
	}

	/**
	 * @param t1
	 * @param t2
	 * @return true if the basic type of t1 is a subtype of the basic type of t2
	 */
	private static boolean basicSubtype(InlineType t1, InlineType t2){
		if(t2.basicType().nativeType().equals(NativeType.ANY)){ // everything is a subtype of ANY, so return true
			return true;
		}

		// otherwise, it can only be subtype if they are equal
		return t1.basicType().equals(t2.basicType());
	}

	private static boolean isSubSetOf(Set<String> s1, Set<String> s2){
		for(String s : s1){
			if(!s2.contains(s)){
				return false;
			}
		}
		
		return true;
	}

	private class Relation{
		private IdentityHashMap<HashSet<Type>, HashSet<Type>> rel;

		public Relation(){
			this.rel = new IdentityHashMap<>();
		}

		public boolean contains(HashSet<Type> set1, HashSet<Type> set2){
			return this.rel.containsKey(set1) && this.rel.get(set1) == set2;
		}

		public void insert(HashSet<Type> set1, HashSet<Type> set2){
			this.rel.put(set1, set2);
		}

		// returns the congrunece closure of this relation
		public Relation congruenceClosure(){
			return this;
		}
	}

	private boolean isSubtypeHKC(Type t1, Type t2){
		Relation rel = new Relation();
		LinkedList<Pair<HashSet<Type>, HashSet<Type>>> todo = new LinkedList<>();

		HashSet<Type> X = new HashSet<>();
		HashSet<Type> Y = new HashSet<>();
		X.add(t1);
		X.add(t2);
		todo.push(this.newPair(X, Y));

		while(!todo.isEmpty()){
			Pair<HashSet<Type>, HashSet<Type>> currPair = todo.pop();
			X = currPair.key();
			Y = currPair.value();
 
			if(rel.congruenceClosure().contains(X, Y)){ // we have processed this pair
				continue;
			}

			// TODO define what basic typing means for sets of nodes
			if(false){
				return false;
			}

			
		}

		return true;
	}

	private Pair<HashSet<Type>,HashSet<Type>> newPair(HashSet<Type> X, HashSet<Type> Y){
		return new Pair<HashSet<Type>, HashSet<Type>>(X, Y);
	}
}
