package staticTypechecker;

import java.util.Map.Entry;
import java.util.HashMap;

import jolie.lang.parse.ast.Program;
import staticTypechecker.slicerLib.JoliePrettyPrinter;
import staticTypechecker.typeStructures.TypeStructure;


public class Main {

	public static void main( String[] args ) {
		HashMap<String, Module> modules = loadModules(args);

		for(Entry<String, Module> m : modules.entrySet()){
			tester(m.getValue());
		}


		// try
		// ( 
		// 	JolieSlicerCommandLineParser cmdLnParser = JolieSlicerCommandLineParser.create( args, Main.class.getClassLoader() ) 
		// )
		// {

		// 	Interpreter.Configuration intConf = cmdLnParser.getInterpreterConfiguration();

		// 	SemanticVerifier.Configuration semVerConfig = new SemanticVerifier.Configuration( intConf.executionTarget() );
		// 	semVerConfig.setCheckForMain( false );

		// 	Program program = ParsingUtils.parseProgram(
		// 		intConf.inputStream(),
		// 		intConf.programFilepath().toURI(),
		// 		intConf.charset(),
		// 		intConf.includePaths(),
		// 		intConf.packagePaths(),
		// 		intConf.jolieClassLoader(),
		// 		intConf.constants(),
		// 		semVerConfig,
		// 		INCLUDE_DOCUMENTATION );

			// SymbolTable symbols = new SymbolTable(program);
			// tester(program, symbols);


		// 	// typeCheck(program);

		// } catch( CommandLineException e ) {
		// 	System.out.println( e.getMessage() );
		// } catch( IOException | ParserException | CodeCheckingException | ModuleException e ) {
		// 	e.printStackTrace();
		// }
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

	private static void tester(Module m){
		System.out.println("Types in " + m.name());
		for(Entry<String, TypeStructure> e : m.symbols().table().entrySet()){
			System.out.println(e.getKey() + ":");
			System.out.println(e.getValue().prettyString());
			System.out.println();
		}
	}

	private static HashMap<String, Module> loadModules(String[] moduleNames){
		HashMap<String, Module> modules = new HashMap<>();

		for(int i = 0; i < moduleNames.length; i++){
			modules.put(moduleNames[i], new Module(moduleNames[i]));
		}

		return modules;
	}
}
