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

	public enum SymbolType{
		TYPE, INTERFACE, OPERATION, SERVICE, INPUT_PORT, OUTPUT_PORT
	}
}
