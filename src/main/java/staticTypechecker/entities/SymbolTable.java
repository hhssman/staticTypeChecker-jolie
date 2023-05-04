package staticTypechecker.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.util.Range;
import staticTypechecker.entities.Symbol.SymbolType;

/**
 * Represents the symbol table for a module
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class SymbolTable {
	private HashMap<Pair<String, SymbolType>, Symbol> table; // maps the name of the symbol to a pair containing the type of the symbol and the symbol itself
	
	public SymbolTable(){
		this.table = new HashMap<>();
		this.addBasicTypes();
	}

	/**
	 * Adds the basic types of Jolie to this symbol table, i.e. int, string, boolean etc.
	 */
	private void addBasicTypes(){
		NativeType[] baseTypes = NativeType.values();
		for(NativeType t : baseTypes){
			InlineType typeStruct = new InlineType(BasicTypeDefinition.of(t), new Range(1, 1), null, false);

			this.table.put(SymbolTable.newPair(t.id(), SymbolType.TYPE), typeStruct);
		}
	}

	public boolean containsKey(String key){
		return this.table.containsKey(new Pair<>(key, null));
	}

	public boolean containsKey(Pair<String, SymbolType> key){
		return this.table.containsKey(key);
	}

	public Symbol get(String key, SymbolType type){
		Pair<String, SymbolType> p = new Pair<>(key, type);

		if(!this.containsKey(p)){
			return null;
		}

		return this.table.get(p);
	}

	public Symbol get(Pair<String, SymbolType> key){
		if(!this.containsKey(key)){
			return null;
		}

		return this.table.get(key);
	}

	public List<Pair<Symbol, SymbolType>> getAllSymbols(String name){
		ArrayList<Pair<Symbol, SymbolType>> ret = new ArrayList<>();

		for(Entry<Pair<String, SymbolType>, Symbol> ent : this.entrySet()){
			String symbolName = ent.getKey().key();
			
			if(symbolName.equals(name)){
				SymbolType type = ent.getKey().value();
				Symbol symbol = ent.getValue();
				ret.add(SymbolTable.newPair(symbol, type));
			}
		}

		return ret;
	}

	// public SymbolType getType(String key){
	// 	Pair<String, SymbolType> p = new Pair<>(key, null);

	// 	if(!this.containsKey(p)){
	// 		return null;
	// 	}

	// 	return this.table.get(p).key();
	// }

	// public Pair<SymbolType, Symbol> getPair(String key){
	// 	Pair<String, SymbolType> p = new Pair<>(key, null);

	// 	if(!this.containsKey(p)){
	// 		return null;
	// 	}
	// 	return this.table.get(p);
	// }

	// public Pair<SymbolType, Symbol> getPair(String name, SymbolType type){
	// 	Pair<String, SymbolType> key = SymbolTable.newPair(name, type);

	// 	if(!this.containsKey(key)){
	// 		return null;
	// 	}

	// 	return this.table.get(key);
	// }



	// public void put(String key, Pair<SymbolType, Symbol> struct){
	// 	this.table.put(new Pair<>(key, null), struct);
	// }

	public void put(Pair<String, SymbolType> key, Symbol symbol){
		this.table.put(key, symbol);
	}

	// filters out the SymbolTypes of the keys
	public Collection<Entry<Pair<String, SymbolType>, Symbol>> entrySet(){
		return this.table.entrySet();
	}

	// public String toString(){
	// 	String res = "";
	// 	for(Entry<String, Pair<SymbolType,Symbol>> ent : this.entrySet()){
	// 		res += ent.getKey() + " (" + ent.getValue().key() + ") = " + ent.getValue().value() + " (" + System.identityHashCode(ent.getValue()) + ")\n";
	// 	}

	// 	return res;
	// }

	public String toString(){
		String res = "";
		for(Entry<Pair<String, SymbolType>, Symbol> ent : this.table.entrySet()){
			res += ent.getKey() + " = " + ent.getValue() + "\n";
		}

		return res;
	}

	public static Pair<String, SymbolType> newPair(String key, SymbolType value){
		return new Pair<String, SymbolType>(key, value);
	}

	public static Pair<Symbol, SymbolType> newPair(Symbol key, SymbolType value){
		return new Pair<Symbol, SymbolType>(key, value);
	}

	public static class Pair<K, V> {
		private K key;
		private V value;
		
		public Pair(K key, V value){
			this.key = key;
			this.value = value;
		}
	
		public K key(){
			return this.key;
		}
	
		public V value(){
			return this.value;
		}
	
		public int hashCode(){
			int hashcode = 0;
			if(this.key != null){
				hashcode += this.key.hashCode();
			}
			if(this.value != null){
				hashcode += this.value.hashCode();
			}
			return hashcode;
		}
	
		public boolean equals(Object other){
			if(this == other){
				return true;
			}
	
			if(!(other instanceof Pair)){
				// System.out.println("not of instance pair");
				return false;
			}
	
			Pair<?,?> parsed = (Pair<?,?>)other;

			if(!this.key.equals(parsed.key)){
				return false;
			}

			if(this.value.equals(SymbolType.ANY) || parsed.value.equals(SymbolType.ANY)){
				return true;
			}

			return this.value.equals(parsed.value);
		}

		public String toString(){
			return "(" + (this.key != null ? this.key.toString() : "null") + ", " + (this.value != null ? this.value.toString() : "null") + ")";
		}
	}
}
