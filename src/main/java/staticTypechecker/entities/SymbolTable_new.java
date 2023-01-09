package staticTypechecker.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.util.Range;
import staticTypechecker.typeStructures.TypeInlineStructure;

public class SymbolTable_new {
	private HashMap<String, Symbol> table;
	
	public SymbolTable_new(){
		this.table = new HashMap<>();
		this.addBaseTypes();
	}

	private void addBaseTypes(){
		NativeType[] baseTypes = NativeType.values();
		for(NativeType t : baseTypes){
			TypeInlineStructure typeStruct = new TypeInlineStructure(BasicTypeDefinition.of(t), new Range(1, 1), null);

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
}
