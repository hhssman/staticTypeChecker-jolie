package staticTypechecker;

import java.util.Map.Entry;
import java.util.HashMap;

import jolie.lang.parse.ast.Program;
import jolie.lang.parse.ast.types.TypeDefinition;
import jolie.lang.parse.ast.types.TypeDefinitionLink;
import jolie.lang.parse.ast.types.TypeInlineDefinition;
import staticTypechecker.slicerLib.JoliePrettyPrinter;
import staticTypechecker.typeStructures.TypeInlineStructure;
import staticTypechecker.typeStructures.TypeStructure;


public class Main {

	public static void main( String[] args ) {
		
		
		// stage 0: parse the modules
		ModuleHandler.loadModules(args);
		
		// TMP TESTER
		// ModuleHandler.modules().values().forEach(module -> {
		// 	module.program().children().forEach(child -> {
		// 		if(child instanceof TypeInlineDefinition){
		// 			TypeInlineDefinition tmp = (TypeInlineDefinition)child;
		// 			for(Entry<String, TypeDefinition> ent : tmp.subTypes()){
		// 				if(ent.getValue() instanceof TypeDefinitionLink){
		// 					TypeDefinitionLink tmp2 = (TypeDefinitionLink)ent.getValue();
		// 					System.out.println(tmp2.linkedType());
		// 					System.out.println(((TypeInlineDefinition)tmp2.linkedType()).subTypes());
		// 				}
		// 			}

		// 			// WHAT WAS I DOING? Trying to figure out how the type stores the structure of the imported type
		// 		}
		// 	});
		// });

		// // stage 1: discover symbols in all modules
		System.out.println("STAGE 1: discover symbols");
		SymbolCollector sCollector = new SymbolCollector();
		
		ModuleHandler.modules().values().forEach(m -> {
			sCollector.collect(m);
		});

		printAllSymbols();

		// stage 2: process type definitions in all modules
		System.out.println("STAGE 2: process types");
		TypeProcessor tProcessor = new TypeProcessor();
		
		// run through all types and make base structures
		System.out.println("Creating bases...");
		ModuleHandler.modules().values().forEach(m -> {
			tProcessor.process(m);
		});

		printAllSymbols();

		// run through them again and actually create the trees
		System.out.println("Finishing bases...");
		ModuleHandler.modules().values().forEach(m -> {
			tProcessor.process(m);
		});

		printAllSymbols();

		// stage 3: process interfaces in all modules
		// stage 4: process service-parameters and input ports in all services
		// stage 5.a: process output ports in all services
		// stage 5.b: process embeddings in all services
		// stage 6: process service behaviors
	}

	private static void printAllSymbols(){
		System.out.println("-----------------------------------------");
		for(Module m : ModuleHandler.modules().values()){
			System.out.println("Module: " + m.name());
			for(Entry<String, TypeStructure> symbol : m.symbols().entrySet()){
				if(symbol.getValue() != null){
					System.out.println("\n" + symbol.getKey() + ": " + symbol.getValue().prettyString());
				}
				else{
					System.out.println("\n" + symbol.getKey() + ": " + null);
				}
			}
			System.out.println("--");
		}
		System.out.println("-----------------------------------------");
	}

	private static void prettyPrintProgram(Program p){
		JoliePrettyPrinter printer = new JoliePrettyPrinter();
		printer.visit(p);
		System.out.println(printer.toString());
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
