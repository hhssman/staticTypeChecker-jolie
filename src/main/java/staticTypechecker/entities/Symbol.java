package staticTypechecker.entities;

import jolie.util.Pair;

/**
 * Represents any entity in Jolie, such as a type name, service name, port name etc.
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public interface Symbol {
	public String prettyString();

	public static Pair<SymbolType, Symbol> newPair(SymbolType type, Symbol symbol){
		return new Pair<SymbolType, Symbol>(type, symbol);
	}

	public static boolean equals(Symbol A, Symbol B){
		if(A == B){
			return true;
		}
		
		if(A instanceof InputPort){
			return ((InputPort)A).equals(B);
		}
		if(A instanceof Interface){
			return ((Interface)A).equals(B);
		}
		if(A instanceof Operation){
			return ((Operation)A).equals(B);
		}
		if(A instanceof OutputPort){
			return ((OutputPort)A).equals(B);
		}
		if(A instanceof Service){
			return ((Service)A).equals(B);
		}
		if(A instanceof Type){
			return ((Type)A).equals(B);
		}

		return false;
	}

	public enum SymbolType{
		TYPE, INTERFACE, OPERATION, SERVICE, INPUT_PORT, OUTPUT_PORT
	}
}
