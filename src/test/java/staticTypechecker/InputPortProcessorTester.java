package staticTypechecker;

import java.util.List;

import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.Module;
import staticTypechecker.utils.ModuleHandler;

public class InputPortProcessorTester {
	public static boolean run(){
		String moduleName = AppTest.BASE_PATH + "testFilesForInputPortProcessor/main.ol";
		List<Module> modules = ModuleHandler.loadModule(moduleName);
		AppTest.runProcessors(modules, 3);

		SymbolTable result = ModuleHandler.get(moduleName).symbols();
		SymbolTable target = new SymbolTable();

		

		return AppTest.testSymbolsForEquality(result, target);
	}
}
