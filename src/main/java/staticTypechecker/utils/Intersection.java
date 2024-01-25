package staticTypechecker.utils;

import java.util.HashMap;

import staticTypechecker.entities.Type;
import staticTypechecker.entities.SymbolTable.Pair;

public class Intersection {
    
	public static Type intersection(Type t1, Type t2) {
		return intersection(t1, t2, new HashMap<>());
	}

	private static Type intersection(Type t1, Type t2, HashMap<Pair<Type, Type>, Type> seen) {
		return null;
	} 
}
