package staticTypechecker;

import static org.junit.Assert.assertTrue;

import java.util.Map.Entry;

import org.junit.Test;

import jolie.lang.NativeType;
import jolie.lang.parse.ast.types.BasicTypeDefinition;
import jolie.util.Pair;
import staticTypechecker.entities.Module;
import staticTypechecker.entities.Symbol;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.entities.SymbolTable;
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
	private BehaviorProcessor bProcessor = new BehaviorProcessor(false);
	private final String BASE_PATH = "./src/test/files/";

    /**
     * Test the symbol checker
     */
    @Test
    public void testSymbolChecking(){
        String moduleName = this.BASE_PATH + "/testSymbolCollector.ol";
		Module module = new Module(moduleName);
		
		SymbolCollector sCollector = new SymbolCollector();
		sCollector.process(module, false);

		SymbolTable result = module.symbols();
		SymbolTable target = new SymbolTable();

		target.put("MyInterface", new Pair<SymbolType, Symbol>(SymbolType.INTERFACE, null));
		target.put("imp", new Pair<SymbolType, Symbol>(SymbolType.TYPE, null));
		target.put("helloReqRes", new Pair<SymbolType, Symbol>(SymbolType.OPERATION, null));
		target.put("outputPort", new Pair<SymbolType, Symbol>(SymbolType.OUTPUT_PORT, null));
		target.put("EmbedMe", new Pair<SymbolType, Symbol>(SymbolType.SERVICE, null));
		target.put("param", new Pair<SymbolType, Symbol>(SymbolType.TYPE, null));
		target.put("MyService", new Pair<SymbolType, Symbol>(SymbolType.SERVICE, null));
		target.put("X", new Pair<SymbolType, Symbol>(SymbolType.TYPE, null));
		target.put("Y", new Pair<SymbolType, Symbol>(SymbolType.TYPE, null));
		target.put("helloOneway", new Pair<SymbolType, Symbol>(SymbolType.OPERATION, null));
		target.put("inputPort", new Pair<SymbolType, Symbol>(SymbolType.INPUT_PORT, null));

		boolean isEqual = true;

		for(Entry<String, Pair<SymbolType, Symbol>> ent : target.entrySet()){
			String key = ent.getKey();

			if(!result.containsKey(key)){
				isEqual = false;
				break;
			}

			Pair<SymbolType, Symbol> p1 = result.getPair(key);
			Pair<SymbolType, Symbol> p2 = target.getPair(key);

			if(p1.key() != p2.key()){
				isEqual = false;
				break;
			}
		}

		assertTrue(isEqual);
    }

	@Test
	public void testNil(){
		String moduleName = "testNil.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		Type target = this.createInline(NativeType.VOID);

		assertTrue( result.equals(target) );
	}

	@Test
	public void testSeq(){
		String moduleName = "testSeq.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		InlineType target = this.createInline(NativeType.VOID);
		InlineType a = this.createInline(NativeType.INT);
		InlineType b = this.createInline(NativeType.STRING);

		target.addChildUnsafe("a", a);
		target.addChildUnsafe("b", b);

		assertTrue( result.equals(target) );
	}

	@Test
	public void testNotify(){
		String moduleName = "testNotify.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		InlineType target = this.createInline(NativeType.VOID);
		InlineType inputType = this.createInline(NativeType.INT);
		inputType.addChildUnsafe("x", this.createInline(NativeType.STRING));
		inputType.addChildUnsafe("y", this.createInline(NativeType.INT));
		target.addChildUnsafe("inputType", inputType);

		assertTrue( result.equals(target) );
	}

	@Test
	public void testOneWay(){
		String moduleName = "testOneWay.ol";
		Module module = this.initializeModule(moduleName);

		Type result = this.bProcessor.process(module);

		InlineType target = this.createInline(NativeType.VOID);
		InlineType inputType = this.createInline(NativeType.INT);
		InlineType p = this.createInline(NativeType.INT);

		inputType.addChildUnsafe("x", this.createInline(NativeType.STRING));
		inputType.addChildUnsafe("y", this.createInline(NativeType.INT));
		p.addChildUnsafe("x", this.createInline(NativeType.STRING));
		p.addChildUnsafe("y", this.createInline(NativeType.INT));

		target.addChildUnsafe("inputType", inputType);
		target.addChildUnsafe("p", p);

		assertTrue( result.equals(target) );
	}

	private InlineType createInline(NativeType nativeType){
		return new InlineType(BasicTypeDefinition.of(nativeType), null, null, false);
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
