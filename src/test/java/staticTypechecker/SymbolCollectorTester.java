package staticTypechecker;

import java.util.List;
import java.util.Map.Entry;

import staticTypechecker.entities.SymbolTable.Pair;
import staticTypechecker.entities.Symbol;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.Module;
import staticTypechecker.utils.ModuleHandler;

public class SymbolCollectorTester {
	public static boolean run(){
		String moduleName = AppTest.BASE_PATH + "testFilesForSymbolChecker/main.ol";
		List<Module> modules = ModuleHandler.loadModule(moduleName);
		AppTest.runProcessors(modules, 0);

		SymbolTable result = modules.get(0).symbols();
		SymbolTable target = new SymbolTable();

		target.put("penguin", SymbolTable.newPair(SymbolType.TYPE, null));
		target.put("EmbedMe", SymbolTable.newPair(SymbolType.SERVICE, null));
		target.put("MyInterface", SymbolTable.newPair(SymbolType.INTERFACE, null));
		target.put("helloReqRes", SymbolTable.newPair(SymbolType.OPERATION, null));
		target.put("helloOneway", SymbolTable.newPair(SymbolType.OPERATION, null));
		target.put("Y", SymbolTable.newPair(SymbolType.TYPE, null));
		target.put("X", SymbolTable.newPair(SymbolType.TYPE, null));
		target.put("paramType", SymbolTable.newPair(SymbolType.TYPE, null));
		target.put("MyService", SymbolTable.newPair(SymbolType.SERVICE, null));
		target.put("param", SymbolTable.newPair(SymbolType.TYPE, null));
		target.put("inputPort", SymbolTable.newPair(SymbolType.INPUT_PORT, null));
		target.put("outputPort", SymbolTable.newPair(SymbolType.OUTPUT_PORT, null));
		target.put("embedMe", SymbolTable.newPair(SymbolType.OUTPUT_PORT, null));

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
