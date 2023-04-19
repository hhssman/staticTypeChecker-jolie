package staticTypechecker;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import staticTypechecker.entities.Module;
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
	private BehaviorProcessor bProcessor = new BehaviorProcessor(false);
	private final String BASE_PATH = "./src/test/files/";

    /**
     * Test the symbol checker
     */
    @Test
    public void testSymbolChecking(){
        String moduleName = this.BASE_PATH + "testSymbolCollector.ol";
		boolean res = TestSymbolChecking.test(moduleName);
		assertTrue(res);
    }

	@Test
	public void testNil(){
		String moduleName = "testNil.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		Type target = Type.VOID();

		assertTrue( result.equals(target) );
	}

	@Test
	public void testSeq(){
		String moduleName = "testSeq.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		InlineType target = Type.VOID();
		InlineType a = Type.INT();
		InlineType b = Type.STRING();

		target.addChildUnsafe("a", a);
		target.addChildUnsafe("b", b);

		assertTrue( result.equals(target) );
	}

	@Test
	public void testNotify(){
		String moduleName = "testNotify.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		InlineType target = Type.VOID();
		InlineType inputType = Type.INT();
		inputType.addChildUnsafe("x", Type.STRING());
		inputType.addChildUnsafe("y", Type.INT());
		target.addChildUnsafe("inputType", inputType);

		assertTrue( result.equals(target) );
	}

	@Test
	public void testOneWay(){
		String moduleName = "testOneWay.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		InlineType target = Type.VOID();
		InlineType inputType = Type.INT();
		InlineType p = Type.INT();

		inputType.addChildUnsafe("x", Type.STRING());
		inputType.addChildUnsafe("y", Type.INT());
		p.addChildUnsafe("x", Type.STRING());
		p.addChildUnsafe("y", Type.INT());

		target.addChildUnsafe("inputType", inputType);
		target.addChildUnsafe("p", p);

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
			visitor.process(module, false);
		}

		return module;
	}
}
