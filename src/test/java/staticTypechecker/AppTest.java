package staticTypechecker;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

import jolie.util.Pair;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Symbol;
import staticTypechecker.entities.SymbolTable;
import staticTypechecker.utils.ModuleHandler;
import staticTypechecker.entities.InlineType;
import staticTypechecker.entities.Type;
import staticTypechecker.entities.Symbol.SymbolType;
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
	public static final String BASE_PATH = "./src/test/files/";
	private BehaviorProcessor bProcessor = new BehaviorProcessor(false);

    /**
     * Test the symbol checker
     */
    @Test
    public void testSymbolChecking(){
		assertTrue(SymbolCollectorTester.run());
    }

	@Test
	public void testTypeProcessor(){
		assertTrue(TypeProcessorTester.run());
	}

	@Test
	public void testInterfaceProcessor(){
		assertTrue(InterfaceProcessorTester.run());
	}

	// @Test
	// public void testNil(){
	// 	String moduleName = "testNil.ol";
	// 	Module module = this.readyForBehaviourProcessor(moduleName, "testFilesForBehaviours");

	// 	Type result = this.bProcessor.process(module);

	// 	Type target = Type.VOID();

	// 	assertTrue( result.equals(target) );
	// }

	// @Test
	// public void testSeq(){
	// 	String moduleName = "testSeq.ol";
	// 	Module module = this.readyForBehaviourProcessor(moduleName, "testFilesForBehaviours");

	// 	Type result = this.bProcessor.process(module);

	// 	InlineType target = Type.VOID();
	// 	InlineType a = Type.INT();
	// 	InlineType b = Type.STRING();

	// 	target.addChildUnsafe("a", a);
	// 	target.addChildUnsafe("b", b);

	// 	assertTrue( result.equals(target) );
	// }

	// @Test
	// public void testNotify(){
	// 	String moduleName = "testNotify.ol";
	// 	Module module = this.readyForBehaviourProcessor(moduleName, "testFilesForBehaviours");

	// 	Type result = this.bProcessor.process(module);

	// 	InlineType target = Type.VOID();
	// 	InlineType inputType = Type.INT();
	// 	inputType.addChildUnsafe("x", Type.STRING());
	// 	inputType.addChildUnsafe("y", Type.INT());
	// 	target.addChildUnsafe("inputType", inputType);

	// 	assertTrue( result.equals(target) );
	// }

	// @Test
	// public void testOneWay(){
	// 	String moduleName = "testOneWay.ol";
	// 	Module module = this.readyForBehaviourProcessor(moduleName, "testFilesForBehaviours");

	// 	Type result = this.bProcessor.process(module);

	// 	InlineType target = Type.VOID();
	// 	InlineType inputType = Type.INT();
	// 	InlineType p = Type.INT();

	// 	inputType.addChildUnsafe("x", Type.STRING());
	// 	inputType.addChildUnsafe("y", Type.INT());
	// 	p.addChildUnsafe("x", Type.STRING());
	// 	p.addChildUnsafe("y", Type.INT());

	// 	target.addChildUnsafe("inputType", inputType);
	// 	target.addChildUnsafe("p", p);

	// 	assertTrue( result.equals(target) );
	// }

	/**
	 * Runs the processors in order up to and including the one specified in parameter steps on the given modules.
	 * Step 0: symbolcollector,
	 * Step 1: type processor,
	 * Step 2: interface processor,
	 * Step 3: input port processor,
	 * Step 4: output port processor,
	 * Step 5: behaviour processor 
	 */
	public static void runProcessors(List<Module> modules, int steps){
		TypeCheckerVisitor[] visitors = {
			new SymbolCollector(),
			new TypeProcessor(),
			new InterfaceProcessor(),
			new InputPortProcessor(),
			new OutputPortProcessor()
		};

		for(int i = 0; i <= steps; i++){
			for(Module m : modules){
				visitors[i].process(m, false);
			}

			for(Module m : modules){
				visitors[i].process(m, true);
			}
		}
	}

	public static boolean testSymbolsForEquality(SymbolTable result, SymbolTable target){
		for(Entry<String, Pair<SymbolType, Symbol>> ent : target.entrySet()){
			String symbolName = ent.getKey();
			Symbol targetSymbol = ent.getValue().value();
			Symbol resultSymbol = result.get(symbolName);

			if(!Symbol.equals(targetSymbol, resultSymbol)){
				System.out.println("FAIL on symbol " + symbolName + ":\n" + resultSymbol.prettyString() + "\n\nis not equal to\n\n" + resultSymbol.prettyString());
				return false;
			}

		}
		
		return true;
	}
}
