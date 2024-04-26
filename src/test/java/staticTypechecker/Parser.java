package staticTypechecker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import jolie.Interpreter;
import jolie.cli.CommandLineException;
import jolie.cli.CommandLineParser;
import jolie.lang.CodeCheckException;
import jolie.lang.parse.ParserException;
import jolie.lang.parse.SemanticVerifier;
import jolie.lang.parse.ast.Program;
import jolie.lang.parse.module.ModuleException;
import jolie.lang.parse.util.ParsingUtils;

public class Parser {
    public static void main(String[] args) {
        String testJolieProgram = """
                    a = 10
                    a.x = "h"
                    a.x.y = true
                    
                    b = "h"
                    b.h = 20L
            
                    a = b.h
            
                    c = a.x.y
        """;
        Program program = simpleParser(testJolieProgram);
        int test = 0;
    }

    public static Program parser(String code) {
        File file = new File("src/test/java/staticTypechecker/Parser.java");
        String[] none = {};
        String[] hardcodedPathToStdLib = {System.getenv("JOLIE_HOME") + "/packages"};
        SemanticVerifier.Configuration semVerConfig = new SemanticVerifier.Configuration( null );
		semVerConfig.setCheckForMain( false );
        Program program = null;
        try {
            String[] args = {"src/test/java/staticTypechecker/Parser.java"};
            final CommandLineParser cmdParser = new CommandLineParser(args, Module.class.getClassLoader());
            Interpreter.Configuration intConf = cmdParser.getInterpreterConfiguration();
            program = ParsingUtils.parseProgram(
                    new ByteArrayInputStream(code.getBytes()),
                    intConf.programFilepath().toURI(),
                    intConf.charset(),
                    intConf.includePaths(),
                    hardcodedPathToStdLib,
                    intConf.jolieClassLoader(),
                    intConf.constants(),
                    semVerConfig,
                    true 
                );
            return program;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ModuleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CodeCheckException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CommandLineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return program;
    }

    public static Program simpleParser(String code) {
        String compCode = "service Main{\nmain{\n" + code + "\n}\n}";
        return parser(compCode);
    }
}
