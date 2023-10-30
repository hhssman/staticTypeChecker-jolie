package staticTypechecker;

import java.util.ArrayList;
import java.util.List;

import jolie.lang.parse.ast.Program;
import staticTypechecker.entities.ChoiceType;
import staticTypechecker.entities.InlineType;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Type;
import staticTypechecker.visitors.Synthesizer;

public class StatmentTester {

    public static boolean assignStatement() {
        String code = """
            a = 10
            a.x = "h"
            a.x.y = true
            
            b = "h"
            b.h = 20L

            a = b.h

            c = a.x.y
        """;
        Module module = readyModule(code);

        Type result = Synthesizer.get(module).synthesize();

        InlineType target = Type.VOID();

		target.addChildUnsafe("a", Type.LONG().addChild("x", Type.STRING().addChild("y", Type.BOOL())));
		target.addChildUnsafe("b", Type.STRING().addChild("h", Type.LONG()));
		target.addChildUnsafe("c", Type.BOOL());

        return result.equals(target);
    }

    public static boolean parallelStatement() {
        String code = """
            {
                a = 10
                c = "string"
            }
            |
            {
                b = "string"
                c = 10
            }
        """;
        Module module = readyModule(code);
        Type result = Synthesizer.get(module).synthesize();
        return false;
    }

    public static boolean sequenceStatement() {
        String code = """
            a = 10
            b = "hey"    
        """;
        Module module = readyModule(code);

        Type result = Synthesizer.get(module).synthesize();

        InlineType target = Type.VOID();
		InlineType a = Type.INT();
		InlineType b = Type.STRING();

		target.addChildUnsafe("a", a);
		target.addChildUnsafe("b", b);

		return result.equals(target);
    }

    public static boolean choiceStatement() {
        String code = """
            interface MyInterface{
                RequestResponse:
                    reqResFunction(inputType)(outputType)
                OneWay:
                    oneWayFunction(int),
                    oneWayFunction2(string)
            }
            
            type inputType: int {
                x: string
                y: int
            }
            
            type outputType: string {
                x: string | int
            }
            
            service MyService(){
                inputPort in {
                    Location: "socket://localhost:8081"
                    Protocol: http { format = "json" }
                    Interfaces: MyInterface
                }
                
                main{
                    [reqResFunction(arg)(out){
                        out = "hey"
                        out.x = "hi"
                    }]{
                        out.y = 10
                    }
                    [oneWayFunction(input)]{
                        out = 10
                    }
                    [oneWayFunction2(input)]{
                        out = true
                        random = 20
                    }
                }
            }        
        """;
        Module module = readyModule(code, true);
        Type result = Synthesizer.get(module).synthesize();

        ChoiceType target = new ChoiceType();
		
		// first choice, tree1
		InlineType tree1 = Type.VOID();
		tree1.addChildUnsafe("arg", Type.INT().addChild("x", Type.STRING()).addChild("y", Type.INT()));
		tree1.addChildUnsafe("out", Type.STRING().addChild("x", Type.STRING()).addChild("y", Type.INT()));

		// second choice, tree2
		InlineType tree2 = Type.VOID();
		tree2.addChildUnsafe("input", Type.INT());
		tree2.addChildUnsafe("out", Type.INT());

		// third choice, tree3
		InlineType tree3 = Type.VOID();
		tree3.addChildUnsafe("input", Type.STRING());
		tree3.addChildUnsafe("out", Type.BOOL());
		tree3.addChildUnsafe("random", Type.INT());

		target.addChoiceUnsafe(tree1);
		target.addChoiceUnsafe(tree2);
		target.addChoiceUnsafe(tree3);

        return result.equals(target);
    }

    public static boolean oneWayStatement() {
        String code = """
            interface MyInterface{
                OneWay:
                    onewayFunction(inputType)
            }
            
            type inputType: int{
                x: string
                y: int
            }
            
            service MyService(){
                inputPort in {
                    location: "local"
                    protocol: sodep
                    interfaces: MyInterface
                }
                
                main{
                    inputType = 10
                    inputType.x = "hi"
                    inputType.y = 20
            
                    onewayFunction(p)
                }
            }
        """;
        Module module = readyModule(code, true);
        Type result = Synthesizer.get(module).synthesize();

        InlineType target = Type.VOID();
		InlineType inputType = Type.INT();
		InlineType p = Type.INT();

		inputType.addChildUnsafe("x", Type.STRING());
		inputType.addChildUnsafe("y", Type.INT());
		p.addChildUnsafe("x", Type.STRING());
		p.addChildUnsafe("y", Type.INT());

		target.addChildUnsafe("inputType", inputType);
		target.addChildUnsafe("p", p);

		return result.equals(target);
    }

    public static boolean requestStatement() {
        String code = """
            interface MyInterface{
                RequestResponse:
                    reqResFunction(inputType)(outputType)
            }
            
            type inputType: int {
                x: string
                y: int
            }
            
            type outputType: string {
                x: string | int
            }
            
            service MyService(){
                inputPort in {
                    Location: "socket://localhost:8081"
                    Protocol: http { format = "json" }
                    Interfaces: MyInterface
                }
            
                main{
                    arg = 10
                    arg.x = "hi"
                    arg.y = 20
            
                    reqResFunction(arg)(out){
                        f = 10
                        out = "hey"
                        out.x = 20
                    }
                }
            }   
        """;
        Module module = readyModule(code, true);
        Type result = Synthesizer.get(module).synthesize();

        InlineType target = Type.VOID();

		// arg
		InlineType arg = Type.INT().addChild("x", Type.STRING()).addChild("y", Type.INT());
		target.addChildUnsafe("arg", arg);

		// f
		InlineType f = Type.INT();
		target.addChildUnsafe("f", f);

		// out
		InlineType out = Type.STRING().addChild("x", Type.INT());
		target.addChildUnsafe("out", out);

		return result.equals(target);
    }

    public static boolean notifyStatement() {
        String code = """
            interface MyInterface{
                OneWay:
                    onewayFunction(inputType)
            }
            
            type inputType: int{
                x: string
                y: int
            }
            
            service MyService(){
                outputPort out {
                    Location: "socket://localhost:8081"
                    Protocol: http { format = "json" }
                    Interfaces: MyInterface
                }
                
                main{
                    inputType = 10
                    inputType.x = "hi"
                    inputType.y = 20
            
                    onewayFunction@out(inputType)
                }
            }        
        """;
        Module module = readyModule(code, true);
        Type result = Synthesizer.get(module).synthesize();

		InlineType target = Type.VOID();
		InlineType inputType = Type.INT();
		inputType.addChildUnsafe("x", Type.STRING());
		inputType.addChildUnsafe("y", Type.INT());
		target.addChildUnsafe("inputType", inputType);

		return result.equals(target);
    }

    public static boolean solicitStatement() {
        String code = """
            interface MyInterface{
                RequestResponse:
                    reqResFunction(inputType)(outputType)
            }
            
            type inputType: int {
                x: string
                y: int
            }
            
            type outputType: string {
                x: string | int
            }
            
            service MyService(){
                outputPort out {
                    Location: "socket://localhost:8081"
                    Protocol: http { format = "json" }
                    Interfaces: MyInterface
                }
                
                main{
                    arg = 10
                    arg.x = "hi"
                    arg.y = 20
            
                    reqResFunction@out(arg)(out)
                }
            }        
        """;
        Module module = readyModule(code, true);
        Type result = Synthesizer.get(module).synthesize();

        InlineType target = Type.VOID();

		// arg
		InlineType arg = Type.INT().addChild("x", Type.STRING()).addChild("y", Type.INT());
		target.addChildUnsafe("arg", arg);

		// out
		ChoiceType child = new ChoiceType();
		child.addChoiceUnsafe(Type.STRING());
		child.addChoiceUnsafe(Type.INT());

		InlineType out = Type.STRING().addChild("x", child);
		target.addChildUnsafe("out", out);

		return result.equals(target);
    }

    public static boolean addAssignStatement() {
        String code = """
            a = 5
            b = 5
            a += 1
            b += 0.1
        """;
        Module module = readyModule(code, false);
        Type result = Synthesizer.get(module).synthesize();

        InlineType target = Type.VOID();
        target.addChildUnsafe("a", Type.INT());
        target.addChildUnsafe("b", Type.DOUBLE());

        return result.equals(target);
    }
    
    public static boolean subtractAssignStatement() {
        String code = """
            a = 5
            b = 5
            a -= 1
            b -= 0.1
        """;
        Module module = readyModule(code, false);
        Type result = Synthesizer.get(module).synthesize();

        InlineType target = Type.VOID();
        target.addChildUnsafe("a", Type.INT());
        target.addChildUnsafe("b", Type.DOUBLE());

        return result.equals(target);
    }

    public static boolean multiplyAssignStatement() {
        String code = """
            a = 5
            b = 5
            a *= 1
            b *= 0.1
        """;
        Module module = readyModule(code, false);
        Type result = Synthesizer.get(module).synthesize();

        InlineType target = Type.VOID();
        target.addChildUnsafe("a", Type.INT());
        target.addChildUnsafe("b", Type.DOUBLE());

        return result.equals(target);
    }

    public static boolean divideAssignStatement() {
        String code = """
            a = 5
            b = 5
            a /= 1
            b /= 0.1
        """;
        Module module = readyModule(code, false);
        Type result = Synthesizer.get(module).synthesize();

        InlineType target = Type.VOID();
        target.addChildUnsafe("a", Type.INT());
        target.addChildUnsafe("b", Type.DOUBLE());

        return result.equals(target);
    }

    public static boolean IfStatement() {
        String code = """
            a = 10
            b = "h"
            b.x = true

            if(true){
                b = 10
                c = "Yo"
                a.x = false
            }

            d = 20 
        """;
        Module module1 = readyModule(code, false);
        Type result1 = Synthesizer.get(module1).synthesize();

        ChoiceType target1 = new ChoiceType();

		// first option, not entering the if
		InlineType option1 = Type.VOID();
		option1.addChildUnsafe("a", Type.INT());
		option1.addChildUnsafe("b", Type.STRING().addChild("x", Type.BOOL()));
		option1.addChildUnsafe("d", Type.INT());
		target1.addChoiceUnsafe(option1);

		// second option, entering the if
		InlineType option2 = Type.VOID();
		option2.addChildUnsafe("a", Type.INT().addChild("x", Type.BOOL()));
		option2.addChildUnsafe("b", Type.INT().addChild("x", Type.BOOL()));
		option2.addChildUnsafe("c", Type.STRING());
		option2.addChildUnsafe("d", Type.INT());
		target1.addChoiceUnsafe(option2);

        code = """
            a = 10
            b = "h"
            b.x = true

            if(true){
                b = 10
                c = "Yo"

                if(false){
                    e = 80
                    c.x = 10
                    b.x.y = "j"
                    a = 5.0
                }

                a.x = false
            }

            d = 20
        """;
        Module module2 = readyModule(code, false);
        Type result2 = Synthesizer.get(module2).synthesize();

        ChoiceType target2 = new ChoiceType();

		// first option, not any if
		target2.addChoiceUnsafe(option1);

		// second option, entering the first if, but not the second
		target2.addChoiceUnsafe(option2);

		// third option, entering both ifs
		InlineType option3 = Type.VOID();
		option3.addChildUnsafe("a", Type.DOUBLE().addChild("x", Type.BOOL()));
		option3.addChildUnsafe("b", Type.INT().addChild("x", Type.BOOL().addChild("y", Type.STRING())));
		option3.addChildUnsafe("c", Type.STRING().addChild("x", Type.INT()));
		option3.addChildUnsafe("d", Type.INT());
		option3.addChildUnsafe("e", Type.INT());
		target2.addChoiceUnsafe(option3);

        return result1.equals(target1) && result2.equals(target2);
		
    }

    private static Module readyModule(String jolieCode) {
        Program program = Parser.simpleParser(jolieCode);

        Module module = new Module("jolie simpel string", program);
        
        List<Module> modules = new ArrayList<>();
        modules.add(module);
        AppTest.runProcessors(modules, 4);

        return module;
    }

    private static Module readyModule(String jolieCode, boolean notSimpel) {
        if(!notSimpel) return readyModule(jolieCode);

        Program program = Parser.parser(jolieCode);

        Module module = new Module("jolie simpel string", program);
        
        List<Module> modules = new ArrayList<>();
        modules.add(module);
        AppTest.runProcessors(modules, 4);

        return module;
    }
}
