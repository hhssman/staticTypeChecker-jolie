package staticTypechecker.entities;

import jolie.util.Pair;

public interface Symbol {
	public String prettyString();

	public static Pair<SymbolType, Symbol> newPair(SymbolType type, Symbol symbol){
		return new Pair<SymbolType, Symbol>(type, symbol);
	}

	public enum SymbolType{
		TYPE, INTERFACE, OPERATION, SERVICE, INPUT_PORT, OUTPUT_PORT
	}
}
