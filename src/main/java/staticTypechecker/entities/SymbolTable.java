package staticTypechecker.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.util.Range;
import staticTypechecker.typeStructures.InlineType;

public class SymbolTable {
	private HashMap<String, Symbol> table;
	
	public SymbolTable(){
		this.table = new HashMap<>();
		this.addBaseTypes();
	}

	private void addBaseTypes(){
		NativeType[] baseTypes = NativeType.values();
		for(NativeType t : baseTypes){
			InlineType typeStruct = new InlineType(BasicTypeDefinition.of(t), new Range(1, 1), null, false);

			this.table.put(t.id(), typeStruct);
		}
	}

	public boolean containsKey(String key){
		return this.table.containsKey(key);
	}

	public Symbol get(String key){
		return this.table.get(key);
	}

	public void put(String key, Symbol struct){
		this.table.put(key, struct);
	}

	public Collection<Entry<String, Symbol>> entrySet(){
		return this.table.entrySet();
	}

	public String toString(){
		return this.table.toString();
	}
}
