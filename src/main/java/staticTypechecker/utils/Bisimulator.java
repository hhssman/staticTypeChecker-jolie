package staticTypechecker.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

import jolie.util.Pair;
import staticTypechecker.typeStructures.ChoiceType;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;

public class Bisimulator {

	
	public static boolean equivalent(Type t1, Type t2){
		return naive(t1, t2);
	}

	// TODO
	private static boolean HKC(Type t1, Type t2){
		IdentityHashMap<Type, Type> R = new IdentityHashMap<>();
		LinkedList<Pair<Type, Type>> todo = new LinkedList<>();
		
		while(!todo.isEmpty()){
			Pair<Type, Type> currPair = todo.pop();
			Type X = currPair.key();
			Type Y = currPair.value();

			if(R.containsKey(X) && R.get(X) == Y){
				continue;
			}

			if(X.getClass() != Y.getClass()){
				return false;
			}



		}

		return true;
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
				ChoiceType p1 = (ChoiceType)currT1;
				ChoiceType p2 = (ChoiceType)currT2;
				
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
