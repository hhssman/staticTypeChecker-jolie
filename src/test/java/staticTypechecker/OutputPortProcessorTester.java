package staticTypechecker;

import java.util.ArrayList;
import java.util.List;

import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.OutputPort;
import staticTypechecker.entities.Symbol;
import staticTypechecker.utils.ModuleHandler;

public class OutputPortProcessorTester {
	public static boolean run(){
		String moduleName = AppTest.BASE_PATH + "testFilesForOutputPortProcessor/main.ol";
		List<Module> modules = ModuleHandler.loadModule(moduleName);
		AppTest.runProcessors(modules, 4);

		SymbolTable result = ModuleHandler.get(moduleName).symbols();
		SymbolTable target = new SymbolTable();

		ArrayList<String> interfacesOutputPort1 = new ArrayList<>();
		interfacesOutputPort1.add("MyInterface1");
		OutputPort OutputPort1 = new OutputPort("OutputPort1", "socket://localhost:8080", "sodep", interfacesOutputPort1);

		target.put("OutputPort1", Symbol.newPair(SymbolType.OUTPUT_PORT, OutputPort1));

		ArrayList<String> interfacesOutputPort2 = new ArrayList<>();
		interfacesOutputPort2.add("MyInterface2");
		OutputPort OutputPort2 = new OutputPort("OutputPort2", "socket://localhost:8081", "http", interfacesOutputPort2);

		target.put("OutputPort2", Symbol.newPair(SymbolType.OUTPUT_PORT, OutputPort2));

		ArrayList<String> interfacesi1 = new ArrayList<>();
		interfacesi1.add("ImportedInterface1");
		OutputPort i1 = new OutputPort("i1", "socket://localhost:8082", "sodep", interfacesi1);

		target.put("i1", Symbol.newPair(SymbolType.OUTPUT_PORT, i1));

		return AppTest.testSymbolsForEquality(result, target);
	}
}
