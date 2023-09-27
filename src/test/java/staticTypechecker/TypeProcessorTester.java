package staticTypechecker;

import java.util.List;

import staticTypechecker.utils.ModuleHandler;
import staticTypechecker.entities.Symbol.SymbolType;
import staticTypechecker.entities.SymbolTable;
import staticTypechecker.entities.ChoiceType;
import staticTypechecker.entities.InlineType;
import staticTypechecker.entities.Type;
import staticTypechecker.entities.Module;;

public class TypeProcessorTester {
	public static boolean run(){
		String moduleName = AppTest.BASE_PATH + "testFilesForTypeProcessor/main.ol";
		List<Module> modules = ModuleHandler.loadModule(moduleName);
		AppTest.runProcessors(modules, 1);

		SymbolTable result = ModuleHandler.get(moduleName).symbols();
		SymbolTable target = new SymbolTable();

		// add all the types manually

		// type importedType1
		InlineType importedType1 = Type.INT();
		importedType1.addChildUnsafe("x", Type.INT());
		importedType1.addChildUnsafe("y", Type.INT());
		target.put(SymbolTable.newPair("importedType1", SymbolType.TYPE), importedType1);

		// type importedType2
		InlineType importedType2 = Type.STRING();
		ChoiceType importedC = new ChoiceType();
		importedC.addChoiceUnsafe(Type.STRING());
		importedC.addChoiceUnsafe(Type.INT());
		importedType2.addChildUnsafe("x", importedC);
		target.put(SymbolTable.newPair("importedType2", SymbolType.TYPE), importedType2);

		// type importedCircular
		InlineType importedCircular = Type.VOID();
		InlineType importedK = Type.INT();
		importedK.addChildUnsafe("x", importedCircular);
		importedCircular.addChildUnsafe("x", importedK);
		target.put(SymbolTable.newPair("importedCircular", SymbolType.TYPE), importedCircular);

		// type A
		InlineType a = Type.VOID();
		a.addChildUnsafe("x", Type.INT());
		a.addChildUnsafe("y", Type.STRING().addChild("z", Type.BOOL()));
		target.put(SymbolTable.newPair("A", SymbolType.TYPE), a);

		// type B
		InlineType b = Type.ANY().setOpenStatus(true);
		b.addChildUnsafe("x", Type.INT());
		target.put(SymbolTable.newPair("B", SymbolType.TYPE), b);

		// type C
		ChoiceType c = new ChoiceType();
		c.addChoiceUnsafe(Type.STRING());
		c.addChoiceUnsafe(Type.INT());
		target.put(SymbolTable.newPair("C", SymbolType.TYPE), c);

		// type D
		ChoiceType d = new ChoiceType();
		d.addChoiceUnsafe(a);
		d.addChoiceUnsafe(c);
		target.put(SymbolTable.newPair("D", SymbolType.TYPE), d);

		// type E
		InlineType e = Type.INT();
		e.addChildUnsafe("x", a);
		target.put(SymbolTable.newPair("E", SymbolType.TYPE), e);

		// type F
		InlineType f = Type.STRING();
		f.addChildUnsafe("x", f);
		target.put(SymbolTable.newPair("F", SymbolType.TYPE), f);

		// type G and H
		InlineType g = Type.STRING();
		InlineType h = Type.INT();

		g.addChildUnsafe("x", h);
		h.addChildUnsafe("x", g);

		target.put(SymbolTable.newPair("G", SymbolType.TYPE), g);
		target.put(SymbolTable.newPair("H", SymbolType.TYPE), h);

		// type I
		InlineType i = Type.BOOL();
		ChoiceType iChildX = new ChoiceType();
		iChildX.addChoiceUnsafe(i);
		iChildX.addChoiceUnsafe(Type.VOID());
		i.addChildUnsafe("x", iChildX);

		target.put(SymbolTable.newPair("I", SymbolType.TYPE), i);

		// type J
		InlineType j = Type.INT();
		j.addChildUnsafe("x", Type.STRING().addChild("y", j));
		target.put(SymbolTable.newPair("J", SymbolType.TYPE), j);

		// type K
		InlineType k = Type.INT().addChild("x", importedCircular);		
		target.put(SymbolTable.newPair("K", SymbolType.TYPE), k);

		// type Weird
		InlineType weird = Type.INT();
		ChoiceType weirdChild = new ChoiceType();
		weird.addChildUnsafe("y", weirdChild);
		weirdChild.addChoiceUnsafe(weird);
		weirdChild.addChoiceUnsafe(Type.INT());
		target.put(SymbolTable.newPair("Weird", SymbolType.TYPE), weird);

		return AppTest.testSymbolsForEquality(result, target);
	}
}
