package staticTypechecker;

import java.util.HashSet;
import java.util.List;

import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.Type;
import staticTypechecker.entities.Operation.OperationType;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.entities.ChoiceType;
import staticTypechecker.entities.InlineType;
import staticTypechecker.entities.Interface;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Operation;
import staticTypechecker.entities.OutputPort;
import staticTypechecker.utils.ModuleHandler;

public class OutputPortProcessorTester {
	public static boolean run(){
		String moduleName = AppTest.BASE_PATH + "testFilesForOutputPortProcessor/main.ol";
		List<Module> modules = ModuleHandler.loadModule(moduleName);
		AppTest.runProcessors(modules, 4);

		SymbolTable result = ModuleHandler.get(moduleName).symbols();
		SymbolTable target = new SymbolTable();

		HashSet<Interface> interfacesOutputPort1 = new HashSet<>();
		Interface i1 = new Interface("MyInterface1");
		i1.addOperation(new Operation("MyReqRes", Type.INT(), Type.STRING(), OperationType.REQRES));
		i1.addOperation(new Operation("MyOneWay", Type.BOOL(), null, OperationType.ONEWAY));
		interfacesOutputPort1.add(i1);
		OutputPort OutputPort1 = new OutputPort("OutputPort1", "socket://localhost:8080", "sodep", interfacesOutputPort1);

		target.put(SymbolTable.newPair("OutputPort1", SymbolType.OUTPUT_PORT), OutputPort1);

		HashSet<Interface> interfacesOutputPort2 = new HashSet<>();
		Interface i2 = new Interface("MyInterface2");
		InlineType A = Type.ANY().addChild("x", Type.STRING());
		A.addChildUnsafe("y", A);
		ChoiceType B = new ChoiceType().addChoice(Type.INT()).addChoice(Type.STRING()); 
		i2.addOperation(new Operation("MyReqRes2", A, B, OperationType.REQRES));
		i2.addOperation(new Operation("MyOneWay2", Type.INT(), null, OperationType.ONEWAY));
		interfacesOutputPort2.add(i2);
		OutputPort OutputPort2 = new OutputPort("OutputPort2", "socket://localhost:8081", "http", interfacesOutputPort2);

		target.put(SymbolTable.newPair("OutputPort2", SymbolType.OUTPUT_PORT), OutputPort2);

		HashSet<Interface> interfacesimport = new HashSet<>();
		Interface i3 = new Interface("ImportedInterface1");
		interfacesimport.add(i3);
		OutputPort imported = new OutputPort("i3", "socket://localhost:8082", "sodep", interfacesimport);

		target.put(SymbolTable.newPair("i3", SymbolType.OUTPUT_PORT), imported);

		return AppTest.testSymbolsForEquality(result, target);
	}
}
