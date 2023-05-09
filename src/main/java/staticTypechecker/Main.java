package staticTypechecker;

import java.util.Map.Entry;
import java.util.HashMap;

import jolie.lang.NativeType;
import staticTypechecker.utils.ModuleHandler;
import staticTypechecker.entities.SymbolTable.Pair;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Symbol;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.faults.FaultHandler;
import staticTypechecker.faults.WarningHandler;
import staticTypechecker.entities.Type;
import staticTypechecker.visitors.BehaviorProcessor;
import staticTypechecker.visitors.InputPortProcessor;
import staticTypechecker.visitors.InterfaceProcessor;
import staticTypechecker.visitors.OutputPortProcessor;
import staticTypechecker.visitors.SymbolCollector;
import staticTypechecker.visitors.TypeProcessor;

public class Main {

	public static void main( String[] args ) {
		// set the default value of the typehints to assertions
		if(System.getProperty("typehint") == null){
			System.setProperty("typehint", "assertions");
		}

		// stage 0: parse the modules
		String moduleName = args[0];
		ModuleHandler.loadModule(moduleName);
		// System.out.println("loaded modules: " + ModuleHandler.modules());

		// stage 1: discover symbols in all modules
		System.out.println("STAGE 1: discover symbols");
		
		SymbolCollector sCollector = new SymbolCollector();
		ModuleHandler.runVisitor(sCollector);

		// printAllSymbols();

		// stage 2: process type definitions in all modules
		System.out.println("STAGE 2: process types");
		TypeProcessor tProcessor = new TypeProcessor();
		ModuleHandler.runVisitor(tProcessor);

		// printAllSymbols();

		// stage 3: process interfaces in all modules
		System.out.println("STAGE 3: process interfaces");
		
		InterfaceProcessor iProcessor = new InterfaceProcessor();
		ModuleHandler.runVisitor(iProcessor);

		printAllSymbols();

		// stage 4: process service-parameters and input ports in all services
		System.out.println("STAGE 4: process input ports and service parameters");
		
		InputPortProcessor ipProcessor = new InputPortProcessor();
		ModuleHandler.runVisitor(ipProcessor);

		// printAllSymbols();

		// stage 5: process output ports and embeddings in all services
		System.out.println("STAGE 5: process output ports and embeddings");

		OutputPortProcessor opProcessor = new OutputPortProcessor();
		ModuleHandler.runVisitor(opProcessor);

		// printAllSymbols();
		
		// stage 6: process service behaviors
		System.out.println("STAGE 6: process behaviors\n");

		HashMap<String, Type> trees = new HashMap<>();
		
		ModuleHandler.modules().values().forEach(m -> {
			BehaviorProcessor bProcessor = new BehaviorProcessor(m.fullPath().equals(args[0]));
			trees.put(m.name(), bProcessor.process(m));
		});

		// printAllSymbols(trees);

		FaultHandler.printFaults();
		WarningHandler.printWarnings();
	}

	private static void printAllSymbols(){
		System.out.println("-----------------------------------------");
		for(Module m : ModuleHandler.modules().values()){
			System.out.println("Module: " + m.name());

			for(Entry<Pair<String, SymbolType>, Symbol> symbol : m.symbols().entrySet()){
				if(!NativeType.isNativeTypeKeyword(symbol.getKey().key())){ // we dont want to print the base types
					if(symbol.getValue() != null && symbol.getValue() != null){ // the symbol object have been initialized and can thus be pretty printed
						System.out.println("\n" + symbol.getKey() + ":\n" + symbol.getValue().prettyString());
					}
					else{
						System.out.println("\n" + symbol.getKey() + ":\n" + null);
					}
				}
			}

			System.out.println("______________");
		}
		System.out.println("-----------------------------------------");
	}

	private static void printAllSymbols(HashMap<String, Type> trees){
		System.out.println("-----------------------------------------");
		for(Module m : ModuleHandler.modules().values()){
			System.out.println("Module: " + m.name());

			for(Entry<Pair<String, SymbolType>, Symbol> symbol : m.symbols().entrySet()){
				if(!NativeType.isNativeTypeKeyword(symbol.getKey().key())){ // we dont want to print the base types
					if(symbol.getValue() != null && symbol.getValue() != null){ // the symbol object have been initialized and can thus be pretty printed
						System.out.println("\n" + symbol.getKey() + ":\n" + symbol.getValue().prettyString());
					}
					else{
						System.out.println("\n" + symbol.getKey() + ":\n" + null);
					}
				}
			}

			System.out.println(trees.get(m.name()).prettyString());

			System.out.println("______________");
		}
		System.out.println("-----------------------------------------");
	}
}
