package staticTypechecker.entities;

import java.io.IOException;

import jolie.Interpreter;
import jolie.cli.CommandLineException;
import jolie.cli.CommandLineParser;
import jolie.lang.CodeCheckingException;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.SemanticVerifier;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.module.ModuleException;
import jolie.lang.parse.util.ParsingUtils;

/**
 * Represents a Jolie module (file)
 * 
 * @author Kasper Bergstedt (kberg18@student.sdu.dk)
 */
public class Module {
	private String name;			// name of the module
	private String path;			// the path to the folder of the module
	private Program program;		// the Program parsed by the Jolie parser
	private SymbolTable symbols;	// the symbol table of the module

	public Module(String name, String path){
		this.name = name;
		this.path = path;
		this.program = Module.parseProgram(path + "/" + name);
		this.symbols = null;
	}

	public String name(){
		return this.name;
	}

	public String path(){
		return this.path;
	}

	public Program program(){
		return this.program;
	}

	public SymbolTable symbols(){
		return this.symbols;
	}

	public void setSymbols(SymbolTable newSymbols){
		this.symbols = newSymbols;
	}

	/**
	 * This function parses the jolie module with the given name
	 */
	private static Program parseProgram(String name){
		String[] args = {name};

		try{
			final CommandLineParser cmdParser = new CommandLineParser(args, Module.class.getClassLoader());

			Interpreter.Configuration intConf = cmdParser.getInterpreterConfiguration();

			SemanticVerifier.Configuration semVerConfig = new SemanticVerifier.Configuration( intConf.executionTarget() );
			semVerConfig.setCheckForMain( false );

			Program program = ParsingUtils.parseProgram(
				intConf.inputStream(),
				intConf.programFilepath().toURI(),
				intConf.charset(),
				intConf.includePaths(),
				intConf.packagePaths(),
				intConf.jolieClassLoader(),
				intConf.constants(),
				semVerConfig,
				true 
			);

			cmdParser.close();

			return program;
		}
		catch(CommandLineException | IOException | ParserException | CodeCheckingException | ModuleException e){
			System.out.println("Error parsing module: " + name + ":\n" + e);
			System.exit(0);
			return null;
		}
	}
}
