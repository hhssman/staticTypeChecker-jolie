package staticTypechecker;

import java.util.List;
import java.util.Map.Entry;

import jolie.util.Pair;
import staticTypechecker.utils.ModuleHandler;
import staticTypechecker.entities.Operation;
import staticTypechecker.entities.Symbol;
import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.Operation.OperationType;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.entities.Interface;
import staticTypechecker.entities.Module;

public class InterfaceProcessorTester {
	public static boolean run(){
		String moduleName = AppTest.BASE_PATH + "testFilesForInterfaceProcessor/main.ol";
		List<Module> modules = ModuleHandler.loadModule(moduleName);
		AppTest.runProcessors(modules, 2);

		SymbolTable result = ModuleHandler.get(moduleName).symbols();
		SymbolTable target = new SymbolTable();

		// manually create the interface objects

		// MyInterface
		Interface myInterface = new Interface("MyInterface");
		myInterface.addOperation("myReqRes", new Operation("myReqRes", "int", "reqResReturn", OperationType.REQRES));
		myInterface.addOperation("myOneWay", new Operation("myOneWay", "string", null, OperationType.ONEWAY));
		myInterface.addOperation("mySecondOneWay", new Operation("mySecondOneWay", "A", null, OperationType.ONEWAY));

		target.put("MyInterface", Symbol.newPair(SymbolType.INTERFACE, myInterface));
		
		// ImportedInterface
		Interface importedInterface = new Interface("ImportedInterface");
		importedInterface.addOperation("importedReqRes", new Operation("importedReqRes", "A", "B", OperationType.REQRES));
		importedInterface.addOperation("importedOneWay", new Operation("importedOneWay", "int", null, OperationType.ONEWAY));

		target.put("ImportedInterface", Symbol.newPair(SymbolType.INTERFACE, importedInterface));


		for(Entry<String, Pair<SymbolType, Symbol>> ent : target.entrySet()){
			String symbolName = ent.getKey();
			Symbol targetSymbol = ent.getValue().value();
			Symbol resultSymbol = result.get(symbolName);

			if(!Symbol.equals(targetSymbol, resultSymbol)){
				System.out.println("FAIL on symbol " + symbolName + ":\n" + resultSymbol.prettyString() + "\n\nis not equal to\n\n" + resultSymbol.prettyString());
				return false;
			}
		}

		
		return true;
	}
}
