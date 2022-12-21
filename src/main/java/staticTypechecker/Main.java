package staticTypechecker;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.HashMap;

import jolie.Interpreter;
import jolie.JolieURLStreamHandlerFactory;
import jolie.cli.CommandLineException;
import jolie.lang.CodeCheckingException;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.SemanticVerifier;
import jolie.lang.parse.ast.InterfaceDefinition;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.ast.OperationDeclaration;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.ast.RequestResponseOperationDeclaration;
import jolie.lang.parse.ast.courier.CourierChoiceStatement.OperationRequestResponseBranch;
import jolie.lang.parse.module.ModuleException;
import jolie.lang.parse.util.ParsingUtils;
import staticTypechecker.slicerLib.JoliePrettyPrinter;
import staticTypechecker.slicerLib.JolieSlicerCommandLineParser;
import staticTypechecker.typeStructures.TypeChoiceStructure;
import staticTypechecker.typeStructures.TypeInlineStructure;
import staticTypechecker.typeStructures.TypeStructure;


public class Main {

	public static void main( String[] args ) {
		Module m = new Module(args[0]);

		tester(m.program(), m.symbols());

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

	private static void tester(Program p, SymbolTable symbols){
		TypeInlineStructure t = (TypeInlineStructure)symbols.getStructure("RecursiveType");
		System.out.println(t.prettyString());
	}
}
