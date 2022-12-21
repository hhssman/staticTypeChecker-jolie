package staticTypechecker;

import java.io.IOException;

import jolie.cli.CommandLineException;
import jolie.cli.CommandLineParser;
import jolie.lang.CodeCheckingException;
import jolie.lang.parse.ParserException;
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
		this.symbols = new SymbolTable(this.program);
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

	public static Program parseProgram(String name){
		String[] args = {name};

		try{
			final CommandLineParser cmdParser = new CommandLineParser(args, Module.class.getClassLoader());

			Program program = ParsingUtils.parseProgram(
				cmdParser.getInterpreterConfiguration().inputStream(),
				cmdParser.getInterpreterConfiguration().programFilepath().toURI(),
				cmdParser.getInterpreterConfiguration().charset(),
				cmdParser.getInterpreterConfiguration().includePaths(),
				cmdParser.getInterpreterConfiguration().packagePaths(),
				cmdParser.getInterpreterConfiguration().jolieClassLoader(),
				cmdParser.getInterpreterConfiguration().constants(),
				cmdParser.getInterpreterConfiguration().executionTarget(),
				true );

				cmdParser.close();

				return program;
		}
		catch(CommandLineException | IOException | ParserException | CodeCheckingException | ModuleException e){
			System.out.println("Error parsing the module: " + e);
			return null;
		}
	}
}
