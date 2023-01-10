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

public class Module {
	private String name;
	private Program program;
	private SymbolTable symbols;

	public Module(String name){
		this.name = name;
		this.program = Module.parseProgram(name);
		this.symbols = null;
	}

	public String name(){
		return name;
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

	public static Program parseProgram(String name){
		// System.out.println("Name of file: " + name);
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
				false 
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
