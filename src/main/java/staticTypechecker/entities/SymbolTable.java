package staticTypechecker.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.util.Pair;
import jolie.util.Range;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.entities.InlineType;

/**
 * Represents the symbol table for a module
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class SymbolTable {
	private HashMap<String, Pair<SymbolType, Symbol>> table; // maps the name of the symbol to a pair containing the type of the symbol and the symbol itself
	
	public SymbolTable(){
		this.table = new HashMap<>();
		this.addBaseTypes();
	}

	/**
	 * Adds the basic types of Jolie to this symbol table, i.e. int, string, boolean etc.
	 */
	private void addBaseTypes(){
		NativeType[] baseTypes = NativeType.values();
		for(NativeType t : baseTypes){
			InlineType typeStruct = new InlineType(BasicTypeDefinition.of(t), new Range(1, 1), null, false);

			this.table.put(t.id(), new Pair<SymbolType, Symbol>(SymbolType.TYPE, typeStruct));
		}
	}

	public boolean containsKey(String key){
		return this.table.containsKey(key);
	}

	public Symbol get(String key){
		if(!this.containsKey(key)){
			return null;
		}

		return this.table.get(key).value();
	}

	public SymbolType getType(String key){
		if(!this.containsKey(key)){
			return null;
		}

		return this.table.get(key).key();
	}

	public Pair<SymbolType, Symbol> getPair(String key){
		if(!this.containsKey(key)){
			return null;
		}
		
		return this.table.get(key);
	}

	public void put(String key, Pair<SymbolType, Symbol> struct){
		this.table.put(key, struct);
	}

	public Collection<Entry<String, Pair<SymbolType, Symbol>>> entrySet(){
		return this.table.entrySet();
	}

	public String toString(){
		String res = "";
		for(Entry<String, Pair<SymbolType,Symbol>> ent : this.table.entrySet()){
			res += ent.getKey() + " (" + ent.getValue().key() + ") = " + ent.getValue().value() + "\n";
		}

		return res;
	}
}
