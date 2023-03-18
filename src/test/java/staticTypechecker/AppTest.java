package staticTypechecker;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jolie.lang.NativeType;
import jolie.lang.parse.OLVisitor;
import jolie.lang.parse.ast.AssignStatement;
import jolie.lang.parse.ast.NullProcessStatement;
import jolie.lang.parse.ast.SequenceStatement;
import jolie.lang.parse.ast.VariablePathNode;
import jolie.lang.parse.ast.expression.ConstantBoolExpression;
import jolie.lang.parse.ast.expression.ConstantIntegerExpression;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.ModuleHandler;
import staticTypechecker.typeStructures.InlineType;
import staticTypechecker.typeStructures.Type;
import staticTypechecker.visitors.BehaviorProcessor;
import staticTypechecker.visitors.InputPortProcessor;
import staticTypechecker.visitors.InterfaceProcessor;
import staticTypechecker.visitors.OutputPortProcessor;
import staticTypechecker.visitors.SymbolCollector;
import staticTypechecker.visitors.TypeCheckerVisitor;
import staticTypechecker.visitors.TypeProcessor;

/**
 * Unit test for simple App.
 */
public class AppTest{
	// private String moduleName = "./src/test/files/testFile.ol";
	// private Module module = new Module(this.moduleName);
	// private Synthesizer synth = Synthesizer.get(new Module(this.moduleName));
	private BehaviorProcessor bProcessor = new BehaviorProcessor();
	private final String BASE_PATH = "./src/test/files/";

    /**
     * Test the symbol checker
     */
    @Test
    public void testSymbolChecking(){
        // String moduleName = "../files/test.ol";
		// ModuleHandler.loadModule(moduleName);
		
		// SymbolCollector sCollector = new SymbolCollector();
		// sCollector.process(ModuleHandler.get(moduleName));
    }

	@Test
	public void testNil(){
		String moduleName = "testNil.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		Type target = new InlineType(BasicTypeDefinition.of(NativeType.VOID), null, null, false);

		assertTrue( result.equals(target) );
	}

	@Test
	public void testSeq(){
		String moduleName = "testSeq.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		InlineType target = new InlineType(BasicTypeDefinition.of(NativeType.VOID), null, null, false);
		InlineType a = new InlineType(BasicTypeDefinition.of(NativeType.INT), null, null, false);
		InlineType b = new InlineType(BasicTypeDefinition.of(NativeType.STRING), null, null, false);

		target.addChildUnsafe("a", a);
		target.addChildUnsafe("b", b);

		assertTrue( result.equals(target) );
	}

	@Test
	public void testNotify(){
		String moduleName = "testNotify.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		InlineType target = new InlineType(BasicTypeDefinition.of(NativeType.VOID), null, null, false);
		InlineType inputType = new InlineType(BasicTypeDefinition.of(NativeType.INT), null, null, false);
		inputType.addChildUnsafe("x", new InlineType(BasicTypeDefinition.of(NativeType.STRING), null, null, false));
		inputType.addChildUnsafe("y", new InlineType(BasicTypeDefinition.of(NativeType.INT), null, null, false));
		target.addChildUnsafe("inputType", inputType);

		System.out.println(target.prettyString());

		assertTrue( result.equals(target) );
	}

	private Module initializeModule(String moduleName){
		moduleName = this.BASE_PATH + moduleName;

		Module module = new Module(moduleName);

		TypeCheckerVisitor[] visitors = {
			new SymbolCollector(),
			new TypeProcessor(),
			new InterfaceProcessor(),
			new InputPortProcessor(),
			new OutputPortProcessor()
		};

		for(TypeCheckerVisitor visitor : visitors){
			visitor.process(module);
		}

		return module;
	}
}
