package staticTypechecker;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashMap;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.Program;
import staticTypechecker.entities.ModuleHandler;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Symbol;
import staticTypechecker.typeStructures.TypeInlineStructure;
import staticTypechecker.typeStructures.TypeStructure;
import staticTypechecker.visitors.BehaviorProcessor;
import staticTypechecker.visitors.InputPortProcessor;
import staticTypechecker.visitors.InterfaceProcessor;
import staticTypechecker.visitors.OutputPortProcessor;
import staticTypechecker.visitors.SymbolCollector;
import staticTypechecker.visitors.TypeChecker;
import staticTypechecker.visitors.TypeProcessor;


public class Main {

	public static void main( String[] args ) {
		
		
		// stage 0: parse the modules
		ModuleHandler.loadModules(args);
	
		// stage 1: discover symbols in all modules
		System.out.println("STAGE 1: discover symbols");
		
		SymbolCollector sCollector = new SymbolCollector();
		ArrayList<Module> failedModules = new ArrayList<>();

		ModuleHandler.modules().values().forEach(m -> {
			try{
				sCollector.collect(m);
			}
			catch(NullPointerException e){
				failedModules.add(m);
			}
		});
		
		for(Module failedMod : failedModules){
			sCollector.collect(failedMod);
		}

		// printAllSymbols();

		// stage 2: process type definitions in all modules
		System.out.println("STAGE 2: process types");

		TypeProcessor tProcessor = new TypeProcessor();
		
		// run through all types and make base structures
		// System.out.println("Creating bases...");
		ModuleHandler.modules().values().forEach(m -> {
			tProcessor.process(m);
		});

		// printAllSymbols();

		// run through them again and actually create the trees
		// System.out.println("Finishing bases...");
		ModuleHandler.modules().values().forEach(m -> {
			tProcessor.process(m);
		});

		// printAllSymbols();

		// stage 3: process interfaces in all modules
		System.out.println("STAGE 3: process interfaces");
		
		InterfaceProcessor iProcessor = new InterfaceProcessor();
		ModuleHandler.modules().values().forEach(m -> {
			iProcessor.process(m);
		});

		// printAllSymbols();

		// stage 4: process service-parameters and input ports in all services
		System.out.println("STAGE 4: process input ports and service parameters");
		
		InputPortProcessor ipProcessor = new InputPortProcessor();
		ModuleHandler.modules().values().forEach(m -> {
			ipProcessor.process(m);
		});

		// printAllSymbols();

		// stage 5: process output ports and embeddings in all services
		System.out.println("STAGE 5: process output ports and embeddings");

		OutputPortProcessor opProcessor = new OutputPortProcessor();
		ModuleHandler.modules().values().forEach(m -> {
			opProcessor.process(m);
		});

		// printAllSymbols();
		
		// stage 6: process service behaviors
		System.out.println("STAGE 6: process behaviors");

		BehaviorProcessor bProcessor = new BehaviorProcessor();
		HashMap<String, TypeInlineStructure> trees = new HashMap<>();

		ModuleHandler.modules().values().forEach(m -> {
			TypeInlineStructure tree = new TypeInlineStructure(null, null, null);
			trees.put(m.name(), tree);
			bProcessor.process(m, tree);
		});

		// printAllSymbols();
	}

	private static void printAllSymbols(){
		System.out.println("-----------------------------------------");
		for(Module m : ModuleHandler.modules().values()){
			System.out.println("Module: " + m.name());

			for(Entry<String, Symbol> symbol : m.symbols().entrySet()){
				if(!NativeType.isNativeTypeKeyword(symbol.getKey())){ // we dont want to print the base types
					if(symbol.getValue() != null){ // the symbol object have been initialized and can thus be pretty printed
						System.out.println("\n" + symbol.getKey() + ": " + symbol.getValue().prettyString());
					}
					else{
						System.out.println("\n" + symbol.getKey() + ": " + null);
					}
				}
			}

			System.out.println("______________");
		}
		System.out.println("-----------------------------------------");
	}

	private static void typeCheck(Program p){
		TypeChecker v = new TypeChecker();
		v.visit(p, null);
	}

	private static void printTable(HashMap<String, TypeStructure> symbols){
		System.out.println("--------------------------------");
		for(Entry<String, TypeStructure> e : symbols.entrySet()){
			System.out.println(e.getKey() + " = " + e.getValue());
		}
		System.out.println("--------------------------------");
	}

	private static void prettyPrintTable(HashMap<String, TypeStructure> symbols){
		System.out.println("--------------------------------");
		for(Entry<String, TypeStructure> e : symbols.entrySet()){
			System.out.println(e.getKey() + " = " + e.getValue().prettyString());
			System.out.println();
		}
		System.out.println("--------------------------------");
	}
}
