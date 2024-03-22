package staticTypechecker;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

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
        String[] none = {};
        String[] hardcodedPathToStdLib = {System.getenv("JOLIE_HOME") + "/packages"};
        SemanticVerifier.Configuration semVerConfig = new SemanticVerifier.Configuration( null );
		semVerConfig.setCheckForMain( false );
        Program program = null;
        try {
            program = ParsingUtils.parseProgram(
                    new ByteArrayInputStream(code.getBytes()),
                    new URI("urn:jolieString"),
                    null,
                    none,
                    hardcodedPathToStdLib,
                    null,
                    new HashMap<>(),
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
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CodeCheckException e) {
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
