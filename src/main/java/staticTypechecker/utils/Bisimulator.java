package staticTypechecker.utils;

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
		IdentityHashMap<Type, Type> R = new IdentityHashMap<>();
		LinkedList<Pair<Type, Type>> todo = new LinkedList<>();

		todo.add(newPair(t1, t2));

		while(!todo.isEmpty()){
			Pair<Type, Type> currPair = todo.pop();
			Type currT1 = currPair.key();
			Type currT2 = currPair.value();

			if((R.containsKey(currT1)) && (R.get(currT1) == currT2)){ // check if we have already processed this pair
				continue;
			}			

			if(currT1.getClass() != currT2.getClass()){
				return false;
			}

			if(currT1 instanceof InlineType){
				InlineType X = (InlineType)currT1;
				InlineType Y = (InlineType)currT2;

				if(!basicSubtype(X, Y)){
					return false;
				}

				Set<String> xChildNames = X.children().keySet();
				Set<String> yChildNames = Y.children().keySet();

				if(X.isClosed() && Y.isClosed()){ // both are closed
					if(!xChildNames.equals(yChildNames)){ // check if sets of childNames are identical
						return false;
					}

					for(String childName : xChildNames){
						Type c1 = X.getChild(childName);
						Type c2 = Y.getChild(childName);
						todo.add(newPair(c1, c2));
					}
				}
				else if(Y.isOpen()){
					if(!isSuperSetOf(xChildNames, yChildNames)){
						return false;
					}

					HashSet<String> childNamesInCommon = intersection(xChildNames, yChildNames);
					HashSet<String> childNamesOnlyInX = subtract(xChildNames, yChildNames);

					for(String childName : childNamesInCommon){
						Type c1 = X.getChild(childName);
						Type c2 = Y.getChild(childName);
						todo.add(newPair(c1, c2));
					}

					for(String childName : childNamesOnlyInX){
						Type c1 = X.getChild(childName);
						Type c2 = Y.getChild("?");
						todo.add(newPair(c1, c2));
					}

					if(X.isOpen()){
						todo.add(newPair(X.getChild("?"), Y.getChild("?")));
					}
				}
				else{
					return false;
				}

				R.put(X, Y);
			}
			else{
				System.out.println("Choice types not supported for equivalence checking yet");
				return false;
			}
		}

		return true;
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

	/**
	 * Returns the resulting set from doing the intersection between s1 and s2
	 * @param s1
	 * @param s2
	 * @return
	 */
	private static HashSet<String> intersection(Set<String> s1, Set<String> s2){
		HashSet<String> result = new HashSet<>();

		for(String childName : s1){
			if(s2.contains(childName)){
				result.add(childName);
			}
		}

		return result;
	}

	/**
	 * Returns the resulting set of subtracting s2 from s1 (s1 \ s2)
	 * @param s1
	 * @param s2
	 * @return
	 */
	private static HashSet<String> subtract(Set<String> s1, Set<String> s2){
		HashSet<String> result = new HashSet<>();

		for(String childName : s1){
			if(!s2.contains(childName)){
				result.add(childName);
			}
		}

		return result;
	}

	/**
	 * Checks if s1 is a superset of s2, that is contains at least all elements of s2
	 * @param s1
	 * @param s2
	 * @return
	 */
	private static boolean isSuperSetOf(Set<String> s1, Set<String> s2){
		for(String s : s2){
			if(!s1.contains(s)){
				System.out.println(s + " is not in X");
				return false;
			}
		}
		
		return true;
	}

	
	
	public static boolean equivalent(Type t1, Type t2){
		return naive(t1, t2);
	}

	// TODO
	private static boolean HKC(Type t1, Type t2){
		IdentityHashMap<Type, Type> R = new IdentityHashMap<>();
		LinkedList<Pair<Type, Type>> todo = new LinkedList<>();
		
		todo.add(newPair(t1, t2));
		
		while(!todo.isEmpty()){
			Pair<Type, Type> currPair = todo.pop();
			Type X = currPair.key();
			Type Y = currPair.value();

			if(isInCongruenceClosure(currPair, R)){
				continue;
			}

			if(!checkBasicTypes(X, Y)){ 
				return false;
			}

			
			
			
		}

		return true;
	}

	/**
	 * TODO
	 */
	private static boolean isInCongruenceClosure(Pair<Type, Type> pair, IdentityHashMap<Type, Type> R){
		return false;
	}

	/**
	 * 
	 * @param X
	 * @param Y
	 * @return
	 */
	private static boolean checkBasicTypes(Type X, Type Y){
		// TODO, talk to Marco about what it means for two nodes, X and Y, o#(X) != o#(Y) (see paper)
		return false;
	}

	/**
	 * Implementation of the naïve algorithm for checking type equivalence
	 * @param t1
	 * @param t2
	 * @return
	 */
	private static boolean naive(Type t1, Type t2){
		// Set<Pair<Type, Type>> relation = Collections.newSetFromMap(new IdentityHashMap<>());
		IdentityHashMap<Type, Type> relation = new IdentityHashMap<>();
		LinkedList<Pair<Type, Type>> queue = new LinkedList<>();

		queue.add(newPair(t1, t2));

		while(!queue.isEmpty()){
			Pair<Type, Type> currPair = queue.pop();
			Type currT1 = currPair.key();
			Type currT2 = currPair.value();

			if((relation.containsKey(currT1)) && (relation.get(currT1) == currT2)){
				continue;
			}			

			if(currT1.getClass() != currT2.getClass()){
				return false;
			}

			if(currT1 instanceof InlineType){
				InlineType p1 = (InlineType)currT1;
				InlineType p2 = (InlineType)currT2;

				if(!p1.basicType().equals(p2.basicType())){
					return false;
				}

				if(p1.children().size() != p2.children().size()){
					return false;
				}

				Set<String> childNames = p1.children().keySet();

				for(String childName : childNames){
					Type c1 = p1.getChild(childName);
					Type c2 = p2.getChild(childName);

					queue.add(newPair(c1, c2));
				}
				
				relation.put(p1, p2);
			}
			else{
				System.out.println("Choice types not supported for equivalence checking yet");
				return false;
			}
		}

		return true;
	}

	private static Pair<Type, Type> newPair(Type t1, Type t2){
		return new Pair<Type, Type>(t1, t2);
	}

	private static void printRelation(Set<Pair<Type, Type>> rel){
		for(Pair<Type, Type> p : rel){
			System.out.println(p.key() + " <--> " + p.value());
		}
	}

	private static void printQueue(LinkedList<Pair<Type, Type>> queue){
		System.out.println("queue:");
		for(Pair<Type, Type> p : queue){
			System.out.print("(" + p.key().prettyString() + ", " + p.value().prettyString() + "); ");
		}
		System.out.println();
	}

	private static void printPair(Pair<Type, Type> p){
		System.out.println("(" + p.key().prettyString() + ", " + p.value().prettyString() + ")");
	}
}
