/*
 * Copyright (C) 2021 Valentino Picotti
 * Copyright (C) 2021 Fabrizio Montesi <famontesi@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

package staticTypechecker;

import java.io.IOException;

import jolie.Interpreter;
import jolie.JolieURLStreamHandlerFactory;
import jolie.cli.CommandLineException;
import jolie.lang.CodeCheckingException;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.SemanticVerifier;
import jolie.lang.parse.ast.OLSyntaxNode;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.module.ModuleException;
import jolie.lang.parse.util.ParsingUtils;


public class Main {

	static {
		JolieURLStreamHandlerFactory.registerInVM();
	}

	private static final boolean INCLUDE_DOCUMENTATION = false;

	public static void main( String[] args ) {
		try
		( 
			JolieSlicerCommandLineParser cmdLnParser = JolieSlicerCommandLineParser.create( args, Main.class.getClassLoader() ) 
		)
		{

			Interpreter.Configuration intConf = cmdLnParser.getInterpreterConfiguration();

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
				INCLUDE_DOCUMENTATION );

			// printProgram(program);
			// prettyPrintProgram(program);
			customVisitor(program);

		} catch( CommandLineException e ) {
			System.out.println( e.getMessage() );
		} catch( IOException | ParserException | CodeCheckingException | ModuleException e ) {
			e.printStackTrace();
		}
	}

	private static void prettyPrintProgram(Program p){
		JoliePrettyPrinter printer = new JoliePrettyPrinter();
		printer.visit(p);
		System.out.println(printer.toString());
	}

	private static void printProgram(Program p){
		for(OLSyntaxNode n : p.children()){
			System.out.println(n.context());
		}
	}

	private static void customVisitor(Program p){
		TypeChecker v = new TypeChecker();
		v.visit(p, null);
	}
}
