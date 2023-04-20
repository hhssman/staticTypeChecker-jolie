package staticTypechecker;

import java.util.List;
import java.util.Map.Entry;

import jolie.util.Pair;
import staticTypechecker.entities.Symbol;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.ModuleHandler;
import staticTypechecker.visitors.SymbolCollector;

public class TestSymbolChecking {
	public static boolean test(){
		String moduleName = AppTest.BASE_PATH + "testFilesForSymbolChecking/main.ol";
		List<Module> modules = ModuleHandler.loadModule(moduleName);

		SymbolCollector sCollector = new SymbolCollector();
		for(Module m : modules){
			sCollector.process(m, false);
		}
		for(Module m : modules){
			sCollector.process(m, true);
		}

		SymbolTable result = modules.get(0).symbols();
		SymbolTable target = new SymbolTable();

		target.put("penguin", new Pair<SymbolType, Symbol>(SymbolType.TYPE, null));
		target.put("EmbedMe", new Pair<SymbolType, Symbol>(SymbolType.SERVICE, null));
		target.put("MyInterface", new Pair<SymbolType, Symbol>(SymbolType.INTERFACE, null));
		target.put("helloReqRes", new Pair<SymbolType, Symbol>(SymbolType.OPERATION, null));
		target.put("helloOneway", new Pair<SymbolType, Symbol>(SymbolType.OPERATION, null));
		target.put("Y", new Pair<SymbolType, Symbol>(SymbolType.TYPE, null));
		target.put("X", new Pair<SymbolType, Symbol>(SymbolType.TYPE, null));
		target.put("paramType", new Pair<SymbolType, Symbol>(SymbolType.TYPE, null));
		target.put("MyService", new Pair<SymbolType, Symbol>(SymbolType.SERVICE, null));
		target.put("param", new Pair<SymbolType, Symbol>(SymbolType.TYPE, null));
		target.put("inputPort", new Pair<SymbolType, Symbol>(SymbolType.INPUT_PORT, null));
		target.put("outputPort", new Pair<SymbolType, Symbol>(SymbolType.OUTPUT_PORT, null));
		target.put("embedMe", new Pair<SymbolType, Symbol>(SymbolType.OUTPUT_PORT, null));

		for(Entry<String, Pair<SymbolType, Symbol>> ent : target.entrySet()){
			String key = ent.getKey();

			if(!result.containsKey(key)){
				System.out.println("symbol table does not include " + key);
				return false;
			}

			Pair<SymbolType, Symbol> p1 = result.getPair(key);
			Pair<SymbolType, Symbol> p2 = target.getPair(key);

			if(p1.key() != p2.key()){
				System.out.println(p1.key() + " != " + p2.key());
				return false;
			}
		}

		return true;
	}
}
