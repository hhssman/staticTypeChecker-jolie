package staticTypechecker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import staticTypechecker.typeStructures.TypeStructure;

public class SymbolTable_new {
	private HashMap<String, TypeStructure> table;
	
	public SymbolTable_new(){
		this.table = new HashMap<>();
	}

	public boolean containsKey(String key){
		return this.table.containsKey(key);
	}

	public TypeStructure get(String key){
		return this.table.get(key);
	}

	public void put(String key, TypeStructure struct){
		this.table.put(key, struct);
	}

	public Collection<Entry<String, TypeStructure>> entrySet(){
		return this.table.entrySet();
	}
}
