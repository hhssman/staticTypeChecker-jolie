package staticTypechecker;

import java.util.ArrayList;
import java.util.List;

import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.entities.InputPort;
import staticTypechecker.entities.Module;
import staticTypechecker.utils.ModuleHandler;

public class InputPortProcessorTester {
	public static boolean run(){
		String moduleName = AppTest.BASE_PATH + "testFilesForInputPortProcessor/main.ol";
		List<Module> modules = ModuleHandler.loadModule(moduleName);
		AppTest.runProcessors(modules, 3);

		SymbolTable result = ModuleHandler.get(moduleName).symbols();
		SymbolTable target = new SymbolTable();

		ArrayList<String> interfacesInputPort1 = new ArrayList<>();
		interfacesInputPort1.add("MyInterface1");
		InputPort InputPort1 = new InputPort("InputPort1", "socket://localhost:8080", "sodep", interfacesInputPort1);

		target.put("InputPort1", SymbolTable.newPair(SymbolType.INPUT_PORT, InputPort1));

		ArrayList<String> interfacesInputPort2 = new ArrayList<>();
		interfacesInputPort2.add("MyInterface2");
		InputPort InputPort2 = new InputPort("InputPort2", "socket://localhost:8082", "http", interfacesInputPort2);

		target.put("InputPort2", SymbolTable.newPair(SymbolType.INPUT_PORT, InputPort2));

		return AppTest.testSymbolsForEquality(result, target);
	}
}
